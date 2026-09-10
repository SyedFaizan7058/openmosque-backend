-- ==============================================================================
-- Flyway Migration: V6 - Seed Authentic Global Mosques & Complete Facility Data
-- ==============================================================================

-- Ensure uuid extension is available
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Insert Additional Standard Facilities if not already present
INSERT INTO facilities (code, name, description, icon_name) VALUES
('QURAN_CLASSES', 'Quran & Tajweed Classes', 'Structured Quran recitation and memorization circles for youth and adults', 'book-open'),
('YOUTH_PROGRAMS', 'Youth & Sports Programs', 'Dedicated youth club, mentorship, and sports activities', 'graduation-cap'),
('COMMUNITY_HALL', 'Community & Event Hall', 'Multi-purpose hall for community gatherings, Nikah ceremonies, and lectures', 'building')
ON CONFLICT (code) DO NOTHING;

-- 2. Insert Famous Global Mosques
-- Mosque 1: East London Mosque & London Muslim Centre
INSERT INTO mosques (
    id, name, slug, description, address, city, state, country, postal_code,
    latitude, longitude, location, contact_phone, contact_email, website_url, live_stream_url,
    is_verified, status
) VALUES (
    '18558e48-a2aa-4219-9583-54cd9a84a76c',
    'East London Mosque & London Muslim Centre',
    'east-london-mosque-london',
    'One of the largest and most historic mosques in the United Kingdom, serving tens of thousands of worshippers weekly with extensive community services.',
    '82-92 Whitechapel Rd',
    'London',
    'Greater London',
    'United Kingdom',
    'E1 1JQ',
    51.5186,
    -0.0655,
    ST_SetSRID(ST_MakePoint(-0.0655, 51.5186), 4326),
    '+44 20 7650 3000',
    'info@eastlondonmosque.org.uk',
    'https://www.eastlondonmosque.org.uk',
    'https://www.youtube.com/@EastLondonMosqueLMC',
    TRUE,
    'ACTIVE'
) ON CONFLICT (slug) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_verified = TRUE;

-- Mosque 2: Islamic Cultural Center of New York
INSERT INTO mosques (
    id, name, slug, description, address, city, state, country, postal_code,
    latitude, longitude, location, contact_phone, contact_email, website_url, live_stream_url,
    is_verified, status
) VALUES (
    '28558e48-a2aa-4219-9583-54cd9a84a76d',
    'Islamic Cultural Center of New York',
    'islamic-cultural-center-new-york',
    'The first purpose-built mosque in New York City, located in Manhattan. Beautiful architectural blend of traditional Islamic and modern styles.',
    '1711 3rd Ave, Upper East Side',
    'New York',
    'NY',
    'United States',
    '10029',
    40.7844,
    -73.9507,
    ST_SetSRID(ST_MakePoint(-73.9507, 40.7844), 4326),
    '+1 212-722-5234',
    'contact@iccny.org',
    'https://iccny.org',
    'https://youtube.com/live/iccny-stream',
    TRUE,
    'ACTIVE'
) ON CONFLICT (slug) DO NOTHING;

-- Mosque 3: Toronto Muslim Community Center (Masjid Toronto)
INSERT INTO mosques (
    id, name, slug, description, address, city, state, country, postal_code,
    latitude, longitude, location, contact_phone, contact_email, website_url, live_stream_url,
    is_verified, status
) VALUES (
    '38558e48-a2aa-4219-9583-54cd9a84a76e',
    'Masjid Toronto Downtown',
    'masjid-toronto-downtown',
    'A bustling downtown mosque serving thousands of students, professionals, and residents with daily prayers and community initiatives.',
    '168 Dundas St W',
    'Toronto',
    'ON',
    'Canada',
    'M5G 1C6',
    43.6547,
    -79.3860,
    ST_SetSRID(ST_MakePoint(-79.3860, 43.6547), 4326),
    '+1 416-596-0507',
    'office@masjidtoronto.com',
    'https://masjidtoronto.com',
    'https://youtube.com/live/masjid-toronto',
    TRUE,
    'ACTIVE'
) ON CONFLICT (slug) DO NOTHING;

-- Mosque 4: Birmingham Central Mosque
INSERT INTO mosques (
    id, name, slug, description, address, city, state, country, postal_code,
    latitude, longitude, location, contact_phone, contact_email, website_url, live_stream_url,
    is_verified, status
) VALUES (
    '48558e48-a2aa-4219-9583-54cd9a84a76f',
    'Birmingham Central Mosque',
    'birmingham-central-mosque-birmingham',
    'One of the largest mosques in Europe and a key cultural hub in the West Midlands with capacity for over 6,000 worshippers.',
    '180 Belgrave Middleway, Highgate',
    'Birmingham',
    'West Midlands',
    'United Kingdom',
    'B12 0XS',
    52.4678,
    -1.8906,
    ST_SetSRID(ST_MakePoint(-1.8906, 52.4678), 4326),
    '+44 121 440 5355',
    'enquiries@centralmosque.org.uk',
    'https://birminghamcentralmosque.org.uk',
    'https://youtube.com/live/birmingham-central',
    TRUE,
    'ACTIVE'
) ON CONFLICT (slug) DO NOTHING;

