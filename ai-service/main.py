import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse

from models import AnalyzeRequest, AnalyzeResponse
import reasoning_engine

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("ArchSentinel AI Service starting up")
    yield
    logger.info("ArchSentinel AI Service shutting down")


app = FastAPI(
    title="ArchSentinel AI Service",
    description="AI-powered architecture analysis for Pull Requests",
    version="1.0.0",
    lifespan=lifespan,
)


@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "archsentinel-ai"}


@app.post("/analyze", response_model=AnalyzeResponse)
async def analyze(request: AnalyzeRequest):
    logger.info("Received analysis request with %d violations", len(request.violations))
    try:
        result = await reasoning_engine.analyze(
            impact_report=request.impact_report,
            graph_data=request.graph_data,
            violations=request.violations,
        )
        return AnalyzeResponse(
            explanation=result.get("explanation", ""),
            suggestions=result.get("suggestions", []),
            risk_assessment=result.get("riskAssessment", "UNKNOWN"),
        )
    except Exception as e:
        logger.error("Analysis failed: %s", str(e))
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")


@app.exception_handler(Exception)
async def global_exception_handler(request, exc):
    logger.error("Unhandled exception: %s", str(exc))
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"},
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
