# OpenMosque Backend — Complete End-to-End Technical Review Specification
# Document Version: 5.0 (Full Monolith Architecture & Production Deployment Specification)
# Target Audience: AI Code Reviewers, Systems Architects, Security Auditors & Frontend Engineers

================================================================================
TABLE OF CONTENTS
================================================================================
1. System Overview & Core Mission
2. High-Level Architecture & Clean Modular Monolith Design
3. Complete Technology Stack & Version Matrix
4. Source Code Package Anatomy
5. Security, Authentication & Role-Based Access Control (RBAC)
6. PostGIS Geospatial Engine & Spatial Query Architecture
7. Database Schema & Flyway Migration Log (V1 to V13)
8. Comprehensive Breakdown of All 10 Core Modules
   8.1. User & Identity Module
   8.2. Mosque Directory & Facilities Module
   8.3. Mosque Ownership Claim & Verification Module
   8.4. Dynamic Prayer Times & Iqamah Engine
   8.5. Community Engagement: Reviews, Ratings, Q&A & Flagging
   8.6. Events & Friday Khutbahs Module
   8.7. Media Upload & Multi-part Storage Module
   8.8. Crowdsourcing Moderation & Contributor Gamification Module
   8.9. User Notifications & Bookmarks/Favorites Module
   8.10. OpenStreetMap (OSM) Automated Ingestion & Deduplication Engine
9. Exhaustive API Reference Catalog (All 48 REST Endpoints)
10. DevOps, Cloud Deployment & Database Infrastructure (Neon + Render)
11. Test Coverage & Quality Assurance
12. AI Reviewer Checklist & Code Audit Prompts

================================================================================
1. SYSTEM OVERVIEW & CORE MISSION
================================================================================
OpenMosque is a production-ready, open-source, community-driven backend engine designed to eliminate the fragmentation of mosque directories, prayer timetables, Islamic amenities, and local community broadcasts worldwide.

Key Architectural Value Drivers:
- Native PostGIS Spatial Queries: Sub-millisecond geographic proximity searches without external geocoding bottlenecks.
- Zero-Password Stateless Authentication: Delegated to Firebase Identity Platform with Spring Security 6 stateless JWT interception.
- Official Mosque Administration Lifecycle: Verification workflows allowing Imams and trustees to claim official control over mosque profiles, manage daily Iqamah timings, and broadcast community events.
- Crowdsourced Directory with Moderation & Gamification: Users submit new mosques or correction proposals; community moderators approve/reject via an audit queue; contributors earn points and tier badges.
- Automated OpenStreetMap Ingestion: Autonomous crawler fetching global mosque geometries from the Overpass API with spatial deduplication (50-meter threshold).

================================================================================
2. HIGH-LEVEL ARCHITECTURE & CLEAN MODULAR MONOLITH DESIGN
================================================================================
The backend is architected as a Clean Modular Monolith in Spring Boot 3.3.3. While contained within a single deployable artifact for operational simplicity, modules maintain strict boundaries, modular package encapsulation, and decoupled services.

       [ Client Applications: React 18 Web / React Native Mobile ]
                                 │
                                 │ (HTTPS + Firebase JWT)
                                 ▼
         ┌──────────────────────────────────────────────┐
         │      Spring Security 6 Stateless Pipeline    │
         │  (FirebaseAuthFilter + RateLimitingFilter)   │
         └──────────────────────┬───────────────────────┘
                                │
        ┌───────────────────────┼────────────────────────┐
        ▼                       ▼                        ▼
  [User & Identity]     [Mosque Directory]       [Prayer Times Engine]
   - Firebase Sync       - PostGIS ST_DWithin     - AlAdhan Calculation
   - RBAC Roles          - Amenity Filters        - Iqamah Offsets/Fixed
   - Gamification        - Cover Media            - Jumu'ah Schedules
        │                       │                        │
        ▼                       ▼                        ▼
  [Claim Management]    [Community & Q&A]        [Moderation & Audit]
   - Legal Proofs        - 5-Star Reviews         - Crowdsource Submissions
   - Trustee Approval    - Questions & Answers    - Field-Level Diff Audit
   - Imam Verification   - Content Flags          - Points & Badges
        │                       │                        │
        ▼                       ▼                        ▼
  [Events & Khutbahs]   [Media Storage]          [OSM Ingestion Engine]
   - Live Streams        - Local/Cloud Upload     - Overpass API Geo Crawler
   - Jummah Archives     - Multi-part Handlers    - Spatial Deduplication
        │                       │                        │
        └───────────────────────┴────────────────────────┘
                                │
                                ▼
         ┌──────────────────────────────────────────────┐
         │     Hibernate Spatial 6.5 + JTS Topology     │
         └──────────────────────┬───────────────────────┘
                                │
                                ▼
         ┌──────────────────────────────────────────────┐
         │   PostgreSQL 16/18 + PostGIS Spatial Engine  │
         │         (Hosted on Neon Cloud / AWS)         │
         └──────────────────────────────────────────────┘

