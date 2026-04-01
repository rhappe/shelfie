import subprocess
import os

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.routes import auth, categories, pantry

app = FastAPI(title="Shelfie API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=[o.strip() for o in settings.cors_origins.split(",")],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router)
app.include_router(categories.router)
app.include_router(pantry.router)


@app.on_event("startup")
async def run_migrations():
    """Run Alembic migrations on startup."""
    alembic_ini = os.path.join(os.path.dirname(os.path.dirname(__file__)), "alembic.ini")
    if os.path.exists(alembic_ini):
        subprocess.run(
            ["alembic", "-c", alembic_ini, "upgrade", "head"],
            check=True,
        )
