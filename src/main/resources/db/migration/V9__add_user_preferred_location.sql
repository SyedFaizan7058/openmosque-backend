-- Flyway Migration: V9 - Add Preferred Location to Users Table
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS preferred_city VARCHAR(100),
ADD COLUMN IF NOT EXISTS preferred_country VARCHAR(100),
ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

CREATE INDEX IF NOT EXISTS idx_users_preferred_city ON users(preferred_city);