================================================================================
3. COMPLETE TECHNOLOGY STACK & VERSION MATRIX
================================================================================
- Java Runtime: Java 17 LTS
- Core Framework: Spring Boot 3.3.3
- Security Framework: Spring Security 6.x (Stateless Session Management)
- Cloud Database: PostgreSQL 16+ (Hosted on Neon Serverless PostgreSQL with pooling)
- Spatial Extension: PostGIS 3.4+
- Spatial ORM Layer: Hibernate Spatial 6.5.2.Final + JTS (LocationTech JTS 1.19.0)
- Database Migrations: Flyway Core 10.x + Flyway PostgreSQL
- Authentication Provider: Firebase Admin SDK 9.2.0 (Google Identity Platform)
- Object Mapping: MapStruct 1.5.5.Final
- Boilerplate Elimination: Project Lombok 1.18.30 (@SuperBuilder, @Getter, @RequiredArgsConstructor)
- API Documentation: SpringDoc OpenAPI 3.0 / Swagger UI (springdoc-openapi-starter-webmvc-ui 2.3.0)
- Rate Limiting: Bucket4j / In-memory token bucket filters
- Containerization: Docker (Eclipse Temurin 17 JRE Alpine Multi-stage build)
- Cloud Host: Render Web Service (render.yaml configured)

================================================================================
4. SOURCE CODE PACKAGE ANATOMY
================================================================================
Root Package: com.openmosque

com.openmosque
├── OpenMosqueApplication.java        # Spring Boot Entry Point & JtsModule Bean
├── common/
│   ├── config/                       # WebConfig, JtsGeoConfig, OpenApiConfig
│   ├── exception/                    # GlobalExceptionHandler, ResourceNotFoundException,
│   │                                 # UnauthorizedException, ForbiddenException, BadRequestException
│   ├── model/                        # ApiResponse<T>, PageResponse<T>, BaseEntity
│   └── util/                         # GeoUtils (WGS84 coordinate conversions, Point factories)
├── security/
│   ├── annotation/                   # @CurrentUser (Parameter injection annotation)
│   ├── config/                       # SecurityConfig, CorsConfig
│   ├── filter/                       # FirebaseAuthFilter, SecurityHeadersFilter
│   ├── model/                        # CustomUserDetails, FirebaseAuthenticationToken
│   ├── ratelimit/                    # RateLimitingFilter (IP & user tier throttling)
│   ├── resolver/                     # CurrentUserArgumentResolver (HandlerMethodArgumentResolver)
│   └── service/                      # FirebaseTokenVerifier (supports live & dev-mock modes)
└── modules/
    ├── claim/                        # Mosque ownership claims, trustee verification, documents
    ├── community/                    # Reviews, ratings, Q&A discussions, upvoting, flags
    ├── event/                        # Announcements, events, Friday Khutbah recordings
    ├── ingestion/                    # OpenStreetMap Overpass sync, geometry deduplication
    ├── media/                        # Multi-part file upload, validation, static file serving
    ├── moderation/                   # Submissions queue, approval workflows, audit logs, gamification
    ├── mosque/                       # Mosques CRUD, PostGIS spatial search, facilities catalog
    ├── notification/                 # In-app notifications, user alerts
    ├── prayer/                       # AlAdhan client, calculation methods, Iqamah schedules
    └── user/                         # User profiles, sync, favorites, contributor badges

================================================================================
5. SECURITY, AUTHENTICATION & ROLE-BASED ACCESS CONTROL (RBAC)
================================================================================
5.1. Zero Password Storage Architecture:
- User passwords NEVER enter or touch the OpenMosque backend or database.
- Authentication is handled exclusively by Google Firebase Identity Platform.
- Users authenticate via Email/Password, Google OAuth, or Apple Sign-In on the client.
- The client receives a cryptographically signed Firebase ID Token and transmits it via HTTP Header:
  Authorization: Bearer <FIREBASE_ID_TOKEN>

