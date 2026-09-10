-- ==============================================================================
-- Flyway Migration: V8 - Seed Phase 4 Community Events, Khutbahs, Reviews, Q&A
-- ==============================================================================

-- 1. Ensure Demo Users exist for attribution
INSERT INTO users (id, firebase_uid, email, display_name, role, points, is_active, is_verified)
VALUES 
('a1111111-1111-1111-1111-111111111111', 'demo-contributor-uid', 'contributor@openmosque.org', 'Brother Tariq', 'USER', 150, TRUE, TRUE),
('a2222222-2222-2222-2222-222222222222', 'demo-imam-uid', 'imam@eastlondonmosque.org.uk', 'Imam Abdul Rahman', 'MOSQUE_ADMIN', 500, TRUE, TRUE),
('a3333333-3333-3333-3333-333333333333', 'demo-sister-uid', 'fatimah@openmosque.org', 'Sister Fatimah', 'USER', 80, TRUE, TRUE)
ON CONFLICT (email) DO UPDATE 
SET display_name = EXCLUDED.display_name,
    role = EXCLUDED.role,
    points = EXCLUDED.points,
    is_active = EXCLUDED.is_active,
    is_verified = EXCLUDED.is_verified;

-- 2. Seed Community Events
INSERT INTO mosque_events (
    id, mosque_id, title, description, event_type, audience,
    start_date_time, end_date_time, location_details, speaker_name,
    banner_image_url, is_cancelled, created_by_user_id
)
SELECT 
    'e1111111-1111-1111-1111-111111111111',
    id,
    'Weekly Tafseer & Quranic Reflection Circle',
    'Join our weekly gathering studying the classical commentary of Surah Al-Kahf with real-world contemporary applications.',
    'HALAQAH',
    'ALL',
    CURRENT_TIMESTAMP + INTERVAL '2 days' + INTERVAL '18 hours',
    CURRENT_TIMESTAMP + INTERVAL '2 days' + INTERVAL '20 hours',
    'Main Prayer Hall - Seminar Section',
    'Shaykh Abdul Rahman',
    'https://images.unsplash.com/photo-1609599006353-e629aaabfeae?auto=format&fit=crop&w=1200&q=80',
    FALSE,
    'a2222222-2222-2222-2222-222222222222'
FROM mosques WHERE slug = 'east-london-mosque-london'
ON CONFLICT (id) DO NOTHING;

INSERT INTO mosque_events (
    id, mosque_id, title, description, event_type, audience,
    start_date_time, end_date_time, location_details, speaker_name,
    banner_image_url, is_cancelled, created_by_user_id
)
SELECT 
    'e2222222-2222-2222-2222-222222222222',
    id,
    'Youth Leadership & Sports Mentorship',
    'Empowering our youth with Islamic character building, team sports, leadership training, and brotherhood.',
    'YOUTH_PROGRAM',
    'YOUTH',
    CURRENT_TIMESTAMP + INTERVAL '4 days' + INTERVAL '14 hours',
    CURRENT_TIMESTAMP + INTERVAL '4 days' + INTERVAL '17 hours',
    'Community Gymnasium & Youth Lounge',
    'Ustadh Bilal Khan',
    'https://images.unsplash.com/photo-1526676037777-05a232554f77?auto=format&fit=crop&w=1200&q=80',
    FALSE,
    'a1111111-1111-1111-1111-111111111111'
FROM mosques WHERE slug = 'islamic-cultural-center-new-york'
ON CONFLICT (id) DO NOTHING;

INSERT INTO mosque_events (
    id, mosque_id, title, description, event_type, audience,
    start_date_time, end_date_time, location_details, speaker_name,
    banner_image_url, is_cancelled, created_by_user_id
)
SELECT 
    'e3333333-3333-3333-3333-333333333333',
    id,
    'Sisters Tajweed & Quran Circle',
    'Structured weekly Tajweed rules, Quranic recitation correction, and sisterhood gathering in a dedicated setting.',
    'WORKSHOP',
    'SISTERS',
    CURRENT_TIMESTAMP + INTERVAL '5 days' + INTERVAL '10 hours',
    CURRENT_TIMESTAMP + INTERVAL '5 days' + INTERVAL '12 hours',
    '2nd Floor Women''s Multipurpose Suite',
    'Ustadha Maryam Al-Hashemi',
    'https://images.unsplash.com/photo-1585036156171-384164a8c675?auto=format&fit=crop&w=1200&q=80',
    FALSE,
    'a3333333-3333-3333-3333-333333333333'
