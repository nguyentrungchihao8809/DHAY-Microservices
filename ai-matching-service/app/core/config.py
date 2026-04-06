import os
from dotenv import load_dotenv
from pydantic import Field

load_dotenv()

class Settings:
    # PROJECT INFO
    PROJECT_NAME: str = "DHAY AI-Matching Service"
    VERSION: str = "1.0.0"

    # AI SETTINGS - ĐÃ SỬA
    GEMINI_API_KEY: str = os.getenv("GEMINI_API_KEY")
    
    # Model mặc định mới nhất (tháng 4/2026)
    MODEL_NAME: str = os.getenv("MODEL_NAME", "gemini-2.5-flash")

    # DATABASE & VECTOR
    DATABASE_URL: str = os.getenv("DATABASE_URL")
    CHROMA_DB_PATH: str = os.getenv("CHROMA_DB_PATH", "./chroma_db")

    # TOOLS URL
    OSRM_SERVER_URL: str = os.getenv("OSRM_SERVER_URL", "http://localhost:5000")

    # NETWORK
    HTTP_PORT: int = int(os.getenv("HTTP_PORT", 8000))
    GRPC_PORT: int = int(os.getenv("GRPC_PORT", 50051))

    def validate_keys(self):
        """Kiểm tra các key quan trọng"""
        if not self.GEMINI_API_KEY:
            raise ValueError("❌ GEMINI_API_KEY is missing! Check your .env file.")
        
        print(f"✅ Config loaded for: {self.PROJECT_NAME}")
        print(f"   • Model: {self.MODEL_NAME}")
        print(f"   • ChromaDB path: {self.CHROMA_DB_PATH}")
        
        # Cảnh báo nếu vẫn dùng model cũ
        old_models = ["gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash"]
        if self.MODEL_NAME in old_models:
            print(f"⚠️  CẢNH BÁO: Model {self.MODEL_NAME} đã bị deprecated. Nên dùng gemini-2.5-flash hoặc mới hơn.")

settings = Settings()

# Tự động check khi import
settings.validate_keys()