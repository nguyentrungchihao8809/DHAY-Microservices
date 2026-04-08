import asyncio
import json
import time  # 1. Thêm thư viện time
from app.agents.graph import matching_agent
from app.tools.retriever_tool import ingest_docs

def format_json_output(data):
    """Hàm bổ trợ để trích xuất và làm đẹp JSON từ phản hồi của AI"""
    if isinstance(data, dict):
        return json.dumps(data, indent=2, ensure_ascii=False)
    
    if isinstance(data, str):
        try:
            clean_json = data.replace('```json', '').replace('```', '').strip()
            parsed = json.loads(clean_json)
            return json.dumps(parsed, indent=2, ensure_ascii=False)
        except:
            return data
    return str(data)

async def run_test():
    print("\n" + "="*50)
    print("🚀 DHAY AI-MATCHING AGENT TESTER")
    print("="*50)
    
    print("\n[1/3] 📚 Đang nạp SOP (Standard Operating Procedure)...")
    try:
        ingest_docs()
        print("✅ SOP đã sẵn sàng trong ChromaDB.")
    except Exception as e:
        print(f"⚠️ Cảnh báo nạp SOP: {e}")
    
    inputs = {
        "trip_id": 2001,
        "candidates": [],
        "relevant_policies": "",
        "routing_data": [],
        "final_decision": ""
    }
    
    print("\n[2/3] 🧠 Agent đang thực hiện quy trình suy luận...")
    print("-" * 30)
    
    # 2. Đánh dấu thời gian bắt đầu
    start_time = time.perf_counter() 
    
    final_state = inputs
    async for output in matching_agent.astream(inputs):
        for key, value in output.items():
            # Tính thời gian riêng cho từng Node (Optional)
            print(f"📍 Node: {key}")
            final_state.update(value)
            
            if key == "retrieve_candidates":
                print(f"   ∟ Tìm thấy {len(value.get('candidates', []))} ứng viên tiềm năng.")
            elif key == "calculate_metrics":
                print(f"   ∟ OSRM đã tính xong thông số lộ trình.")
            elif key == "retrieve_policies":
                print(f"   ∟ Đã trích xuất luật chơi từ RAG.")
            elif key == "reasoning":
                print(f"   ∟ Gemini đã hoàn tất suy luận.")
    
    # 3. Đánh dấu thời gian kết thúc
    end_time = time.perf_counter()
    total_latency = end_time - start_time

    print("-" * 30)
    print(f"⏱️  TỔNG THỜI GIAN XỬ LÝ: {total_latency:.2f} giây") # Hiển thị 2 chữ số thập phân
    
    print("\n[3/3] 🏁 KẾT QUẢ CUỐI CÙNG:")
    print("=" * 60)
    
    decision = final_state.get("final_decision")
    
    if decision:
        formatted_decision = format_json_output(decision)
        print(formatted_decision)
    else:
        print("❌ Agent không đưa ra được quyết định.")
    
    print("=" * 60 + "\n")

if __name__ == "__main__":
    try:
        asyncio.run(run_test())
    except KeyboardInterrupt:
        print("\nĐã dừng test.")
    except Exception as e:
        print(f"\n❌ Lỗi thực thi: {e}")