FROM mosques WHERE slug = 'masjid-toronto-downtown'
ON CONFLICT (id) DO NOTHING;

-- 3. Seed Friday Jumu'ah Khutbahs
INSERT INTO mosque_khutbahs (
    id, mosque_id, khutbah_date, topic, khatib_name, batch_number,
    khutbah_time, adhaan_time, iqamah_time, language, stream_url, notes
)
SELECT 
    'c1111111-1111-1111-1111-111111111111',
    id,
    CURRENT_DATE + ((5 - EXTRACT(DOW FROM CURRENT_DATE))::INTEGER % 7),
    'Strengthening the Bonds of Kinship & Community Brotherhood',
    'Imam Abdul Rahman',
    1,
    '13:00:00',
    '12:45:00',
    '13:25:00',
    'English & Arabic',
    'https://www.youtube.com/@EastLondonMosqueLMC',
    'Please arrive 15 minutes early to secure prayer space. Underground parking available.'
FROM mosques WHERE slug = 'east-london-mosque-london'
ON CONFLICT (id) DO NOTHING;

INSERT INTO mosque_khutbahs (
    id, mosque_id, khutbah_date, topic, khatib_name, batch_number,
    khutbah_time, adhaan_time, iqamah_time, language, stream_url, notes
)
SELECT 
    'c2222222-2222-2222-2222-222222222222',
    id,
    CURRENT_DATE + ((5 - EXTRACT(DOW FROM CURRENT_DATE))::INTEGER % 7),
    'Steadfastness and Gratitude in Modern Times',
    'Dr. Ahmad Hassan',
    1,
    '13:15:00',
    '13:00:00',
    '13:40:00',
    'English & Arabic',
    'https://www.youtube.com/live/demo-stream',
    'Second Jumu''ah prayer follows promptly at 14:15.'
FROM mosques WHERE slug = 'islamic-cultural-center-new-york'
ON CONFLICT (id) DO NOTHING;

-- 4. Seed Mosque Reviews
INSERT INTO mosque_reviews (
    id, mosque_id, user_id, rating_overall, rating_cleanliness,
    rating_facilities, rating_womens_area, rating_parking, review_text, status
)
SELECT 
    'd1111111-1111-1111-1111-111111111111',
    m.id,
    u.id,
    5, 5, 5, 5, 4,
    'Exceptional mosque with world-class facilities. The Wudu area is always pristine, acoustics in the main hall are inspiring, and the staff is extremely welcoming.',
    'PUBLISHED'
FROM mosques m, users u
WHERE m.slug = 'east-london-mosque-london' AND u.email = 'contributor@openmosque.org'
ON CONFLICT (mosque_id, user_id) DO NOTHING;

INSERT INTO mosque_reviews (
    id, mosque_id, user_id, rating_overall, rating_cleanliness,
    rating_facilities, rating_womens_area, rating_parking, review_text, status
)
SELECT 
    'd2222222-2222-2222-2222-222222222222',
    m.id,
    u.id,
    5, 5, 5, 5, 3,
    'A spiritual oasis in Manhattan! Gorgeous architecture, dedicated and spacious women''s section with clear sound and video feeds for the Khutbah.',
    'PUBLISHED'
FROM mosques m, users u
WHERE m.slug = 'islamic-cultural-center-new-york' AND u.email = 'fatimah@openmosque.org'
ON CONFLICT (mosque_id, user_id) DO NOTHING;

-- 5. Seed Community Questions & Answers
INSERT INTO mosque_questions (
    id, mosque_id, user_id, question_text, status
)
SELECT 
    'f1111111-1111-1111-1111-111111111111',
    m.id,
    u.id,
    'Is there full wheelchair and stroller accessibility to both the brothers and sisters prayer areas?',
    'ANSWERED'
FROM mosques m, users u
WHERE m.slug = 'east-london-mosque-london' AND u.email = 'contributor@openmosque.org'
ON CONFLICT (id) DO NOTHING;

INSERT INTO mosque_answers (
    id, question_id, user_id, answer_text, is_official_mosque_admin, status
)
SELECT 
    'f2222222-2222-2222-2222-222222222222',
    'f1111111-1111-1111-1111-111111111111',
    u.id,
    'Yes, alhamdulillah! All main entrances have ramps, and elevator access is available directly from the ground floor to the upper sisters gallery and basement wudu facilities.',
    TRUE,
    'PUBLISHED'
FROM users u
WHERE u.email = 'imam@eastlondonmosque.org.uk'
ON CONFLICT (id) DO NOTHING;
