import os
from langchain_google_genai import GoogleGenerativeAIEmbeddings
from langchain_chroma import Chroma                     # ← Import mới
from langchain_community.document_loaders import TextLoader
from langchain_text_splitters import CharacterTextSplitter
from app.core.config import settings

# Khởi tạo embedding model
embeddings = GoogleGenerativeAIEmbeddings(
    model="gemini-embedding-001",
    google_api_key=settings.GEMINI_API_KEY,
)

def ingest_docs():
    """Hàm nạp dữ liệu từ thư mục docs vào ChromaDB"""
    loader = TextLoader("docs/matching_rules.md", encoding='utf-8')
    documents = loader.load()
    
    text_splitter = CharacterTextSplitter(chunk_size=500, chunk_overlap=50)
    docs = text_splitter.split_documents(documents)
    
    # Tạo vectorstore mới
    vectorstore = Chroma.from_documents(
        documents=docs, 
        embedding=embeddings,                    # .from_documents vẫn dùng 'embedding'
        persist_directory=settings.CHROMA_DB_PATH,
        collection_name="sop_matching"           # Nên chỉ định để tránh lỗi
    )
    print("✅ Đã nạp SOP vào ChromaDB thành công!")
    return vectorstore.as_retriever()


def get_policy_tool(query: str):
    """Công cụ lấy SOP từ ChromaDB - ĐÃ SỬA"""
    try:
        print(f"📚 Đang tìm SOP cho query: {query[:80]}...")

        # Sửa quan trọng: dùng langchain_chroma + embedding_function
        vectorstore = Chroma(
            persist_directory=settings.CHROMA_DB_PATH,
            embedding_function=embeddings,           # ← Sửa thành embedding_function
            collection_name="sop_matching"
        )
        
        results = vectorstore.similarity_search(query, k=4)   # Tăng lên 4 để có thêm ngữ cảnh
        
        context = "Dưới đây là các luật kinh doanh (SOP) liên quan:\n\n"
        for i, res in enumerate(results):
            context += f"**Quy định {i+1}:**\n{res.page_content}\n\n"
        
        print(f"✅ Trả về {len(results)} đoạn SOP từ ChromaDB")
        return context
        
    except Exception as e:
        print(f"❌ Lỗi khi lấy SOP từ ChromaDB: {e}")
        return "Không tìm thấy quy định SOP nào liên quan. Vui lòng kiểm tra lại dữ liệu."