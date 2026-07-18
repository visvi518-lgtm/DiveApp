"""Initial schema

Creates all MVP tables defined in Docs/11_DatabaseSchema.md.

Revision ID: 0001
Revises:
Create Date: 2026-07-14

"""
from typing import Sequence, Union

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

revision: str = "0001"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def _uuid_pk() -> sa.Column:
    return sa.Column(
        "id",
        postgresql.UUID(as_uuid=True),
        primary_key=True,
        server_default=sa.text("gen_random_uuid()"),
    )


def _timestamps() -> list[sa.Column]:
    return [
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True),
    ]


def upgrade() -> None:
    op.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto")

    auth_provider = postgresql.ENUM("NAVER", "GOOGLE", name="auth_provider")
    user_role = postgresql.ENUM("USER", "ADMIN", name="user_role")
    account_status = postgresql.ENUM("ACTIVE", "DORMANT", "SUSPENDED", "DELETED", name="account_status")
    dive_type = postgresql.ENUM("FREEDIVING", "SCUBA", name="dive_type")
    certification_organization = postgresql.ENUM(
        "AIDA", "PADI", "SSI", "RAID", "SDI", "NAUI", "CMAS", name="certification_organization"
    )
    banner_type = postgresql.ENUM("NOTICE", "EVENT", "PROMOTION", "INFORMATION", name="banner_type")

    # Each enum is used in exactly one table below, so create_table's own DDL
    # emission creates the backing CREATE TYPE automatically — no separate
    # pre-creation step is needed (and doing so causes a duplicate-type error).

    op.create_table(
        "User",
        _uuid_pk(),
        sa.Column("provider", auth_provider, nullable=False),
        sa.Column("provider_user_id", sa.String(255), nullable=False),
        sa.Column("email", sa.String(255), nullable=False),
        sa.Column("role", user_role, nullable=False, server_default="USER"),
        sa.Column("account_status", account_status, nullable=False, server_default="ACTIVE"),
        sa.Column("last_login_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("email_verified", sa.Boolean(), nullable=False, server_default=sa.false()),
        *_timestamps(),
        sa.UniqueConstraint("email", name="uq_user_email"),
        sa.UniqueConstraint("provider", "provider_user_id", name="uq_user_provider_identity"),
    )
    op.create_index("ix_user_email", "User", ["email"])
    op.create_index("ix_user_provider_identity", "User", ["provider", "provider_user_id"])
    op.create_index("ix_user_account_status", "User", ["account_status"])

    op.create_table(
        "UserProfile",
        _uuid_pk(),
        sa.Column("user_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("User.id"), nullable=False),
        sa.Column("nickname", sa.String(30), nullable=False),
        sa.Column("profile_image_url", sa.Text(), nullable=True),
        sa.Column("phone_number", sa.String(20), nullable=True),
        sa.Column("bio", sa.Text(), nullable=True),
        *_timestamps(),
        sa.UniqueConstraint("user_id", name="uq_userprofile_user_id"),
        sa.UniqueConstraint("nickname", name="uq_userprofile_nickname"),
    )
    op.create_index("ix_userprofile_user_id", "UserProfile", ["user_id"])
    op.create_index("ix_userprofile_nickname", "UserProfile", ["nickname"])

    op.create_table(
        "DiveLocation",
        _uuid_pk(),
        sa.Column("name", sa.String(100), nullable=False),
        sa.Column("address", sa.String(255), nullable=True),
        sa.Column("latitude", sa.Numeric(10, 7), nullable=False),
        sa.Column("longitude", sa.Numeric(10, 7), nullable=False),
        sa.Column("naver_place_id", sa.String(100), nullable=True),
        sa.Column("country", sa.String(100), nullable=True),
        sa.Column("city", sa.String(100), nullable=True),
        *_timestamps(),
        sa.UniqueConstraint("name", "city", "country", name="uq_divelocation_name_city_country"),
        sa.CheckConstraint("latitude BETWEEN -90 AND 90", name="ck_divelocation_latitude_range"),
        sa.CheckConstraint("longitude BETWEEN -180 AND 180", name="ck_divelocation_longitude_range"),
    )
    op.create_index("ix_divelocation_name", "DiveLocation", ["name"])
    op.create_index("ix_divelocation_city", "DiveLocation", ["city"])
    op.create_index("ix_divelocation_lat_lng", "DiveLocation", ["latitude", "longitude"])

    op.create_table(
        "DiveLog",
        _uuid_pk(),
        sa.Column("user_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("User.id"), nullable=False),
        sa.Column("location_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("DiveLocation.id"), nullable=False),
        sa.Column("dive_type", dive_type, nullable=False),
        sa.Column("dive_date", sa.Date(), nullable=False),
        sa.Column("latitude", sa.Numeric(10, 7), nullable=True),
        sa.Column("longitude", sa.Numeric(10, 7), nullable=True),
        sa.Column("memo", sa.Text(), nullable=True),
        *_timestamps(),
        sa.CheckConstraint("latitude IS NULL OR latitude BETWEEN -90 AND 90", name="ck_divelog_latitude_range"),
        sa.CheckConstraint(
            "longitude IS NULL OR longitude BETWEEN -180 AND 180", name="ck_divelog_longitude_range"
        ),
    )
    op.create_index("ix_divelog_user_id", "DiveLog", ["user_id"])
    op.create_index("ix_divelog_location_id", "DiveLog", ["location_id"])
    op.create_index("ix_divelog_dive_date", "DiveLog", ["dive_date"])
    op.create_index("ix_divelog_dive_type", "DiveLog", ["dive_type"])
    op.create_index("ix_divelog_user_id_dive_date", "DiveLog", ["user_id", "dive_date"])

    op.create_table(
        "FreedivingLog",
        _uuid_pk(),
        sa.Column("dive_log_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("DiveLog.id"), nullable=False),
        sa.Column("max_depth", sa.Numeric(5, 2), nullable=False),
        sa.Column("dive_time_seconds", sa.Integer(), nullable=False),
        *_timestamps(),
        sa.UniqueConstraint("dive_log_id", name="uq_freedivinglog_dive_log_id"),
        sa.CheckConstraint("max_depth >= 0", name="ck_freedivinglog_max_depth_nonnegative"),
        sa.CheckConstraint("dive_time_seconds >= 0", name="ck_freedivinglog_dive_time_nonnegative"),
    )
    op.create_index("ix_freedivinglog_dive_log_id", "FreedivingLog", ["dive_log_id"])
    op.create_index("ix_freedivinglog_max_depth", "FreedivingLog", ["max_depth"])

    op.create_table(
        "ScubaLog",
        _uuid_pk(),
        sa.Column("dive_log_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("DiveLog.id"), nullable=False),
        sa.Column("max_depth", sa.Numeric(5, 2), nullable=False),
        sa.Column("dive_time_seconds", sa.Integer(), nullable=False),
        sa.Column("tank_pressure_start", sa.Integer(), nullable=False),
        sa.Column("tank_pressure_end", sa.Integer(), nullable=False),
        *_timestamps(),
        sa.UniqueConstraint("dive_log_id", name="uq_scubalog_dive_log_id"),
        sa.CheckConstraint("max_depth >= 0", name="ck_scubalog_max_depth_nonnegative"),
        sa.CheckConstraint("dive_time_seconds >= 0", name="ck_scubalog_dive_time_nonnegative"),
        sa.CheckConstraint("tank_pressure_start >= 0", name="ck_scubalog_tank_start_nonnegative"),
        sa.CheckConstraint("tank_pressure_end >= 0", name="ck_scubalog_tank_end_nonnegative"),
        sa.CheckConstraint("tank_pressure_start >= tank_pressure_end", name="ck_scubalog_tank_start_gte_end"),
    )
    op.create_index("ix_scubalog_dive_log_id", "ScubaLog", ["dive_log_id"])
    op.create_index("ix_scubalog_max_depth", "ScubaLog", ["max_depth"])

    op.create_table(
        "DivePhoto",
        _uuid_pk(),
        sa.Column("dive_log_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("DiveLog.id"), nullable=False),
        sa.Column("image_url", sa.Text(), nullable=False),
        sa.Column("display_order", sa.Integer(), nullable=False, server_default="1"),
        *_timestamps(),
        sa.UniqueConstraint("dive_log_id", "display_order", name="uq_divephoto_log_display_order"),
        sa.CheckConstraint("display_order >= 1", name="ck_divephoto_display_order_positive"),
    )
    op.create_index("ix_divephoto_dive_log_id", "DivePhoto", ["dive_log_id"])
    op.create_index("ix_divephoto_display_order", "DivePhoto", ["display_order"])

    op.create_table(
        "TrainingRecord",
        _uuid_pk(),
        sa.Column("user_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("User.id"), nullable=False),
        sa.Column("total_sets", sa.Integer(), nullable=False),
        sa.Column("completed_sets", sa.Integer(), nullable=False, server_default="0"),
        sa.Column("is_completed", sa.Boolean(), nullable=False, server_default=sa.false()),
        sa.Column("rest_time_seconds", sa.Integer(), nullable=False),
        sa.Column("hold_time_seconds", sa.Integer(), nullable=False),
        sa.Column("rest_interval_seconds", sa.Integer(), nullable=False),
        sa.Column("hold_interval_seconds", sa.Integer(), nullable=False),
        sa.Column("completed_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
        *_timestamps(),
        sa.CheckConstraint("total_sets >= 5 AND total_sets <= 20", name="ck_trainingrecord_total_sets_range"),
        sa.CheckConstraint(
            "completed_sets >= 0 AND completed_sets <= total_sets", name="ck_trainingrecord_completed_sets_range"
        ),
        sa.CheckConstraint("rest_time_seconds > 0", name="ck_trainingrecord_rest_time_positive"),
        sa.CheckConstraint("hold_time_seconds > 0", name="ck_trainingrecord_hold_time_positive"),
    )
    op.create_index("ix_trainingrecord_user_id", "TrainingRecord", ["user_id"])
    op.create_index("ix_trainingrecord_completed_at", "TrainingRecord", ["completed_at"])
    op.create_index("ix_trainingrecord_is_completed", "TrainingRecord", ["is_completed"])

    op.create_table(
        "Certificate",
        _uuid_pk(),
        sa.Column("user_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("User.id"), nullable=False),
        sa.Column("organization", certification_organization, nullable=False),
        sa.Column("certification_level", sa.String(100), nullable=False),
        sa.Column("certification_number", sa.String(100), nullable=True),
        sa.Column("issue_date", sa.Date(), nullable=True),
        sa.Column("expiration_date", sa.Date(), nullable=True),
        sa.Column("instructor", sa.String(100), nullable=True),
        sa.Column("dive_center", sa.String(100), nullable=True),
        sa.Column("certificate_image_url", sa.Text(), nullable=True),
        sa.Column("memo", sa.Text(), nullable=True),
        *_timestamps(),
        sa.CheckConstraint(
            "expiration_date IS NULL OR issue_date IS NULL OR expiration_date >= issue_date",
            name="ck_certificate_expiration_after_issue",
        ),
    )
    op.create_index("ix_certificate_user_id", "Certificate", ["user_id"])
    op.create_index("ix_certificate_organization", "Certificate", ["organization"])
    op.create_index("ix_certificate_certification_level", "Certificate", ["certification_level"])

    op.create_table(
        "CommunityPost",
        _uuid_pk(),
        sa.Column("user_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("User.id"), nullable=False),
        sa.Column("title", sa.String(200), nullable=False),
        sa.Column("content", sa.Text(), nullable=False),
        sa.Column("view_count", sa.Integer(), nullable=False, server_default="0"),
        sa.Column("like_count", sa.Integer(), nullable=False, server_default="0"),
        sa.Column("comment_count", sa.Integer(), nullable=False, server_default="0"),
        sa.Column("is_pinned", sa.Boolean(), nullable=False, server_default=sa.false()),
        *_timestamps(),
        sa.CheckConstraint("view_count >= 0", name="ck_communitypost_view_count_nonnegative"),
        sa.CheckConstraint("like_count >= 0", name="ck_communitypost_like_count_nonnegative"),
        sa.CheckConstraint("comment_count >= 0", name="ck_communitypost_comment_count_nonnegative"),
    )
    op.create_index("ix_communitypost_user_id", "CommunityPost", ["user_id"])
    op.create_index("ix_communitypost_is_pinned", "CommunityPost", ["is_pinned"])
    op.create_index("ix_communitypost_created_at", "CommunityPost", ["created_at"])
    op.create_index("ix_communitypost_view_count", "CommunityPost", ["view_count"])

    op.create_table(
        "CommunityComment",
        _uuid_pk(),
        sa.Column("post_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("CommunityPost.id"), nullable=False),
        sa.Column("user_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("User.id"), nullable=False),
        sa.Column("content", sa.Text(), nullable=False),
        *_timestamps(),
    )
    op.create_index("ix_communitycomment_post_id", "CommunityComment", ["post_id"])
    op.create_index("ix_communitycomment_created_at", "CommunityComment", ["created_at"])

    op.create_table(
        "InformationArticle",
        _uuid_pk(),
        sa.Column("title", sa.String(200), nullable=False),
        sa.Column("content", sa.Text(), nullable=False),
        sa.Column("thumbnail_image_url", sa.Text(), nullable=True),
        sa.Column("view_count", sa.Integer(), nullable=False, server_default="0"),
        sa.Column("is_published", sa.Boolean(), nullable=False, server_default=sa.false()),
        sa.Column("published_at", sa.DateTime(timezone=True), nullable=True),
        *_timestamps(),
        sa.CheckConstraint("view_count >= 0", name="ck_informationarticle_view_count_nonnegative"),
    )
    op.create_index("ix_informationarticle_is_published", "InformationArticle", ["is_published"])
    op.create_index("ix_informationarticle_published_at", "InformationArticle", ["published_at"])
    op.create_index("ix_informationarticle_created_at", "InformationArticle", ["created_at"])

    op.create_table(
        "Banner",
        _uuid_pk(),
        sa.Column("title", sa.String(100), nullable=False),
        sa.Column("image_url", sa.Text(), nullable=False),
        sa.Column("banner_type", banner_type, nullable=False),
        sa.Column("target_url", sa.Text(), nullable=True),
        sa.Column("display_order", sa.Integer(), nullable=False, server_default="1"),
        sa.Column("is_active", sa.Boolean(), nullable=False, server_default=sa.true()),
        sa.Column("start_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("end_at", sa.DateTime(timezone=True), nullable=True),
        *_timestamps(),
        sa.CheckConstraint("display_order >= 1", name="ck_banner_display_order_positive"),
    )
    op.create_index("ix_banner_is_active", "Banner", ["is_active"])
    op.create_index("ix_banner_display_order", "Banner", ["display_order"])
    op.create_index("ix_banner_start_at", "Banner", ["start_at"])
    op.create_index("ix_banner_end_at", "Banner", ["end_at"])

    op.create_table(
        "AdminLog",
        _uuid_pk(),
        sa.Column("admin_user_id", postgresql.UUID(as_uuid=True), sa.ForeignKey("User.id"), nullable=False),
        sa.Column("action", sa.String(100), nullable=False),
        sa.Column("target_type", sa.String(100), nullable=False),
        sa.Column("target_id", postgresql.UUID(as_uuid=True), nullable=True),
        sa.Column("description", sa.Text(), nullable=True),
        sa.Column("ip_address", sa.String(45), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.text("now()"), nullable=False),
    )
    op.create_index("ix_adminlog_admin_user_id", "AdminLog", ["admin_user_id"])
    op.create_index("ix_adminlog_action", "AdminLog", ["action"])
    op.create_index("ix_adminlog_target_type", "AdminLog", ["target_type"])
    op.create_index("ix_adminlog_created_at", "AdminLog", ["created_at"])


def downgrade() -> None:
    op.drop_table("AdminLog")
    op.drop_table("Banner")
    op.drop_table("InformationArticle")
    op.drop_table("CommunityComment")
    op.drop_table("CommunityPost")
    op.drop_table("Certificate")
    op.drop_table("TrainingRecord")
    op.drop_table("DivePhoto")
    op.drop_table("ScubaLog")
    op.drop_table("FreedivingLog")
    op.drop_table("DiveLog")
    op.drop_table("DiveLocation")
    op.drop_table("UserProfile")
    op.drop_table("User")

    bind = op.get_bind()
    for enum_name in (
        "banner_type",
        "certification_organization",
        "dive_type",
        "account_status",
        "user_role",
        "auth_provider",
    ):
        postgresql.ENUM(name=enum_name).drop(bind, checkfirst=True)