-- Mosque 5: Sultanahmet Mosque (Blue Mosque)
INSERT INTO mosques (
    id, name, slug, description, address, city, state, country, postal_code,
    latitude, longitude, location, contact_phone, contact_email, website_url, live_stream_url,
    is_verified, status
) VALUES (
    '58558e48-a2aa-4219-9583-54cd9a84a770',
    'Sultanahmet Mosque (Blue Mosque)',
    'sultanahmet-mosque-istanbul',
    'Iconic historical imperial mosque built between 1609 and 1616, celebrated worldwide for hand-painted blue tiles and towering minarets.',
    'Sultan Ahmet, Atmeydani Cd. No:7, Fatih',
    'Istanbul',
    'Istanbul',
    'Turkey',
    '34122',
    41.0054,
    28.9768,
    ST_SetSRID(ST_MakePoint(28.9768, 41.0054), 4326),
    '+90 212 458 44 04',
    'info@sultanahmetcamii.org',
    'https://sultanahmetcamii.org',
    'https://youtube.com/live/blue-mosque',
    TRUE,
    'ACTIVE'
) ON CONFLICT (slug) DO NOTHING;

-- Mosque 6: King Fahd Islamic Cultural Center
INSERT INTO mosques (
    id, name, slug, description, address, city, state, country, postal_code,
    latitude, longitude, location, contact_phone, contact_email, website_url, live_stream_url,
    is_verified, status
) VALUES (
    '68558e48-a2aa-4219-9583-54cd9a84a771',
    'King Fahd Islamic Cultural Center',
    'king-fahd-islamic-center-buenos-aires',
    'The largest mosque in Latin America, featuring expansive gardens, library, educational college, and sports facilities.',
    'Av. Int. Bullrich 55, Palermo',
    'Buenos Aires',
    'Buenos Aires',
    'Argentina',
    'C1425',
    -34.5721,
    -58.4239,
    ST_SetSRID(ST_MakePoint(-58.4239, -34.5721), 4326),
    '+54 11 4899 1144',
    'contacto@ccikf.org.ar',
    'https://ccikf.org.ar',
    'https://youtube.com/live/ccikf',
    TRUE,
    'ACTIVE'
) ON CONFLICT (slug) DO NOTHING;

-- Mosque 7: Mosque Maryam (Chicago)
INSERT INTO mosques (
    id, name, slug, description, address, city, state, country, postal_code,
    latitude, longitude, location, contact_phone, contact_email, website_url, live_stream_url,
    is_verified, status
) VALUES (
    '78558e48-a2aa-4219-9583-54cd9a84a772',
    'Downtown Chicago Islamic Center',
    'downtown-chicago-islamic-center',
    'Centrally located community mosque providing daily congregational prayers, youth Quran programs, and interfaith dialogue.',
    '231 S State St',
    'Chicago',
    'IL',
    'United States',
    '60604',
    41.8789,
    -87.6278,
    ST_SetSRID(ST_MakePoint(-87.6278, 41.8789), 4326),
    '+1 312-555-0199',
    'info@chicagomosque.org',
    'https://chicagomosque.org',
    'https://youtube.com/live/chicago-ic',
    TRUE,
    'ACTIVE'
) ON CONFLICT (slug) DO NOTHING;

-- Mosque 8: Al-Farooq Omar Bin Al-Khattab Mosque (Dubai)
INSERT INTO mosques (
    id, name, slug, description, address, city, state, country, postal_code,
    latitude, longitude, location, contact_phone, contact_email, website_url, live_stream_url,
    is_verified, status
) VALUES (
    '88558e48-a2aa-4219-9583-54cd9a84a773',
    'Al Farooq Omar Bin Al Khattab Mosque',
    'al-farooq-omar-bin-al-khattab-mosque-dubai',
    'Known as the Blue Mosque of Dubai, accommodating up to 2,000 worshippers with traditional Ottoman architecture and an expansive Islamic library.',
    'Al Safa 1, Jumeirah',
    'Dubai',
    'Dubai',
    'United Arab Emirates',
    '00000',
    25.1764,
    55.2422,
    ST_SetSRID(ST_MakePoint(55.2422, 25.1764), 4326),
    '+971 4 394 4448',
    'info@alfarooqcentre.com',
    'https://alfarooqcentre.com',
    'https://youtube.com/live/alfarooq-dubai',
    TRUE,
    'ACTIVE'
) ON CONFLICT (slug) DO NOTHING;

-- 3. Associate Facilities with Mosques
INSERT INTO mosque_facilities (mosque_id, facility_id, custom_details)
SELECT m.id, f.id, 'Available with full access'
FROM mosques m
CROSS JOIN facilities f
WHERE m.slug IN (
    'east-london-mosque-london',
    'islamic-cultural-center-new-york',
    'masjid-toronto-downtown',
    'birmingham-central-mosque-birmingham',
    'sultanahmet-mosque-istanbul',
    'king-fahd-islamic-center-buenos-aires',
    'downtown-chicago-islamic-center',
    'al-farooq-omar-bin-al-khattab-mosque-dubai'
)
AND f.code IN ('WUDU_AREA', 'WOMENS_SECTION', 'WHEELCHAIR_ACCESSIBILITY', 'PARKING', 'AIR_CONDITIONING', 'LIBRARY')
ON CONFLICT (mosque_id, facility_id) DO NOTHING;

