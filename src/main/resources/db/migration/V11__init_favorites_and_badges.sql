-- Flyway Migration V11: User Favorite Mosques & Contributor Badges System

-- Ensure pgcrypto extension is available for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. User Favorite Mosques Table (Hard Delete by design to simplify unique constraints)
CREATE TABLE IF NOT EXISTS user_favorite_mosques (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    mosque_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_favorite_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorite_mosque FOREIGN KEY (mosque_id) REFERENCES mosques(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_favorite_mosque UNIQUE(user_id, mosque_id)
);

CREATE INDEX IF NOT EXISTS idx_favorite_user_id ON user_favorite_mosques(user_id);
CREATE INDEX IF NOT EXISTS idx_favorite_mosque_id ON user_favorite_mosques(mosque_id);

-- 2. Badges Catalog Table
CREATE TABLE IF NOT EXISTS badges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    icon_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    threshold_points INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_badges_code ON badges(code);
CREATE INDEX IF NOT EXISTS idx_badges_category ON badges(category);

-- 3. User Badges Earned Table (Protected with UNIQUE constraint against duplicate awards)
CREATE TABLE IF NOT EXISTS user_badges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    badge_id UUID NOT NULL,
    earned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_badges_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_badges_badge FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_badge UNIQUE(user_id, badge_id)
);

CREATE INDEX IF NOT EXISTS idx_user_badges_user_id ON user_badges(user_id);
CREATE INDEX IF NOT EXISTS idx_user_badges_badge_id ON user_badges(badge_id);

-- 4. Seed Standard Badges Catalog
INSERT INTO badges (id, code, name, description, icon_name, category, threshold_points, is_active, created_at)
VALUES
    (gen_random_uuid(), 'PIONEER', 'Pioneer', 'Awarded when your first submitted mosque is verified and approved.', 'compass', 'SUBMISSION', 0, TRUE, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'MOSQUE_EXPLORER', 'Mosque Explorer', 'Awarded when you bookmark 5 or more community mosques.', 'star', 'LOYALTY', 0, TRUE, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'CENTURION_CONTRIBUTOR', 'Centurion Contributor', 'Awarded upon reaching 100 community contribution points.', 'award', 'POINTS', 100, TRUE, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'COMMUNITY_PILLAR', 'Community Pillar', 'Awarded upon reaching 500 community contribution points.', 'shield-check', 'POINTS', 500, TRUE, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'VERIFIED_IMAM', 'Verified Mosque Administrator', 'Awarded when your official mosque claim and administration is verified.', 'check-circle', 'VERIFICATION', 0, TRUE, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'DEVOTED_PATRON', 'Devoted Patron', 'Awarded when you bookmark your first favorite mosque for daily prayer.', 'heart', 'LOYALTY', 0, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;
