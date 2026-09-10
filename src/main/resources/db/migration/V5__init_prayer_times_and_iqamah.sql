-- ==============================================================================
-- Flyway Migration: V5 - Prayer Times Calculation Config & Iqamah Schedules
-- ==============================================================================

-- 1. Mosque Prayer Configuration Table
CREATE TABLE IF NOT EXISTS mosque_prayer_configs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mosque_id UUID NOT NULL UNIQUE REFERENCES mosques(id) ON DELETE CASCADE,
    calculation_method VARCHAR(50) NOT NULL DEFAULT 'MUSLIM_WORLD_LEAGUE',
    juristic_school VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
    time_zone VARCHAR(100) NOT NULL DEFAULT 'UTC',
    fajr_angle DOUBLE PRECISION,
    isha_angle DOUBLE PRECISION,
    high_latitude_rule VARCHAR(50) DEFAULT 'MIDDLE_OF_THE_NIGHT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_prayer_configs_mosque_id ON mosque_prayer_configs(mosque_id);

-- 2. Mosque Iqamah Schedule & Overrides Table
CREATE TABLE IF NOT EXISTS mosque_iqamah_schedules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mosque_id UUID NOT NULL UNIQUE REFERENCES mosques(id) ON DELETE CASCADE,
    fajr_type VARCHAR(50) NOT NULL DEFAULT 'OFFSET_AFTER_ADHAN',
    fajr_offset_minutes INT DEFAULT 20,
    fajr_fixed_time TIME,
    dhuhr_type VARCHAR(50) NOT NULL DEFAULT 'FIXED_TIME',
    dhuhr_offset_minutes INT DEFAULT 15,
    dhuhr_fixed_time TIME DEFAULT '13:30:00',
    asr_type VARCHAR(50) NOT NULL DEFAULT 'OFFSET_AFTER_ADHAN',
    asr_offset_minutes INT DEFAULT 15,
    asr_fixed_time TIME,
    maghrib_type VARCHAR(50) NOT NULL DEFAULT 'OFFSET_AFTER_ADHAN',
    maghrib_offset_minutes INT DEFAULT 10,
    maghrib_fixed_time TIME,
    isha_type VARCHAR(50) NOT NULL DEFAULT 'OFFSET_AFTER_ADHAN',
    isha_offset_minutes INT DEFAULT 15,
    isha_fixed_time TIME,
    jummah_1_time TIME DEFAULT '13:15:00',
    jummah_2_time TIME,
    jummah_khutbah_language VARCHAR(50) DEFAULT 'English',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_iqamah_schedules_mosque_id ON mosque_iqamah_schedules(mosque_id);