5.2. Request Lifecycle & Interception (FirebaseAuthFilter):
1. Incoming HTTP requests pass through FirebaseAuthFilter.
2. The filter extracts the Bearer token and delegates validation to FirebaseTokenVerifier.
3. Development Mock Auth Mode: In local/test environments (app.security.firebase.dev-mock-auth: true), special test tokens (e.g., dev-user-..., dev-admin-...) bypass Firebase network calls for rapid offline testing.
4. On valid verification, the user's firebase_uid is looked up in PostgreSQL.
5. If the user does not exist, an on-demand synchronization automatically provisions a User entity with role USER.
6. An authenticated FirebaseAuthenticationToken is populated in SecurityContextHolder.

5.3. Custom @CurrentUser Parameter Resolver:
Controllers do not manually parse security contexts. Any controller method can declare:
public ResponseEntity<ApiResponse<T>> execute(@CurrentUser User user)
The custom CurrentUserArgumentResolver automatically validates and injects the authenticated User JPA entity.

5.4. User Role Hierarchy:
- USER: Regular worshipper. Can view directory, post reviews, ask questions, submit new mosques, bookmark favorites.
- MOSQUE_ADMIN: Mosque Imam or Trustee. Has verified ownership over a specific mosque. Can configure Iqamah schedules, publish events, and edit mosque profile details.
- MODERATOR: Community Trustee. Can approve/reject user submissions, inspect photo uploads, and resolve content flags.
- SUPER_ADMIN: System Administrator. Can approve Mosque Ownership Claims, appoint moderators, trigger bulk ingestion, and view global audit logs.

================================================================================
6. POSTGIS GEOSPATIAL ENGINE & SPATIAL QUERY ARCHITECTURE
================================================================================
6.1. Geometry Representation:
All mosque geographic coordinates are stored in PostgreSQL using the PostGIS geometry(Point, 4326) type, mapped in Hibernate via LocationTech JTS org.locationtech.jts.geom.Point.

6.2. Spatial Indexing:
A Generalized Search Tree (GIST) index guarantees sub-millisecond execution times even with hundreds of thousands of mosques:
CREATE INDEX idx_mosques_location ON mosques USING GIST (location);

6.3. Spatial Proximity Search (ST_DWithin):
The proximity finder executes native PostGIS spatial functions using the Earth spheroid model:
SELECT m.*, 
       ST_DistanceSphere(m.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)) / 1000.0 AS distance_km
FROM mosques m
WHERE m.status = 'ACTIVE'
  AND ST_DWithin(
        m.location::geography, 
        ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography, 
        :radiusMeters
      )
ORDER BY distance_km ASC;

Benefits:
- Eliminates Euclidean planar distortion on the spherical globe.
- Calculates exact driving/crow-flies distance in kilometers in a single query.
- Filters by amenities/facilities in the same query via SQL JOINs.

================================================================================
7. DATABASE SCHEMA & FLYWAY MIGRATION LOG (V1 to V13)
================================================================================
All database schemas are strictly version-controlled via Flyway migrations in src/main/resources/db/migration:

- V1__init_postgis_and_users.sql:
  - Activates PostGIS extension (CREATE EXTENSION IF NOT EXISTS postgis;).
  - Creates users table (id, firebase_uid, email, display_name, role, points, active, created_at, updated_at).
