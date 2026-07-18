import enum


class AccountStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    DORMANT = "DORMANT"
    SUSPENDED = "SUSPENDED"
    DELETED = "DELETED"


class AuthProvider(str, enum.Enum):
    NAVER = "NAVER"
    GOOGLE = "GOOGLE"
    EMAIL = "EMAIL"


class UserRole(str, enum.Enum):
    USER = "USER"
    ADMIN = "ADMIN"


class DiveType(str, enum.Enum):
    FREEDIVING = "FREEDIVING"
    SCUBA = "SCUBA"


class CertificationOrganization(str, enum.Enum):
    AIDA = "AIDA"
    PADI = "PADI"
    SSI = "SSI"
    RAID = "RAID"
    SDI = "SDI"
    NAUI = "NAUI"
    CMAS = "CMAS"


class BannerType(str, enum.Enum):
    NOTICE = "NOTICE"
    EVENT = "EVENT"
    PROMOTION = "PROMOTION"
    INFORMATION = "INFORMATION"
