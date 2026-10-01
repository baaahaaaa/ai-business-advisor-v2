from pydantic import (
    BaseModel,
    ConfigDict,
    Field
)


# ============================================================
# COMMON INSURANCE RISK INPUT
# ============================================================

class InsuranceRiskRequest(BaseModel):

    model_config = ConfigDict(
        extra="forbid"
    )

    Exposure: float = Field(
        gt=0,
        le=1
    )

    VehPower: int = Field(
        ge=1
    )

    VehAge: int = Field(
        ge=0
    )

    DrivAge: int = Field(
        ge=18
    )

    BonusMalus: float = Field(
        ge=0
    )

    Density: float = Field(
        ge=0
    )

    Area: str = Field(
        min_length=1
    )

    VehBrand: str = Field(
        min_length=1
    )

    VehGas: str = Field(
        min_length=1
    )

    Region: str = Field(
        min_length=1
    )


# ============================================================
# OCCURRENCE OUTPUT
# ============================================================

class ClaimOccurrenceResponse(BaseModel):

    claim_probability: float

    technical_threshold: float

    technical_risk_flag: bool


# ============================================================
# FREQUENCY OUTPUT
# ============================================================

class ClaimFrequencyResponse(BaseModel):

    predicted_frequency: float

    exposure: float

    expected_claim_count: float


# ============================================================
# FRAUD INPUT
# ============================================================

class FraudRequest(BaseModel):

    model_config = ConfigDict(
        extra="forbid"
    )

    Age: float | None = Field(
        default=None,
        ge=0
    )

    Deductible: float = Field(
        ge=0
    )

    WeekOfMonth: int = Field(
        ge=1,
        le=5
    )

    WeekOfMonthClaimed: int = Field(
        ge=1,
        le=5
    )

    DriverRating: int = Field(
        ge=1
    )

    Month: str

    DayOfWeek: str

    Make: str

    AccidentArea: str

    DayOfWeekClaimed: str | None = None

    MonthClaimed: str | None = None

    Sex: str

    MaritalStatus: str

    VehicleCategory: str

    VehiclePrice: str

    PastNumberOfClaims: str

    AgeOfVehicle: str

    AgeOfPolicyHolder: str

    AgentType: str

    NumberOfCars: str

    BasePolicy: str


# ============================================================
# FRAUD OUTPUT
# ============================================================

class FraudResponse(BaseModel):

    fraud_probability: float

    technical_threshold: float

    investigation_flag: bool