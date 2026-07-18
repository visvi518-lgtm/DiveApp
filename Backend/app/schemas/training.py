from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, Field, model_validator


class TrainingRecordCreateRequest(BaseModel):
    total_sets: int = Field(ge=5, le=20)
    completed_sets: int = Field(ge=0)
    is_completed: bool
    rest_time_seconds: int = Field(gt=0)
    hold_time_seconds: int = Field(gt=0)
    rest_interval_seconds: int
    hold_interval_seconds: int

    @model_validator(mode="after")
    def check_completed_sets_within_total(self) -> "TrainingRecordCreateRequest":
        if self.completed_sets > self.total_sets:
            raise ValueError("completed_sets must not exceed total_sets")
        return self


class TrainingRecordResponse(BaseModel):
    id: UUID
    total_sets: int
    completed_sets: int
    is_completed: bool
    rest_time_seconds: int
    hold_time_seconds: int
    rest_interval_seconds: int
    hold_interval_seconds: int
    completed_at: datetime

    model_config = {"from_attributes": True}


class TrainingStatisticsResponse(BaseModel):
    total_training_count: int
    completion_rate: float
    average_completed_sets: float
    last_training_at: datetime | None
