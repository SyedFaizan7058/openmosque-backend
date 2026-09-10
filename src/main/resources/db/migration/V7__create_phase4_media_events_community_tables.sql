-- ==============================================================================
-- Flyway Migration: V7 - Phase 4: Media, Events, Khutbahs, Reviews, Q&A, and Flags
-- ==============================================================================

-- 1. Mosque Events Table
CREATE TABLE IF NOT EXISTS mosque_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mosque_id UUID NOT NULL REFERENCES mosques(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    event_type VARCHAR(50) NOT NULL,
    audience VARCHAR(50) NOT NULL DEFAULT 'ALL',
    start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    location_details VARCHAR(255),
    speaker_name VARCHAR(150),
    banner_image_url VARCHAR(500),
    registration_url VARCHAR(500),
    is_cancelled BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_mosque_events_mosque_id ON mosque_events(mosque_id);
CREATE INDEX IF NOT EXISTS idx_mosque_events_start_date ON mosque_events(start_date_time);
CREATE INDEX IF NOT EXISTS idx_mosque_events_type ON mosque_events(event_type);

-- 2. Mosque Friday Khutbahs Table
CREATE TABLE IF NOT EXISTS mosque_khutbahs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mosque_id UUID NOT NULL REFERENCES mosques(id) ON DELETE CASCADE,
    khutbah_date DATE NOT NULL,
    topic VARCHAR(255) NOT NULL,
    khatib_name VARCHAR(150) NOT NULL,
    batch_number INT NOT NULL DEFAULT 1,
    khutbah_time TIME NOT NULL,
    adhaan_time TIME,
    iqamah_time TIME,
    language VARCHAR(100) DEFAULT 'English',
    stream_url VARCHAR(500),
    recording_url VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_mosque_khutbahs_mosque_id ON mosque_khutbahs(mosque_id);
CREATE INDEX IF NOT EXISTS idx_mosque_khutbahs_date ON mosque_khutbahs(khutbah_date);

-- 3. Mosque Reviews Table
CREATE TABLE IF NOT EXISTS mosque_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mosque_id UUID NOT NULL REFERENCES mosques(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating_overall INT NOT NULL CHECK (rating_overall BETWEEN 1 AND 5),
    rating_cleanliness INT CHECK (rating_cleanliness BETWEEN 1 AND 5),
    rating_facilities INT CHECK (rating_facilities BETWEEN 1 AND 5),
    rating_womens_area INT CHECK (rating_womens_area BETWEEN 1 AND 5),
    rating_parking INT CHECK (rating_parking BETWEEN 1 AND 5),
    review_text TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_mosque_reviews_user UNIQUE (mosque_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_mosque_reviews_mosque_id ON mosque_reviews(mosque_id);
CREATE INDEX IF NOT EXISTS idx_mosque_reviews_status ON mosque_reviews(status);

-- 4. Mosque Community Questions Table
CREATE TABLE IF NOT EXISTS mosque_questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mosque_id UUID NOT NULL REFERENCES mosques(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_mosque_questions_mosque_id ON mosque_questions(mosque_id);
CREATE INDEX IF NOT EXISTS idx_mosque_questions_status ON mosque_questions(status);

-- 5. Mosque Community Answers Table
CREATE TABLE IF NOT EXISTS mosque_answers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question_id UUID NOT NULL REFERENCES mosque_questions(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    answer_text TEXT NOT NULL,
    is_official_mosque_admin BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_mosque_answers_question_id ON mosque_answers(question_id);
CREATE INDEX IF NOT EXISTS idx_mosque_answers_status ON mosque_answers(status);

-- 6. Community Content Moderation Flags Table
CREATE TABLE IF NOT EXISTS community_content_flags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    target_type VARCHAR(50) NOT NULL, -- REVIEW, QUESTION, ANSWER
    target_id UUID NOT NULL,
    reporter_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, RESOLVED, DISMISSED
    reviewer_id UUID REFERENCES users(id) ON DELETE SET NULL,
    reviewer_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_community_flags_status ON community_content_flags(status);
CREATE INDEX IF NOT EXISTS idx_community_flags_target ON community_content_flags(target_type, target_id);
