import uuid

from sqlalchemy import (
    Boolean,
    Column,
    Date,
    DateTime,
    ForeignKey,
    Integer,
    Numeric,
    String,
    Text,
    UniqueConstraint,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import DeclarativeBase
from sqlalchemy.sql import func


class Base(DeclarativeBase):
    pass


class Household(Base):
    __tablename__ = "households"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    name = Column(String(255), nullable=False)
    invite_code = Column(String(64), unique=True, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())


class User(Base):
    __tablename__ = "users"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    username = Column(String(255), unique=True, nullable=False)
    display_name = Column(String(255), nullable=False)
    password_hash = Column(String(255), nullable=False)
    household_id = Column(
        UUID(as_uuid=True),
        ForeignKey("households.id", ondelete="CASCADE"),
        nullable=False,
    )
    role = Column(String(16), nullable=False, default="OWNER")
    created_at = Column(DateTime(timezone=True), server_default=func.now())


class Category(Base):
    __tablename__ = "categories"
    __table_args__ = (UniqueConstraint("name", "household_id"),)

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    name = Column(String(255), nullable=False)
    description = Column(Text, nullable=True)
    color = Column(String(16), nullable=True)
    sort_order = Column(Integer, nullable=False, default=0)
    household_id = Column(
        UUID(as_uuid=True),
        ForeignKey("households.id", ondelete="CASCADE"),
        nullable=False,
    )


class PantryItem(Base):
    __tablename__ = "pantry_items"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    name = Column(String(255), nullable=False)
    quantity = Column(Numeric(10, 3), nullable=False, default=0)
    unit = Column(String(64), nullable=False)
    category_id = Column(
        UUID(as_uuid=True),
        ForeignKey("categories.id", ondelete="SET NULL"),
        nullable=True,
    )
    expiration_date = Column(Date, nullable=True)
    low_stock_threshold = Column(Numeric(10, 3), nullable=False, default=0)
    notify_on_low_stock = Column(Boolean, nullable=False, default=True)
    barcode = Column(String(128), nullable=True)
    household_id = Column(
        UUID(as_uuid=True),
        ForeignKey("households.id", ondelete="CASCADE"),
        nullable=False,
    )
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())
