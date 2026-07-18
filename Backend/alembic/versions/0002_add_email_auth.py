"""Add EMAIL auth provider and password_hash column

Revision ID: 0002
Revises: 0001
Create Date: 2026-07-16

"""
from typing import Sequence, Union

import sqlalchemy as sa
from alembic import op

revision: str = "0002"
down_revision: Union[str, None] = "0001"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # ALTER TYPE ... ADD VALUE cannot run inside a transaction block in
    # PostgreSQL, so it needs Alembic's autocommit block.
    with op.get_context().autocommit_block():
        op.execute("ALTER TYPE auth_provider ADD VALUE IF NOT EXISTS 'EMAIL'")

    op.add_column("User", sa.Column("password_hash", sa.String(255), nullable=True))


def downgrade() -> None:
    op.drop_column("User", "password_hash")
    # Postgres has no direct "remove enum value" operation, so downgrading
    # the auth_provider enum itself back to NAVER/GOOGLE-only isn't supported.
