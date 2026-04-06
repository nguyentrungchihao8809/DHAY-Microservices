from app.core.config import settings
from langchain_google_genai import ChatGoogleGenerativeAI

def test_brain():
    try:
        llm = ChatGoogleGenerativeAI(model=settings.MODEL_NAME, google_api_key=settings.GEMINI_API_KEY)
        response = llm.invoke("Chào bạn, bạn đã sẵn sàng làm bộ não cho DHAY chưa?")
        print("--- PHẢN HỒI TỪ GEMINI ---")
        print(response.content)
        print("--------------------------")
    except Exception as e:
        print(f"❌ Lỗi kết nối Gemini: {e}")

if __name__ == "__main__":
    test_brain()