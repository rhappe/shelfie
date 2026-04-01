import uuid

from fastapi import APIRouter, Depends, status
from fastapi.responses import JSONResponse, Response
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth import get_current_user
from app.database import get_db
from app.models import Category
from app.schemas import CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest

router = APIRouter(prefix="/v1/categories", tags=["categories"])


def _to_response(cat: Category) -> CategoryResponse:
    return CategoryResponse(
        id=str(cat.id),
        name=cat.name,
        description=cat.description,
        color=cat.color,
        sort_order=cat.sort_order,
        household_id=str(cat.household_id),
    )


@router.get("", response_model=list[CategoryResponse], response_model_by_alias=True)
async def list_categories(
    user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    household_id = uuid.UUID(user["household_id"])
    result = await db.execute(
        select(Category)
        .where(Category.household_id == household_id)
        .order_by(Category.sort_order)
    )
    categories = result.scalars().all()
    return [_to_response(c) for c in categories]


@router.post("", status_code=status.HTTP_201_CREATED, response_model=CategoryResponse, response_model_by_alias=True)
async def create_category(
    request: CreateCategoryRequest,
    user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    household_id = uuid.UUID(user["household_id"])
    category = Category(
        name=request.name,
        description=request.description,
        color=request.color,
        sort_order=request.sort_order,
        household_id=household_id,
    )
    db.add(category)
    await db.commit()
    await db.refresh(category)
    return _to_response(category)


@router.put("/{category_id}", response_model=CategoryResponse, response_model_by_alias=True)
async def update_category(
    category_id: str,
    request: UpdateCategoryRequest,
    user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    household_id = uuid.UUID(user["household_id"])
    cat_uuid = uuid.UUID(category_id)
    result = await db.execute(
        select(Category).where(
            Category.id == cat_uuid,
            Category.household_id == household_id,
        )
    )
    category = result.scalars().first()
    if category is None:
        return JSONResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content={"error": "Category not found"},
        )

    category.name = request.name
    category.description = request.description
    category.color = request.color
    category.sort_order = request.sort_order
    await db.commit()
    await db.refresh(category)
    return _to_response(category)


@router.delete("/{category_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_category(
    category_id: str,
    user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    household_id = uuid.UUID(user["household_id"])
    cat_uuid = uuid.UUID(category_id)
    result = await db.execute(
        select(Category).where(
            Category.id == cat_uuid,
            Category.household_id == household_id,
        )
    )
    category = result.scalars().first()
    if category is None:
        return JSONResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content={"error": "Category not found"},
        )

    await db.delete(category)
    await db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
