from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str = "postgresql+asyncpg://shelfie:shelfie@db:5432/shelfie"
    jwt_secret: str
    jwt_expiry_days: int = 90

    model_config = {"env_file": ".env", "extra": "ignore"}


settings = Settings()
