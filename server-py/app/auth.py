from datetime import datetime, timedelta, timezone

import bcrypt
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
import jwt

from app.config import settings

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/v1/auth/login")

ALGORITHM = "HS256"


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password=password.encode(), salt=bcrypt.gensalt()).decode()


def verify_password(plain: str, hashed: str) -> bool:
    return bcrypt.checkpw(password=plain.encode(), hashed_password=hashed.encode())


def create_token(user_id: str, household_id: str) -> str:
    expire = datetime.now(timezone.utc) + timedelta(days=settings.jwt_expiry_days)
    payload = {
        "userId": user_id,
        "householdId": household_id,
        "exp": expire,
    }
    return jwt.encode(payload=payload, key=settings.jwt_secret, algorithm=ALGORITHM)


async def get_current_user(token: str = Depends(oauth2_scheme)) -> dict:
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail={"error": "Token is not valid or has expired"},
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(jwt=token, key=settings.jwt_secret, algorithms=[ALGORITHM])
        user_id: str | None = payload.get("userId")
        household_id: str | None = payload.get("householdId")
        if user_id is None:
            raise credentials_exception
        return {"user_id": user_id, "household_id": household_id}
    except jwt.PyJWTError:
        raise credentials_exception
