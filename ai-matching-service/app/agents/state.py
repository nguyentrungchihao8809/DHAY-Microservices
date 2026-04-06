from typing import Annotated, List, TypedDict, Dict, Any
from langchain_core.messages import BaseMessage
from langgraph.graph.message import add_messages

class AgentState(TypedDict):
    messages: Annotated[List[BaseMessage], add_messages]
    trip_id: int
    candidates: List[dict]
    relevant_policies: str
    routing_data: dict
    routing_metrics: List[dict]
    final_decision: dict