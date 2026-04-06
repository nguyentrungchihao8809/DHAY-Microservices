from langgraph.graph import StateGraph, END
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_google_genai import HarmCategory, HarmBlockThreshold
from app.core.config import settings
from app.agents.prompts import SYSTEM_PROMPT
from app.agents.state import AgentState
from app.tools.osrm_tool import osrm_tool
from app.tools.db_tool import find_nearby_passengers
import json
import re

# Khởi tạo LLM
llm = ChatGoogleGenerativeAI(
    model=settings.MODEL_NAME,
    google_api_key=settings.GEMINI_API_KEY,
    temperature=0.0,
    max_tokens=2048,
    safety_settings={
        HarmCategory.HARM_CATEGORY_HARASSMENT: HarmBlockThreshold.BLOCK_NONE,
        HarmCategory.HARM_CATEGORY_HATE_SPEECH: HarmBlockThreshold.BLOCK_NONE,
        HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT: HarmBlockThreshold.BLOCK_NONE,
        HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT: HarmBlockThreshold.BLOCK_NONE,
    },
)

# ==================== CÁC HÀM BỔ TRỢ ====================

def parse_wkt_point(wkt_str):
    """Trích xuất [lng, lat] từ chuỗi 'POINT(lng lat)' của PostGIS"""
    if not wkt_str: return None
    try:
        coords = re.findall(r"[-+]?\d*\.\d+|\d+", wkt_str)
        return [float(coords[0]), float(coords[1])] if len(coords) >= 2 else None
    except Exception:
        return None

# ==================== CÁC NODE ====================

def retrieve_candidates_node(state: AgentState):
    """Node 1: Tìm khách hàng tiềm năng gần lộ trình từ PostGIS"""
    print("🔎 [DEBUG] Node: retrieve_candidates")
    
    # Mặc định tìm trong bán kính 1500m
    candidates = find_nearby_passengers(
        trip_id=state.get("trip_id"), 
        buffer_meters=1500
    )
    
    return {"candidates": candidates}


def calculate_metrics_node(state: AgentState):
    """Node 2: Tính toán quãng đường lệch (Detour) và thời gian thực tế qua OSRM"""
    print("🚗 [DEBUG] Node: calculate_metrics")
    candidates = state.get("candidates", [])
    metrics_list = []

    # Giả định tọa độ hiện tại của tài xế (Thực tế nên lấy từ database d.route_geom)
    # Ở đây dùng tọa độ demo trung tâm TP.HCM
    driver_loc = [106.66017, 10.762622] 

    for can in candidates:
        pickup_loc = parse_wkt_point(can.get('pickup_point'))
        # Tọa độ điểm đến của khách (Demo)
        dropoff_loc = [106.7017, 10.7726] 

        if pickup_loc:
            metrics = osrm_tool.get_match_metrics(
                driver_loc=driver_loc,
                pickup_loc=pickup_loc,
                dropoff_loc=dropoff_loc
            )
            if metrics:
                metrics['candidate_id'] = can['id']
                metrics_list.append(metrics)

    return {"routing_data": metrics_list}


def retrieve_policies_node(state: AgentState):
    """Node 3: Truy xuất 'Luật chơi' (SOP) từ Vector DB (RAG)"""
    print("📚 [DEBUG] Node: retrieve_policies")
    
    from app.tools.retriever_tool import get_policy_tool
    
    query = f"Quy định ghép chuyến, giới hạn detour và thời gian chờ cho trip {state.get('trip_id')}"
    policies = get_policy_tool(query)
    
    return {"relevant_policies": policies}


def reasoning_node(state: AgentState):
    """Node 4: Agent suy luận dựa trên tất cả dữ liệu đã thu thập"""
    print("🧠 [DEBUG] Node: reasoning")
    
    try:
        # Tổng hợp dữ liệu đầu vào cho bộ não AI
        context = f"""
### 1. LUẬT KINH DOANH (SOP):
{state.get('relevant_policies', 'N/A')}

### 2. DANH SÁCH ỨNG VIÊN (POSTGIS):
{json.dumps(state.get('candidates', []), ensure_ascii=False, indent=2)}

### 3. THÔNG SỐ THỰC TẾ (OSRM):
{json.dumps(state.get('routing_data', []), ensure_ascii=False, indent=2)}
"""

        messages = [
            ("system", SYSTEM_PROMPT),
            ("user", f"""
Hãy phân tích dữ liệu và đưa ra quyết định ghép chuyến tốt nhất cho Trip ID: {state.get('trip_id')}.
Nếu không có ứng viên nào thỏa mãn SOP, hãy trả về is_match: false.

DỮ LIỆU ĐẦU VÀO:
{context}
""")
        ]

        response = llm.invoke(messages)
        print("✅ [DEBUG] Agent đã đưa ra quyết định.")

        return {"final_decision": response.content}

    except Exception as e:
        print(f"❌ Reasoning error: {e}")
        return {"final_decision": json.dumps({"is_match": False, "reasoning": f"Lỗi hệ thống: {str(e)}"}, ensure_ascii=False)}


# ==================== XÂY DỰNG LUỒNG (GRAPH) ====================

workflow = StateGraph(AgentState)

# Thêm các Node vào hệ thống
workflow.add_node("retrieve_candidates", retrieve_candidates_node)
workflow.add_node("calculate_metrics", calculate_metrics_node)
workflow.add_node("retrieve_policies", retrieve_policies_node)
workflow.add_node("reasoning", reasoning_node)

# Thiết lập trình tự chạy (Edges)
workflow.set_entry_point("retrieve_candidates")

workflow.add_edge("retrieve_candidates", "calculate_metrics")
workflow.add_edge("calculate_metrics", "retrieve_policies")
workflow.add_edge("retrieve_policies", "reasoning")
workflow.add_edge("reasoning", END)

# Compile Agent
matching_agent = workflow.compile()

print("✅ AI Matching Agent Graph compiled successfully with 4 nodes!")