from __future__ import annotations

import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict
from pydantic.alias_generators import to_camel


class CamelModel(BaseModel):
    model_config = ConfigDict(
        populate_by_name=True,
        alias_generator=to_camel,
    )


# ── Auth ─────────────────────────────────────────────────────────────────

class RegisterRequest(CamelModel):
    email: str
    password: str
    display_name: str


class LoginRequest(CamelModel):
    email: str
    password: str


class AuthResponse(CamelModel):
    token: str
    user_id: str
    email: str
    display_name: str
    household_id: str


# ── Categories ───────────────────────────────────────────────────────────

class CreateCategoryRequest(CamelModel):
    name: str
    description: Optional[str] = None
    color: Optional[str] = None
    sort_order: int = 0


class UpdateCategoryRequest(CamelModel):
    name: str
    description: Optional[str] = None
    color: Optional[str] = None
    sort_order: int = 0


class CategoryResponse(CamelModel):
    id: str
    name: str
    description: Optional[str] = None
    color: Optional[str] = None
    sort_order: int = 0
    household_id: str


# ── Pantry Items ─────────────────────────────────────────────────────────

class CreatePantryItemRequest(CamelModel):
    name: str
    quantity: float
    unit: str
    category_id: Optional[str] = None
    expiration_date: Optional[str] = None
    low_stock_threshold: float = 0.0
    notify_on_low_stock: bool = True
    barcode: Optional[str] = None


class UpdatePantryItemRequest(CamelModel):
    name: str
    quantity: float
    unit: str
    category_id: Optional[str] = None
    expiration_date: Optional[str] = None
    low_stock_threshold: float = 0.0
    notify_on_low_stock: bool = True
    barcode: Optional[str] = None


class PantryItemResponse(CamelModel):
    id: str
    name: str
    quantity: float
    unit: str
    category_id: Optional[str] = None
    expiration_date: Optional[str] = None
    low_stock_threshold: float = 0.0
    notify_on_low_stock: bool = True
    barcode: Optional[str] = None
    household_id: str
    created_at: str
    updated_at: str


# ── Errors ───────────────────────────────────────────────────────────────

class ErrorResponse(BaseModel):
    error: str
