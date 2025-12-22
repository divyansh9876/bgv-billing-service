-- V2__seed_plans_and_features.sql

-- Seed Features
INSERT INTO feature (code, description) VALUES
('PAN_VERIFY', 'PAN Card Verification'),
('AADHAAR_VERIFY', 'Aadhaar Card Verification'),
('DRIVING_LICENSE_VERIFY', 'Driving License Verification');

-- Seed Plans
-- Note: Using hardcoded UUIDs for deterministic seeding
-- Single Check Plan
INSERT INTO plan (id, code, name, type, price, currency, billing_period, is_active) VALUES
('a1b2c3d4-e5f6-7890-1234-567890abcdef', 'SINGLE_PAN_CHECK', 'Single PAN Check', 'SINGLE_CHECK', 50.00, 'INR', 'ONE_TIME', true);

-- Subscription Plans
INSERT INTO plan (id, code, name, type, price, currency, billing_period, is_active) VALUES
('b2c3d4e5-f6a7-8901-2345-67890abcdef1', 'STARTER', 'Starter Plan', 'SUBSCRIPTION', 1000.00, 'INR', 'MONTHLY', true),
('c3d4e5f6-a7b8-9012-3456-7890abcdef20', 'PRO', 'Pro Plan', 'SUBSCRIPTION', 5000.00, 'INR', 'MONTHLY', true),
('d4e5f6a7-b8c9-0123-4567-890abcdef300', 'BUSINESS', 'Business Plan', 'SUBSCRIPTION', 10000.00, 'INR', 'YEARLY', true),
('e5f6a7b8-c9d0-1234-5678-90abcdef4000', 'UNLIMITED', 'Unlimited Plan', 'SUBSCRIPTION', 50000.00, 'INR', 'YEARLY', true);

-- Seed Plan Features
-- Single PAN Check
INSERT INTO plan_feature (id, plan_id, feature_code, quota, quota_type) VALUES
(gen_random_uuid(), 'a1b2c3d4-e5f6-7890-1234-567890abcdef', 'PAN_VERIFY', 1, 'LIMITED');

-- Starter Plan
INSERT INTO plan_feature (id, plan_id, feature_code, quota, quota_type) VALUES
(gen_random_uuid(), 'b2c3d4e5-f6a7-8901-2345-67890abcdef1', 'PAN_VERIFY', 25, 'LIMITED'),
(gen_random_uuid(), 'b2c3d4e5-f6a7-8901-2345-67890abcdef1', 'AADHAAR_VERIFY', 10, 'LIMITED');

-- Pro Plan
INSERT INTO plan_feature (id, plan_id, feature_code, quota, quota_type) VALUES
(gen_random_uuid(), 'c3d4e5f6-a7b8-9012-3456-7890abcdef20', 'PAN_VERIFY', 100, 'LIMITED'),
(gen_random_uuid(), 'c3d4e5f6-a7b8-9012-3456-7890abcdef20', 'AADHAAR_VERIFY', 50, 'LIMITED'),
(gen_random_uuid(), 'c3d4e5f6-a7b8-9012-3456-7890abcdef20', 'DRIVING_LICENSE_VERIFY', 25, 'LIMITED');

-- Business Plan
INSERT INTO plan_feature (id, plan_id, feature_code, quota, quota_type) VALUES
(gen_random_uuid(), 'd4e5f6a7-b8c9-0123-4567-890abcdef300', 'PAN_VERIFY', 500, 'LIMITED'),
(gen_random_uuid(), 'd4e5f6a7-b8c9-0123-4567-890abcdef300', 'AADHAAR_VERIFY', 250, 'LIMITED'),
(gen_random_uuid(), 'd4e5f6a7-b8c9-0123-4567-890abcdef300', 'DRIVING_LICENSE_VERIFY', 100, 'LIMITED');

-- Unlimited Plan
INSERT INTO plan_feature (id, plan_id, feature_code, quota, quota_type) VALUES
(gen_random_uuid(), 'e5f6a7b8-c9d0-1234-5678-90abcdef4000', 'PAN_VERIFY', -1, 'UNLIMITED'),
(gen_random_uuid(), 'e5f6a7b8-c9d0-1234-5678-90abcdef4000', 'AADHAAR_VERIFY', -1, 'UNLIMITED'),
(gen_random_uuid(), 'e5f6a7b8-c9d0-1234-5678-90abcdef4000', 'DRIVING_LICENSE_VERIFY', -1, 'UNLIMITED');