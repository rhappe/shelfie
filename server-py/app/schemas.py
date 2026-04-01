from __future__ import annotations

import datetime
from typing import Optional

from pydantic import BaseModel, ConfigDict, field_validator
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

    @field_validator("email")
    @classmethod
    def email_must_not_be_blank(cls, v: str) -> str:
        if not v or not v.strip():
            raise ValueError("Email must not be blank")
        return v

    @field_validator("password")
    @classmethod
    def password_min_length(cls, v: str) -> str:
        if len(v) < 6:
            raise ValueError("Password must be at least 6 characters")
        return v

    @field_validator("display_name")
    @classmethod
    def display_name_must_not_be_blank(cls, v: str) -> str:
        if not v or not v.strip():
            raise ValueError("Display name must not be blank")
        return v


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
