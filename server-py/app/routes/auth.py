import uuid

from fastapi import APIRouter, Depends, status
from fastapi.responses import JSONResponse
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth import create_token, hash_password, verify_password
from app.database import get_db
from app.models import Household, User
from app.schemas import AuthResponse, LoginRequest, RegisterRequest

router = APIRouter(prefix="/v1/auth", tags=["auth"])


@router.post("/register", status_code=status.HTTP_201_CREATED, response_model=AuthResponse, response_model_by_alias=True)
async def register(request: RegisterRequest, db: AsyncSession = Depends(get_db)):
    # Check for existing username
    result = await db.execute(select(User).where(User.username == request.username))
    if result.scalars().first() is not None:
        return JSONResponse(
            status_code=status.HTTP_409_CONFLICT,
            content={"error": "Username already taken"},
        )

    invite_code = uuid.uuid4().hex[:16]
    household = Household(
        name=f"{request.display_name}'s Household",
        invite_code=invite_code,
    )
    db.add(household)
    await db.flush()

    user = User(
        username=request.username,
        display_name=request.display_name,
        password_hash=hash_password(password=request.password),
        household_id=household.id,
        role="OWNER",
    )
    db.add(user)
    await db.commit()

    token = create_token(user_id=str(user.id), household_id=str(household.id))
    return AuthResponse(
        token=token,
        user_id=str(user.id),
        username=user.username,
        display_name=user.display_name,
        household_id=str(household.id),
    )


@router.post("/login", response_model=AuthResponse, response_model_by_alias=True)
async def login(request: LoginRequest, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.username == request.username))
    user = result.scalars().first()

    if user is None or not verify_password(plain=request.password, hashed=user.password_hash):
        return JSONResponse(
            status_code=status.HTTP_401_UNAUTHORIZED,
            content={"error": "Invalid username or password"},
        )

    token = create_token(user_id=str(user.id), household_id=str(user.household_id))
    return AuthResponse(
        token=token,
        user_id=str(user.id),
        username=user.username,
        display_name=user.display_name,
        household_id=str(user.household_id),
    )
