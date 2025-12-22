-- V1__create_billing_tables.sql -- force checksum mismatch

-- ENUMS
CREATE TYPE plan_type AS ENUM ('SINGLE_CHECK', 'SUBSCRIPTION', 'ADDON');
CREATE TYPE billing_period AS ENUM ('ONE_TIME', 'MONTHLY', 'YEARLY');
CREATE TYPE quota_type AS ENUM ('LIMITED', 'UNLIMITED');
CREATE TYPE subscription_status AS ENUM ('ACTIVE', 'EXPIRED', 'CANCELLED');
CREATE TYPE reservation_status AS ENUM ('RESERVED', 'COMMITTED', 'RELEASED');

-- TABLES
CREATE TABLE plan (
    id UUID PRIMARY KEY,
    code VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    type plan_type NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    billing_period billing_period NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE feature (
    code VARCHAR(255) PRIMARY KEY,
    description TEXT
);

CREATE TABLE plan_feature (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL REFERENCES plan(id),
    feature_code VARCHAR(255) NOT NULL REFERENCES feature(code),
    quota INTEGER,
    quota_type quota_type NOT NULL,
    UNIQUE (plan_id, feature_code)
);

CREATE TABLE subscription (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    org_id UUID,
    plan_id UUID NOT NULL REFERENCES plan(id),
    status subscription_status NOT NULL,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_subscription_user_id ON subscription(user_id);
CREATE INDEX idx_subscription_org_id ON subscription(org_id);
CREATE INDEX idx_subscription_status ON subscription(status);

CREATE TABLE usage (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES subscription(id),
    feature_code VARCHAR(255) NOT NULL REFERENCES feature(code),
    used_count INTEGER NOT NULL DEFAULT 0,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    UNIQUE (subscription_id, feature_code, period_start)
);

CREATE INDEX idx_usage_subscription_id_feature_code ON usage(subscription_id, feature_code);

CREATE TABLE usage_reservation (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES subscription(id),
    feature_code VARCHAR(255) NOT NULL REFERENCES feature(code),
    reserved_count INTEGER NOT NULL,
    status reservation_status NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_usage_reservation_status_expires_at ON usage_reservation(status, expires_at);
