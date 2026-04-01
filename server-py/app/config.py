from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str
    jwt_secret: str
    jwt_expiry_days: int = 90
    cors_origins: str = "*"

    model_config = {"env_file": ".env", "extra": "ignore"}


settings = Settings()
