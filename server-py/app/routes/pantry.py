from datetime import date, datetime, timezone
from uuid import UUID
from typing import Optional

from fastapi import APIRouter, Depends, Query, status
from fastapi.responses import JSONResponse, Response
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.auth import get_current_user
from app.database import get_db
from app.models import PantryItem
from app.schemas import (
    CreatePantryItemRequest,
    PantryItemResponse,
    UpdatePantryItemRequest,
)

router = APIRouter(prefix="/v1/pantry/items", tags=["pantry"])


def _to_response(item: PantryItem) -> PantryItemResponse:
    return PantryItemResponse(
        id=str(item.id),
        name=item.name,
        quantity=float(item.quantity),
        unit=item.unit,
        category_id=str(item.category_id) if item.category_id else None,
        expiration_date=item.expiration_date.isoformat() if item.expiration_date else None,
        low_stock_threshold=float(item.low_stock_threshold),
        notify_on_low_stock=item.notify_on_low_stock,
        barcode=item.barcode,
        household_id=str(item.household_id),
        created_at=item.created_at.isoformat() if item.created_at else "",
        updated_at=item.updated_at.isoformat() if item.updated_at else "",
    )


@router.get("", response_model=list[PantryItemResponse], response_model_by_alias=True)
async def list_items(
    search: Optional[str] = Query(None),
    categoryId: Optional[str] = Query(None),
    sortBy: Optional[str] = Query("name"),
    user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    household_id = UUID(user["household_id"])
    query = select(PantryItem).where(PantryItem.household_id == household_id)

    if search:
        query = query.where(PantryItem.name.ilike(f"%{search}%"))
    if categoryId:
        query = query.where(PantryItem.category_id == UUID(categoryId))

    if sortBy == "quantity":
        query = query.order_by(PantryItem.quantity)
    elif sortBy == "expirationDate":
        query = query.order_by(PantryItem.expiration_date)
    else:
        query = query.order_by(PantryItem.name)

    result = await db.execute(query)
    items = result.scalars().all()
    return [_to_response(i) for i in items]


@router.post("", status_code=status.HTTP_201_CREATED, response_model=PantryItemResponse, response_model_by_alias=True)
async def create_item(
    request: CreatePantryItemRequest,
    user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    household_id = UUID(user["household_id"])
    now = datetime.now(timezone.utc)

    item = PantryItem(
        name=request.name,
        quantity=request.quantity,
        unit=request.unit,
        category_id=UUID(request.category_id) if request.category_id else None,
        expiration_date=date.fromisoformat(request.expiration_date) if request.expiration_date else None,
        low_stock_threshold=request.low_stock_threshold,
        notify_on_low_stock=request.notify_on_low_stock,
        barcode=request.barcode,
        household_id=household_id,
        created_at=now,
        updated_at=now,
    )
    db.add(item)
    await db.commit()
    await db.refresh(item)
    return _to_response(item)


@router.put("/{item_id}", response_model=PantryItemResponse, response_model_by_alias=True)
async def update_item(
    item_id: UUID,
    request: UpdatePantryItemRequest,
    user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    household_id = UUID(user["household_id"])
    item_uuid = item_id
    result = await db.execute(
        select(PantryItem).where(
            PantryItem.id == item_uuid,
            PantryItem.household_id == household_id,
        )
    )
    item = result.scalars().first()
    if item is None:
        return JSONResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content={"error": "Item not found"},
        )

    item.name = request.name
    item.quantity = request.quantity
    item.unit = request.unit
    item.category_id = UUID(request.category_id) if request.category_id else None
    item.expiration_date = date.fromisoformat(request.expiration_date) if request.expiration_date else None
    item.low_stock_threshold = request.low_stock_threshold
    item.notify_on_low_stock = request.notify_on_low_stock
    item.barcode = request.barcode
    item.updated_at = datetime.now(timezone.utc)

    await db.commit()
    await db.refresh(item)
    return _to_response(item)


@router.delete("/{item_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_item(
    item_id: UUID,
    user: dict = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    household_id = UUID(user["household_id"])
    item_uuid = item_id
    result = await db.execute(
        select(PantryItem).where(
            PantryItem.id == item_uuid,
            PantryItem.household_id == household_id,
        )
    )
    item = result.scalars().first()
    if item is None:
        return JSONResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content={"error": "Item not found"},
        )

    await db.delete(item)
    await db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)