- V2__init_mosques_and_facilities.sql:
  - Creates facilities catalog (Wudu, Wheelchair, Women's Section, Parking, Halal Food, Janazah Service, Quran Classes, Library).
  - Creates mosques table (id, name, slug, address, city, state, country, postal_code, latitude, longitude, location GEOMETRY, status, verified, capacity, established_year, contact_email, contact_phone, website_url, cover_image_url).
  - Creates mosque_facilities join table.
  - Creates mosque_photos table.
- V3__add_submission_collection_tables.sql:
  - Creates mosque_submissions table (crowdsourced proposals with JSONB payload of suggested changes, status PENDING, APPROVED, REJECTED).
  - Creates moderation_logs table (auditing reviewer decisions and comments).
- V4__init_mosque_claims.sql:
  - Creates mosque_claims table for Imams/Trustees claiming official mosque administration (status, claimant_role, proof_document_url, contact_phone).
- V5__init_prayer_times_and_iqamah.sql:
  - Creates mosque_prayer_configs table (calculation_method, juristic_school, time_zone, high_latitude_rule).
  - Creates mosque_iqamah_schedules table (fajr_type, fajr_fixed_time, fajr_offset_minutes, dhuhr_type, asr_type, maghrib_type, isha_type, jummah1_time, jummah2_time, jummah_khutbah_language).
- V6__seed_rich_global_mosques.sql:
  - Seeds rich production data for premier international mosques across London, New York, Istanbul, Medina, Kuala Lumpur, and Cairo.
- V7__create_phase4_media_events_community_tables.sql:
  - Creates mosque_reviews table (1-5 star ratings, review text, visit type).
  - Creates mosque_questions and mosque_answers tables (community Q&A thread, accepted answer flag).
  - Creates mosque_events table (title, start/end time, description, location type).
  - Creates mosque_khutbahs table (Friday sermon archive, speaker name, language, audio/video stream URL).
  - Creates community_content_flags table (flagging spam/offensive content).
- V8__seed_phase4_community_data.sql:
  - Seeds reviews, Q&As, events, and khutbah archives for initial onboarding.
- V9__add_user_preferred_location.sql:
  - Adds user preferred latitude, longitude, and search radius to users.
- V10__add_custom_adhan_times.sql:
  - Adds custom manual Adhan overrides in mosque_prayer_configs.
- V11__init_favorites_and_badges.sql:
  - Creates user_favorite_mosques table (bookmarks).
  - Creates contributor_badges and user_badges tables (Bronze, Silver, Gold, Platinum).
- V12__create_user_notifications.sql:
  - Creates user_notifications table (read/unread, title, type, link).
- V13__alter_claim_proof_document_url_to_text.sql:
  - Alters proof document column from VARCHAR to TEXT for cloud base64/long URL support.

================================================================================
8. COMPREHENSIVE BREAKDOWN OF ALL 10 CORE MODULES
================================================================================

8.1. User & Identity Module (com.openmosque.modules.user)
- Handles user onboarding, profile updates, and gamification points tracking.
- Sync Endpoint: POST /api/v1/users/sync idempotently maps Firebase UID to Postgres entity.
- Profile Endpoint: GET /api/v1/users/me returns current user stats, assigned badges, and permissions.
- Bookmarks: Users can favorite mosques for quick offline access and home screen widgets.

8.2. Mosque Directory & Facilities Module (com.openmosque.modules.mosque)
- Central repository of mosques worldwide.
- Features: Name search, address lookup, city/country filters, and PostGIS ST_DWithin radius discovery.
- Amenities/Facilities: Standardized flags (WUDU_AREA, WHEELCHAIR_ACCESSIBLE, SEPARATE_WOMENS_PRAYER_HALL, DEDICATED_WOMENS_WUDU, PARKING_LOT, HALAL_FOOD_NEARBY, JANAZAH_FACILITY, ISLAMIC_LIBRARY, FULL_TIME_IMAM, WEEKEND_MADRASSA).
- Cover photos & multi-photo gallery management.

8.3. Mosque Ownership Claim & Verification Module (com.openmosque.modules.claim)
- Workflow allowing Imams, Committee Trustees, and Mosque Directors to gain administrative control.
- Claimant submits official position, phone number, and uploads legal registration/utility bill as proof document.
- State Machine: PENDING -> APPROVED (User role elevated to MOSQUE_ADMIN) or REJECTED.
- Reviewed exclusively by SUPER_ADMIN.

8.4. Dynamic Prayer Times & Iqamah Engine (com.openmosque.modules.prayer)
- Integrates with AlAdhan astronomical calculations based on mosque latitude/longitude.
- Supports all international calculation methods:
  - MUSLIM_WORLD_LEAGUE (Europe, Far East)
  - ISNA (North America)
  - EGYPTIAN (Africa, Middle East)
  - UMM_AL_QURA (Saudi Arabia / Arabian Peninsula)
  - KARACHI (Pakistan, India, Bangladesh)
  - TEHRAN, GULF, KUWAIT, QATAR, SINGAPORE, TURKEY, FRANCE, RUSSIA
- Juristic School (Asr calculation):
  - STANDARD (Shafi'i, Maliki, Hanbali)
  - HANAFI (Shadow ratio 2:1)
- Flexible Iqamah Timing Modes:
  - FIXED_TIME: Iqamah at a static time (e.g., Fajr at 05:30).
  - OFFSET_AFTER_ADHAN: Dynamic Iqamah (e.g., Maghrib exactly 10 minutes after sunset Adhan).
- Friday Jumu'ah Management: Supports multiple shifts (Jummah 1, Jummah 2), khutbah languages, and guest Khatib names.

8.5. Community Engagement: Reviews, Ratings, Q&A & Flagging (com.openmosque.modules.community)
- 5-Star Reviews: Verified worshippers can rate facilities, cleanliness, parking, and women's accommodations.
- Community Q&A: Public thread for questions like "Is parking free on Fridays?" or "Are sisters allowed in Taraweeh?".
- Answers can be submitted by community members and officially verified by Mosque Admins.
- Content Moderation: Users can flag abusive or inaccurate content (SPAM, OFFENSIVE, INACCURATE) for moderator review.

8.6. Events & Friday Khutbahs Module (com.openmosque.modules.event)
- Events: Mosque conferences, Ramadan Iftar schedules, Quran competitions, Eid prayers.
- Friday Khutbah Archives: Historical repository of sermons with audio/video stream links (YouTube, Facebook Live, SoundCloud), khutbah summaries, and multi-lingual tags.

8.7. Media Upload & Multi-part Storage Module (com.openmosque.modules.media)
- Handles secure file uploads for mosque photos, cover pictures, and trustee verification documents.
- Validates MIME types (JPEG, PNG, WEBP, PDF) and enforces size limits.
- Supports both local static directory uploads and cloud storage configurations.

8.8. Crowdsourcing Moderation & Contributor Gamification Module (com.openmosque.modules.moderation)
- Community users propose unlisted mosques or submit correction diffs.
- Changes enter a PENDING moderation queue.
- Moderators inspect submissions with side-by-side field diffs before approving.
- On approval, the proposing contributor is automatically awarded reward points (e.g., 50 points for a new mosque, 15 points for an edit).
- Tier Badges: Users level up across Contributor Badges (NOVICE -> BRONZE -> SILVER -> GOLD -> LEGEND).

8.9. User Notifications & Bookmarks/Favorites Module (com.openmosque.modules.notification)
- Dispatches in-app notifications when:
  - A user's mosque submission is approved or rejected.
  - A mosque ownership claim is decided.
  - A favorited mosque posts a new announcement or prayer schedule change.

8.10. OpenStreetMap (OSM) Automated Ingestion & Deduplication Engine (com.openmosque.modules.ingestion)
- Background ingestion service connecting to the Overpass API:
  Querying amenity=place_of_worship and religion=muslim.
- Spatial Deduplication: Calculates distance to existing mosques. If another mosque exists within 50 meters, the record is skipped or updated to prevent duplicates.
- Automates global directory bootstrapping across entire cities or countries.

================================================================================
9. EXHAUSTIVE API REFERENCE CATALOG (ALL 48 REST ENDPOINTS)
================================================================================

--- MODULE 1: SYSTEM & OPENAPI DOCUMENTATION ---
1. GET /v3/api-docs (Public) - Raw OpenAPI 3.0 JSON specification.
2. GET /swagger-ui.html (Public) - Interactive Swagger UI documentation.

--- MODULE 2: USER & IDENTITY ---
3. POST /api/v1/users/sync (Public) - Synchronize / onboard Firebase user to PostgreSQL.
4. GET /api/v1/users/me (Authenticated) - Get current user profile, role, points, and badges.
5. PUT /api/v1/users/me/location (Authenticated) - Update user preferred GPS coordinates and radius.
6. GET /api/v1/users/me/favorites (Authenticated) - List favorited / bookmarked mosques.
7. POST /api/v1/users/me/favorites/{mosqueId} (Authenticated) - Bookmark a mosque.
8. DELETE /api/v1/users/me/favorites/{mosqueId} (Authenticated) - Remove a bookmark.
9. GET /api/v1/users/me/badges (Authenticated) - List earned contributor badges.

--- MODULE 3: FACILITIES CATALOG ---
10. GET /api/v1/facilities (Public) - List all available amenity/facility types.

--- MODULE 4: MOSQUE DIRECTORY & SPATIAL SEARCH ---
11. GET /api/v1/mosques/nearby (Public) - Geospatial proximity search via PostGIS (latitude, longitude, radiusKm, facilities).
12. GET /api/v1/mosques/search (Public) - Text and keyword search (name, city, country, page, size).
13. GET /api/v1/mosques/{id} (Public) - Get full mosque profile details by UUID.
14. GET /api/v1/mosques/slug/{slug} (Public) - Get mosque details by URL-friendly slug.
15. POST /api/v1/mosques (Role: SUPER_ADMIN) - Create a verified mosque directly.
16. PUT /api/v1/mosques/{id} (Role: MOSQUE_ADMIN or SUPER_ADMIN) - Update mosque profile.
17. DELETE /api/v1/mosques/{id} (Role: SUPER_ADMIN) - Soft or hard delete a mosque.

--- MODULE 5: PRAYER TIMES & IQAMAH SCHEDULE ---
18. GET /api/v1/prayer-times/mosque/{mosqueId} (Public) - Get calculated prayer & Iqamah timings for date.
19. GET /api/v1/prayer-times/mosque/{mosqueId}/config (Public) - Get mosque calculation method & juristic settings.
20. PUT /api/v1/prayer-times/mosque/{mosqueId}/config (Role: MOSQUE_ADMIN or SUPER_ADMIN) - Update calculation config.
21. GET /api/v1/prayer-times/mosque/{mosqueId}/iqamah (Public) - Get current Iqamah schedule and Friday Jumu'ah times.
22. PUT /api/v1/prayer-times/mosque/{mosqueId}/iqamah (Role: MOSQUE_ADMIN or SUPER_ADMIN) - Update Iqamah offsets and fixed times.
23. GET /api/v1/calculation-methods (Public) - List supported global prayer calculation authorities.

--- MODULE 6: MOSQUE OWNERSHIP CLAIMS ---
24. POST /api/v1/claims (Authenticated) - Submit claim for mosque administration with proof documents.
25. GET /api/v1/claims/my (Authenticated) - View user's submitted claim statuses.
26. GET /api/v1/claims/pending (Role: SUPER_ADMIN) - List all pending claims awaiting verification.
27. PUT /api/v1/claims/{id}/review (Role: SUPER_ADMIN) - Approve or reject claim (elevates user to MOSQUE_ADMIN).

--- MODULE 7: CROWDSOURCING & MODERATION ---
28. POST /api/v1/submissions (Authenticated) - Propose new mosque or suggest edits to existing mosque.
29. GET /api/v1/submissions/my (Authenticated) - View user's submission history and awarded points.
30. GET /api/v1/submissions/pending (Role: MODERATOR or SUPER_ADMIN) - List pending moderation queue.
31. PUT /api/v1/submissions/{id}/review (Role: MODERATOR or SUPER_ADMIN) - Approve/reject submission with audit notes.
32. GET /api/v1/moderation/audit-logs (Role: SUPER_ADMIN) - View full moderation audit trail.

--- MODULE 8: COMMUNITY REVIEWS & Q&A ---
33. GET /api/v1/reviews/mosque/{mosqueId} (Public) - List paginated reviews and average rating.
34. POST /api/v1/reviews/mosque/{mosqueId} (Authenticated) - Submit 1-5 star review.
35. DELETE /api/v1/reviews/{reviewId} (Role: AUTHOR or MODERATOR) - Delete review.
36. GET /api/v1/questions/mosque/{mosqueId} (Public) - List Q&A threads for a mosque.
37. POST /api/v1/questions/mosque/{mosqueId} (Authenticated) - Ask a community question.
38. POST /api/v1/answers/question/{questionId} (Authenticated) - Post an answer.
39. PUT /api/v1/answers/{answerId}/accept (Role: MOSQUE_ADMIN) - Mark answer as officially verified.
40. POST /api/v1/community/flags (Authenticated) - Report spam or inappropriate content.
41. GET /api/v1/community/flags/pending (Role: MODERATOR) - List reported content queue.
42. PUT /api/v1/community/flags/{id}/resolve (Role: MODERATOR) - Resolve content flag.

--- MODULE 9: EVENTS & FRIDAY KHUTBAHS ---
43. GET /api/v1/events/mosque/{mosqueId} (Public) - List upcoming mosque events and announcements.
44. POST /api/v1/events/mosque/{mosqueId} (Role: MOSQUE_ADMIN) - Create a new event.
45. DELETE /api/v1/events/{eventId} (Role: MOSQUE_ADMIN) - Cancel / delete an event.
46. GET /api/v1/khutbahs/mosque/{mosqueId} (Public) - List Friday Khutbah recordings and live streams.
47. POST /api/v1/khutbahs/mosque/{mosqueId} (Role: MOSQUE_ADMIN) - Publish Friday sermon recording.

--- MODULE 10: MEDIA & INGESTION ---
48. POST /api/v1/media/upload (Authenticated) - Upload image or verification PDF file.
49. POST /api/v1/ingestion/osm (Role: SUPER_ADMIN) - Trigger automated Overpass OpenStreetMap sync for a bounding box.

================================================================================
10. DEVOPS, CLOUD DEPLOYMENT & DATABASE INFRASTRUCTURE (NEON + RENDER)
================================================================================
10.1. Neon Serverless PostgreSQL Configuration:
- Connection Mode: PostgreSQL 16 with pooling (sslmode=require).
- Geographic Region: AWS ap-southeast-1 (Singapore) for low latency.
- Hibernate Dialect: org.hibernate.dialect.PostgreSQLDialect with spatial mapping.
- Auto-DDL Disabled: Flyway handles all production migrations (ddl-auto: validate).

10.2. Production Multi-Stage Dockerfile:
- Stage 1 (Builder): maven:3.9.6-eclipse-temurin-17-alpine compiles and packages fat JAR with dependency caching.
- Stage 2 (Runner): eclipse-temurin:17-jre-alpine runs the optimized fat JAR under a non-root security context.

10.3. Render Blueprint (render.yaml):
- Service Type: Docker Web Service.
- Plan: Free / Starter.
- Port: 8080.
- Automatic environment variable bindings for SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, and SPRING_DATASOURCE_PASSWORD.

================================================================================
11. TEST COVERAGE & QUALITY ASSURANCE
================================================================================
The backend contains 9 comprehensive automated test suites in src/test/java/com/openmosque:

1. OpenMosqueApplicationTests.java: Context loading & Spring bean initialization sanity.
2. MosqueServiceTests.java: PostGIS spatial calculations, distance assertions, and facility filtering.
3. PrayerTimesServiceTests.java: Calculation methods, Hanafi vs Standard Asr, fixed vs offset Iqamah schedules.
4. CommunityReviewAndQAServiceTests.java: Review ratings, moderation flags, Q&A threads, and accepted answer lifecycle.
5. OsmIngestionServiceTests.java: Overpass API mock responses, bounding box parsing, and 50m spatial deduplication.
6. MediaUploadServiceTests.java: MIME type whitelist enforcement, file size boundaries, and storage safety.
7. MosqueEventServiceTests.java: Event scheduling, chronological sorting, and Khutbah media links.
8. FavoritesAndBadgesTests.java: User bookmarking, contributor point accumulation, and badge tier promotions.
9. NotificationAndMediaTests.java: In-app notification delivery and read status updates.

Test Profile Strategy:
- application-test.yml runs an in-memory H2 database in PostgreSQL mode (MODE=PostgreSQL) with dev-mock-auth: true to enable deterministic, lightning-fast test execution without internet connectivity or external DB dependencies.

================================================================================
12. AI REVIEWER CHECKLIST & CODE AUDIT PROMPTS
================================================================================
When reviewing this backend as an AI agent, please focus your audit on the following critical dimensions:

1. Spatial Index Utilization:
   - Verify that all spatial queries on mosques.location leverage the GIST index and use ST_DWithin on geography rather than non-indexed Euclidean calculations.
2. Security & RBAC Enforcement:
   - Check that @PreAuthorize or manual role checks strictly verify user permissions before executing administrative operations (especially Iqamah updates, claim approvals, and moderation decisions).
3. Concurrency & Optimistic Locking:
   - Inspect update operations on mosque_iqamah_schedules and mosque_submissions to guarantee thread-safe state transitions.
4. Idempotency of User Sync:
   - Ensure POST /api/v1/users/sync safely handles duplicate concurrent calls without causing unique constraint violations on firebase_uid or email.
5. Error Handling & Payload Contracts:
   - Verify all exceptions are intercepted by GlobalExceptionHandler and return standardized ApiResponse<T> envelopes with appropriate HTTP status codes (400, 401, 403, 404, 500).

--- END OF SPECIFICATION ---
