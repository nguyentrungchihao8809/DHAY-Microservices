from langgraph.graph import StateGraph, END
from langchain_google_genai import ChatGoogleGenerativeAI
from pydantic import BaseModel, Field
from typing import Optional, List
import json
import re

from app.core.config import settings
from app.agents.prompts import SYSTEM_PROMPT
from app.agents.state import AgentState
from app.tools.osrm_tool import OSRMTool
from app.tools.db_tool import find_nearby_passengers, get_trip_geometry

# 1. Khởi tạo Tools và LLM
osrm_tool = OSRMTool()
llm = ChatGoogleGenerativeAI(
    model=settings.MODEL_NAME,
    google_api_key=settings.GEMINI_API_KEY,
    temperature=0.0
)

# 2. Định nghĩa Structured Output Schema (Sửa lỗi Unsupported schema type)
class MatchingDecision(BaseModel):
    is_match: bool = Field(description="Quyết định cuối cùng: Có ghép chuyến hay không")
    match_score: float = Field(description="Điểm số phù hợp từ 0.0 đến 1.0 dựa trên SOP")
    reasoning: str = Field(description="Giải thích lý do chi tiết bằng tiếng Việt")
    candidate_id: Optional[int] = Field(None, description="ID của khách hàng được chọn (nếu có)")

# Hàm bổ trợ parse tọa độ từ WKT PostGIS
def parse_wkt_point(wkt_str):
    if not wkt_str: return None
    coords = re.findall(r"[-+]?\d*\.\d+|\d+", wkt_str)
    return [float(coords[0]), float(coords[1])] if len(coords) >= 2 else None

# --- CÁC NODES ---

def retrieve_candidates_node(state: AgentState):
    """Tầng 1: Truy vấn PostGIS để lọc hành khách trong hành lang di chuyển (1.5km)"""
    print("🔎 [Node] retrieve_candidates: Đang tìm khách hàng tiềm năng...")
    candidates = find_nearby_passengers(state.get("trip_id"), buffer_meters=1500)
    return {"candidates": candidates}

def calculate_metrics_node(state: AgentState):
    """Tầng 2: Gọi OSRM tính toán quãng đường lệch (Detour) thực tế"""
    print("📍 [Node] calculate_metrics: Đang tính toán OSRM...")
    trip_id = state["trip_id"]
    candidates = state.get("candidates", [])
    driver_info = get_trip_geometry(trip_id)
    
    if not driver_info or not candidates:
        return {"routing_data": []}

    driver_start = parse_wkt_point(driver_info['start_point'])
    driver_end = parse_wkt_point(driver_info['end_point'])
    baseline_km = driver_info['length_km']

    routing_results = []
    for can in candidates:
        pickup_loc = parse_wkt_point(can['pickup_point'])
        # Tính route: Điểm đầu tài xế -> Điểm đón khách -> Điểm cuối tài xế
        metrics = osrm_tool.get_match_metrics(driver_start, pickup_loc, driver_end)
        
        if metrics:
            detour_km = max(0, metrics['total_dist_km'] - baseline_km)
            routing_results.append({
                "candidate_id": can['id'],
                "passenger_name": can.get('name'),
                "detour_km": round(detour_km, 2),
                "detour_percent": round((detour_km/baseline_km)*100, 2) if baseline_km > 0 else 0,
                "wait_time": 5 # Giả định, có thể update từ ETA thực tế
            })
    
    return {"routing_data": routing_results}

def reasoning_node(state: AgentState):
    """Tầng 3: AI Reasoning ra quyết định dựa trên dữ liệu thực tế và SOP"""
    print("🧠 [Node] reasoning: Gemini đang phân tích...")
    
    # Ép kiểu output theo Pydantic Model
    structured_llm = llm.with_structured_output(MatchingDecision)
    
    input_context = {
        "trip_id": state["trip_id"],
        "routing_metrics": state.get("routing_data", []),
        "business_rules": "Ưu tiên: Detour < 5km và Detour% < 25%. Thời gian chờ < 10 phút."
    }

    messages = [
        ("system", SYSTEM_PROMPT),
        ("user", f"Dữ liệu thực tế cần xử lý: {json.dumps(input_context, ensure_ascii=False)}")
    ]
    
    try:
        # Gọi Gemini và nhận thẳng Object MatchingDecision
        decision = structured_llm.invoke(messages)
        
        # Trả về dictionary để LangGraph lưu vào State
        return {"final_decision": decision.model_dump()}
        
    except Exception as e:
        print(f"❌ Reasoning Error: {e}")
        return {
            "final_decision": {
                "is_match": False, 
                "reasoning": f"Lỗi xử lý logic LLM: {str(e)}",
                "match_score": 0.0
            }
        }

# --- XÂY DỰNG GRAPH ---
workflow = StateGraph(AgentState)

# Thêm các Node vào quy trình
workflow.add_node("retrieve_candidates", retrieve_candidates_node)
workflow.add_node("calculate_metrics", calculate_metrics_node)
workflow.add_node("reasoning", reasoning_node)

# Thiết lập luồng (Edge)
# Đi từ Lọc thô -> Tính toán thực tế -> LLM quyết định
workflow.set_entry_point("retrieve_candidates")
workflow.add_edge("retrieve_candidates", "calculate_metrics")
workflow.add_edge("calculate_metrics", "reasoning")
workflow.add_edge("reasoning", END)

# Compile Agent
matching_agent = workflow.compile()