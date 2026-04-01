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
    if not request.email or not request.email.strip():
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content={"error": "Email must not be blank"},
        )
    if len(request.password) < 6:
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content={"error": "Password must be at least 6 characters"},
        )
    if not request.display_name or not request.display_name.strip():
        return JSONResponse(
            status_code=status.HTTP_400_BAD_REQUEST,
            content={"error": "Display name must not be blank"},
        )

    # Check for existing email
    result = await db.execute(select(User).where(User.email == request.email))
    if result.scalars().first() is not None:
        return JSONResponse(
            status_code=status.HTTP_409_CONFLICT,
            content={"error": "Email already registered"},
        )

    invite_code = uuid.uuid4().hex[:16]
    household = Household(
        name=f"{request.display_name}'s Household",
        invite_code=invite_code,
    )
    db.add(household)
    await db.flush()

    user = User(
        email=request.email,
        display_name=request.display_name,
        password_hash=hash_password(request.password),
        household_id=household.id,
        role="OWNER",
    )
    db.add(user)
    await db.commit()

    token = create_token(str(user.id), str(household.id), user.email)
    return AuthResponse(
        token=token,
        user_id=str(user.id),
        email=user.email,
        display_name=user.display_name,
        household_id=str(household.id),
    )


@router.post("/login", response_model=AuthResponse, response_model_by_alias=True)
async def login(request: LoginRequest, db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(User).where(User.email == request.email))
    user = result.scalars().first()

    if user is None or not verify_password(request.password, user.password_hash):
        return JSONResponse(
            status_code=status.HTTP_401_UNAUTHORIZED,
            content={"error": "Invalid email or password"},
        )

    token = create_token(str(user.id), str(user.household_id), user.email)
    return AuthResponse(
        token=token,
        user_id=str(user.id),
        email=user.email,
        display_name=user.display_name,
        household_id=str(user.household_id),
    )
