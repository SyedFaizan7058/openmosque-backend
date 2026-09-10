-- ==============================================================================
-- Flyway Migration: V3 - Add Submission ElementCollection Tables
-- ==============================================================================

CREATE TABLE IF NOT EXISTS submission_facility_codes (
    submission_id UUID NOT NULL REFERENCES mosque_submissions(id) ON DELETE CASCADE,
    facility_code VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS submission_image_urls (
    submission_id UUID NOT NULL REFERENCES mosque_submissions(id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sub_fac_codes_sub_id ON submission_facility_codes(submission_id);
CREATE INDEX IF NOT EXISTS idx_sub_img_urls_sub_id ON submission_image_urls(submission_id);
