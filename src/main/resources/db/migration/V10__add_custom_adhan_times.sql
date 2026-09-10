-- ==============================================================================
-- Flyway Migration: V10 - Custom Adhan Times for Mosque Iqamah Schedules
-- ==============================================================================

ALTER TABLE mosque_iqamah_schedules
    ADD COLUMN IF NOT EXISTS fajr_adhan_time TIME,
    ADD COLUMN IF NOT EXISTS dhuhr_adhan_time TIME,
    ADD COLUMN IF NOT EXISTS asr_adhan_time TIME,
    ADD COLUMN IF NOT EXISTS maghrib_adhan_time TIME,
    ADD COLUMN IF NOT EXISTS isha_adhan_time TIME;
