-- ==============================================================================
-- Flyway Migration: V2 - Mosques, Facilities, Spatial Index & Moderation Queue
-- ==============================================================================

-- 1. Standard Islamic Facilities / Amenities Table
CREATE TABLE IF NOT EXISTS facilities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_name VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Seed Predefined Standard Islamic Facilities
INSERT INTO facilities (code, name, description, icon_name) VALUES
('WUDU_AREA', 'Wudu Area', 'Dedicated ablution facilities for men and women', 'water-drop'),
('WOMENS_SECTION', 'Women''s Prayer Section', 'Dedicated women prayer hall with separate entrance and audio', 'woman'),
('WHEELCHAIR_ACCESSIBILITY', 'Wheelchair Accessible', 'Ramps, elevators, and accessible prayer space', 'wheelchair-pickup'),
('PARKING', 'On-site Parking', 'Dedicated parking lot or marked street parking', 'local-parking'),
('JANAZAH_SERVICES', 'Janazah Services', 'Facilities for funeral prayers and wash preparation', 'church'),
('LIBRARY', 'Islamic Library', 'Collection of Islamic books, Quran copies, and study spaces', 'menu-book'),
('AIR_CONDITIONING', 'Air Conditioning & Heating', 'Climate-controlled prayer hall for summer/winter', 'ac-unit'),
('DAILY_HALAQAH', 'Daily Halaqah & Lectures', 'Regular Quran circles and Islamic education programs', 'school')
ON CONFLICT (code) DO NOTHING;

-- 2. Mosques Table (with PostGIS 4326 Point geometry)
CREATE TABLE IF NOT EXISTS mosques (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(250) NOT NULL UNIQUE,
    description TEXT,
    address VARCHAR(300) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    location GEOMETRY(Point, 4326) NOT NULL,
    contact_phone VARCHAR(50),
    contact_email VARCHAR(255),
    website_url VARCHAR(500),
    live_stream_url VARCHAR(500),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- PostGIS Spatial Index for ultra-fast nearby radius queries (ST_DWithin)
CREATE INDEX IF NOT EXISTS idx_mosques_location ON mosques USING GIST(location);
CREATE INDEX IF NOT EXISTS idx_mosques_slug ON mosques(slug);
CREATE INDEX IF NOT EXISTS idx_mosques_city ON mosques(city);
CREATE INDEX IF NOT EXISTS idx_mosques_country ON mosques(country);
CREATE INDEX IF NOT EXISTS idx_mosques_status ON mosques(status);

-- 3. Mosque Facilities Join Table
CREATE TABLE IF NOT EXISTS mosque_facilities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mosque_id UUID NOT NULL REFERENCES mosques(id) ON DELETE CASCADE,
    facility_id UUID NOT NULL REFERENCES facilities(id) ON DELETE CASCADE,
    custom_details VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_mosque_facility UNIQUE (mosque_id, facility_id)
);

CREATE INDEX IF NOT EXISTS idx_mosque_facilities_mosque_id ON mosque_facilities(mosque_id);
CREATE INDEX IF NOT EXISTS idx_mosque_facilities_facility_id ON mosque_facilities(facility_id);

-- 4. Mosque Images Gallery Table
CREATE TABLE IF NOT EXISTS mosque_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mosque_id UUID NOT NULL REFERENCES mosques(id) ON DELETE CASCADE,
    image_url VARCHAR(1000) NOT NULL,
    caption VARCHAR(255),
    is_cover BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_mosque_images_mosque_id ON mosque_images(mosque_id);

-- 5. Crowdsourced Mosque Submissions Queue
CREATE TABLE IF NOT EXISTS mosque_submissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    submitter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_mosque_id UUID REFERENCES mosques(id) ON DELETE SET NULL,
    submission_type VARCHAR(50) NOT NULL DEFAULT 'NEW_MOSQUE', -- 'NEW_MOSQUE' or 'EDIT_SUGGESTION'
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',            -- 'PENDING', 'APPROVED', 'REJECTED'
    name VARCHAR(200) NOT NULL,
    description TEXT,
    address VARCHAR(300) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    contact_phone VARCHAR(50),
    contact_email VARCHAR(255),
    website_url VARCHAR(500),
    live_stream_url VARCHAR(500),
    facility_codes TEXT[],                                     -- Array of facility codes e.g. {'PARKING', 'WUDU_AREA'}
    image_urls TEXT[],                                         -- Array of image URLs
    reviewer_id UUID REFERENCES users(id) ON DELETE SET NULL,
    review_comments TEXT,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_mosque_submissions_status ON mosque_submissions(status);
CREATE INDEX IF NOT EXISTS idx_mosque_submissions_submitter_id ON mosque_submissions(submitter_id);
CREATE INDEX IF NOT EXISTS idx_mosque_submissions_type ON mosque_submissions(submission_type);

-- 6. Moderation Audit Log Table
CREATE TABLE IF NOT EXISTS moderation_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    submission_id UUID NOT NULL REFERENCES mosque_submissions(id) ON DELETE CASCADE,
    moderator_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,                               -- 'APPROVED', 'REJECTED', 'COMMENTED'
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_moderation_logs_submission_id ON moderation_logs(submission_id);
CREATE INDEX IF NOT EXISTS idx_moderation_logs_moderator_id ON moderation_logs(moderator_id);
