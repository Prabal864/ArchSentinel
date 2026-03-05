from pydantic import BaseModel, Field
from typing import Any, Dict, List, Optional


class ArchitectureSnapshotModel(BaseModel):
    coupling_score: float = 0.0
    circular_dependencies: int = 0
    complexity_score: float = 0.0
    layer_violations: int = 0
    performance_risks: int = 0
    health_score: float = 0.0
    circular_paths: List[str] = Field(default_factory=list)
    violations: List[str] = Field(default_factory=list)
    risks: List[str] = Field(default_factory=list)


class ImpactReportModel(BaseModel):
    coupling_delta: float = 0.0
    new_circular_dependency: bool = False
    new_circular_count: int = 0
    complexity_delta: float = 0.0
    new_layer_violations: int = 0
    new_performance_risks: int = 0
    health_score_change: float = 0.0
    risk_level: str = "LOW"
    pr_health_score: float = 0.0
    circular_paths: List[str] = Field(default_factory=list)
    violations: List[str] = Field(default_factory=list)
    risks: List[str] = Field(default_factory=list)
    # New pinpointed fields
    coupling_hotspots: List[Dict[str, Any]] = Field(default_factory=list)
    complex_methods: List[Dict[str, Any]] = Field(default_factory=list)
    class_details: List[Dict[str, Any]] = Field(default_factory=list)
    total_classes: int = 0
    total_methods: int = 0
    base_health_score: float = 0.0
    base_total_classes: int = 0
    base_total_methods: int = 0


class AnalyzeRequest(BaseModel):
    impact_report: Dict[str, Any] = Field(default_factory=dict, alias="impactReport")
    graph_data: Dict[str, Any] = Field(default_factory=dict, alias="graphData")
    violations: List[str] = Field(default_factory=list)

    class Config:
        populate_by_name = True


class AnalyzeResponse(BaseModel):
    explanation: str
    suggestions: List[str]
    risk_assessment: str