-- 4. Seed Cover and Gallery Images
INSERT INTO mosque_images (mosque_id, image_url, caption, is_cover, display_order)
SELECT id, 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=1200&q=80', 'Front Courtyard and Minarets', TRUE, 0
FROM mosques WHERE slug = 'east-london-mosque-london'
ON CONFLICT DO NOTHING;

INSERT INTO mosque_images (mosque_id, image_url, caption, is_cover, display_order)
SELECT id, 'https://images.unsplash.com/photo-1564769625905-50e93615e769?auto=format&fit=crop&w=1200&q=80', 'Main Prayer Hall Dome', TRUE, 0
FROM mosques WHERE slug = 'islamic-cultural-center-new-york'
ON CONFLICT DO NOTHING;

INSERT INTO mosque_images (mosque_id, image_url, caption, is_cover, display_order)
SELECT id, 'https://images.unsplash.com/photo-1584551246679-0daf3d275d0f?auto=format&fit=crop&w=1200&q=80', 'Facade and Entryway', TRUE, 0
FROM mosques WHERE slug = 'masjid-toronto-downtown'
ON CONFLICT DO NOTHING;

INSERT INTO mosque_images (mosque_id, image_url, caption, is_cover, display_order)
SELECT id, 'https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?auto=format&fit=crop&w=1200&q=80', 'Exterior and Gardens', TRUE, 0
FROM mosques WHERE slug = 'birmingham-central-mosque-birmingham'
ON CONFLICT DO NOTHING;

INSERT INTO mosque_images (mosque_id, image_url, caption, is_cover, display_order)
SELECT id, 'https://images.unsplash.com/photo-1564507592333-c60657eea523?auto=format&fit=crop&w=1200&q=80', 'Historic Domes at Sunset', TRUE, 0
FROM mosques WHERE slug = 'sultanahmet-mosque-istanbul'
ON CONFLICT DO NOTHING;

INSERT INTO mosque_images (mosque_id, image_url, caption, is_cover, display_order)
SELECT id, 'https://images.unsplash.com/photo-1580618672591-eb180b1a973f?auto=format&fit=crop&w=1200&q=80', 'Grand Archways', TRUE, 0
FROM mosques WHERE slug = 'king-fahd-islamic-center-buenos-aires'
ON CONFLICT DO NOTHING;

INSERT INTO mosque_images (mosque_id, image_url, caption, is_cover, display_order)
SELECT id, 'https://images.unsplash.com/photo-1519817650390-64a93db51149?auto=format&fit=crop&w=1200&q=80', 'Urban Prayer Center', TRUE, 0
FROM mosques WHERE slug = 'downtown-chicago-islamic-center'
ON CONFLICT DO NOTHING;

INSERT INTO mosque_images (mosque_id, image_url, caption, is_cover, display_order)
SELECT id, 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=1200&q=80', 'Courtyard and Ottoman Minarets', TRUE, 0
FROM mosques WHERE slug = 'al-farooq-omar-bin-al-khattab-mosque-dubai'
ON CONFLICT DO NOTHING;

-- 5. Seed Prayer Configurations for All Mosques
INSERT INTO mosque_prayer_configs (mosque_id, calculation_method, juristic_school, time_zone)
SELECT id, 'MUSLIM_WORLD_LEAGUE', 'STANDARD',
  CASE
    WHEN city = 'London' OR city = 'Birmingham' THEN 'Europe/London'
    WHEN city = 'New York' THEN 'America/New_York'
    WHEN city = 'Chicago' THEN 'America/Chicago'
    WHEN city = 'Toronto' THEN 'America/Toronto'
    WHEN city = 'Istanbul' THEN 'Europe/Istanbul'
    WHEN city = 'Dubai' THEN 'Asia/Dubai'
    ELSE 'UTC'
  END
FROM mosques
ON CONFLICT (mosque_id) DO NOTHING;

-- 6. Seed Iqamah Schedules & Friday Jumu'ah Times
INSERT INTO mosque_iqamah_schedules (
    mosque_id, fajr_type, fajr_offset_minutes, dhuhr_type, dhuhr_fixed_time,
    asr_type, asr_offset_minutes, maghrib_type, maghrib_offset_minutes,
    isha_type, isha_offset_minutes, jummah_1_time, jummah_2_time, jummah_khutbah_language
)
SELECT id, 'OFFSET_AFTER_ADHAN', 20, 'FIXED_TIME', '13:15:00',
       'OFFSET_AFTER_ADHAN', 15, 'OFFSET_AFTER_ADHAN', 10,
       'OFFSET_AFTER_ADHAN', 15, '13:00:00', '14:00:00', 'English & Arabic'
FROM mosques
ON CONFLICT (mosque_id) DO NOTHING;
