# OpenMosque Backend — Complete Technical Architecture & Implementation Documentation

**Platform:** OpenMosque Global Platform  
**Architecture:** Micro-Modular Monolith (Clean Architecture)  
**Backend Framework:** Spring Boot 3.3.3 (Java 17)  
**Database:** PostgreSQL 16/18 + PostGIS Spatial Engine  
**Authentication & Security:** Firebase Admin SDK + Spring Security 6 (Stateless JWT)  
**Documentation Version:** 5.0 (Phases 1, 2, 3, 4 & OpenStreetMap Automated Ingestion Engine Completed)  

---

## Table of Contents
1. [Executive Summary & Problem Statement](#1-executive-summary--problem-statement)
2. [High-Level Architecture & Tech Stack](#2-high-level-architecture--tech-stack)
3. [Authentication, Security & Authorization Pipeline](#3-authentication-security--authorization-pipeline)
4. [User Roles & Promotion Workflows (Way A & Way B)](#4-user-roles--promotion-workflows-way-a--way-b)
5. [Data Storage & PostGIS Geospatial Engine](#5-data-storage--postgis-geospatial-engine)
6. [Anti-Boilerplate Architecture & Code Reuse Patterns](#6-anti-boilerplate-architecture--code-reuse-patterns)
7. [End-to-End Functional Flows & Diagrams](#7-end-to-end-functional-flows--diagrams)
   - [7.1. User Registration & Sync Flow](#71-user-registration--sync-flow)
   - [7.2. PostGIS Nearby Discovery & Spatial Search Flow](#72-postgis-nearby-discovery--spatial-search-flow)
   - [7.3. Crowdsourcing Submissions & Moderation Flow](#73-crowdsourcing-submissions--moderation-flow)
   - [7.4. Mosque Claim & Committee Verification Flow](#74-mosque-claim--committee-verification-flow)
8. [Database Schema & ERD](#8-database-schema--erd)
9. [Complete REST API Reference Directory (17 Endpoints)](#9-complete-rest-api-reference-directory-17-endpoints)
10. [Local Setup, Swagger & Testing Guide](#10-local-setup-swagger--testing-guide)

---

## 1. Executive Summary & Problem Statement
**OpenMosque** is an open-source, community-driven platform designed to eliminate the fragmentation of mosque directories, prayer timetables, and Islamic community services worldwide.

### Core Mission & Key Highlights:
* **Accurate & Verified Directory**: Crowdsourced discovery verified by community moderators.
* **Geospatial Proximity Search**: Sub-millisecond radius searches powered by native PostGIS spatial indexes (`ST_DWithin`, `ST_DistanceSphere`).
* **Islamic Amenities Catalog**: Predefined filters for Wudu areas, dedicated Women's sections, wheelchair accessibility, parking, and Halal services.
* **Decoupled Role-Based Security**: Zero password storage on backend using Firebase Auth JWTs.
* **Mosque Administration Portal**: Verification workflow for Imams and trustees to claim official control over their mosque profiles.
* **Gamification & Rewards**: Points and contributor badges rewarded for verified mosque submissions and updates.

---

## 2. High-Level Architecture & Tech Stack

```
 [ React Web / React Native Mobile Apps ]
                    │
                    │ (HTTPS + Firebase ID Token)
                    ▼
     ┌─────────────────────────────┐
     │      Spring Security 6      │ ◄─── Validates Firebase JWT Token
     └──────────────┬──────────────┘
                    │
   ┌────────────────┼──────────────────────────────┬──────────────────────────────┐
   │                │                              │                              │
   ▼                ▼                              ▼                              ▼
[Users & RBAC]  [Mosque Directory]         [Crowdsource Queue]            [Mosque Claims]
Module          Module (PostGIS)           Module (Rewards)               Module (Admin Upgrade)
   │                │                              │                              │
   └────────────────┴──────────────┬───────────────┴──────────────────────────────┘
                                   │
                                   ▼
                    [ PostgreSQL 16/18 + PostGIS ]
```

### Component Details:
* **Language & Runtime:** Java 17 LTS.
* **Core Framework:** Spring Boot 3.3.3 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`).
* **Database & Spatial Engine:** PostgreSQL 16/18 + PostGIS (`hibernate-spatial 6.5.2.Final`, `org.locationtech.jts`).
* **Authentication:** Firebase Admin SDK (Google Identity Platform).
* **Database Migrations:** Flyway (`flyway-core`, `flyway-database-postgresql`).
* **DTO Mapping & Code Generation:** MapStruct 1.5.5 + Project Lombok (`@SuperBuilder`).
* **API Documentation:** SpringDoc OpenAPI 3.0 (`/swagger-ui.html`).

---

## 3. Authentication, Security & Authorization Pipeline

### A. How Passwords & User Data Are Stored Safely
* **Zero Password Storage on Backend**: User passwords **never touch our database**. All password salting, hashing (hardened Scrypt/Bcrypt), and social logins (Google, Apple) occur entirely on Google's hardened identity infrastructure.
* **Stateless JWT Flow**: When a user logs in, Firebase returns a signed ID Token. The client sends this token in the `Authorization: Bearer <token>` header.

### B. Request Interception (`FirebaseAuthFilter`)
1. The request reaches `FirebaseAuthFilter`.
2. `FirebaseTokenVerifier` cryptographically verifies the token.
3. The filter queries PostgreSQL for the user by `firebase_uid`. If the user is new, it automatically provisions a new `User` record with role `USER`.
4. It wraps the user in `CustomUserDetails` and sets the Spring Security `SecurityContextHolder`.

### C. Declarative Controller Injection (`@CurrentUser`)
Our custom `CurrentUserArgumentResolver` automatically injects the authenticated `User` entity into any controller parameter annotated with `@CurrentUser`:
```java
@GetMapping("/me")
public ResponseEntity<ApiResponse<UserResponseDto>> getProfile(@CurrentUser User user) {
    return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUserProfile(user)));
}
```

---

## 4. User Roles & Promotion Workflows (Way A & Way B)

### The 4 System Roles:
1. **`USER`**: Default role for any registered user. Can browse mosques, submit proposals, and suggest corrections.
2. **`MOSQUE_ADMIN`**: Verified Imam or committee member. Can manage their mosque profile, edit Iqamah schedules, and broadcast live streams.
3. **`MODERATOR`**: Trusted community reviewer. Can approve/reject crowdsourced mosque submissions and verify mosque claim requests.
4. **`SUPER_ADMIN`**: Full platform authority. Can change user roles, manage system configurations, and oversee all operations.

```
                    ┌──────────────────────────────┐
                    │      New User (USER)         │
                    └──────────────┬───────────────┘
                                   │
            ┌──────────────────────┴──────────────────────┐
            │                                             │
            ▼ (Way A: Super Admin Promotion)               ▼ (Way B: Mosque Claim Request)
┌──────────────────────────────┐              ┌──────────────────────────────┐
│ PATCH /api/v1/admin/users/   │              │ POST /api/v1/mosques/{id}/   │
│ {id}/role                    │              │ claim (Sends proof)          │
└──────────────┬───────────────┘              └──────────────┬───────────────┘
               │                                             │
               │                                             ▼
               │                              ┌──────────────────────────────┐
               │                              │ Moderator Reviews & Approves │
               │                              └──────────────┬───────────────┘
               │                                             │
               ▼                                             ▼
┌──────────────────────────────┐              ┌──────────────────────────────┐
│  Promoted to MODERATOR or    │              │ Upgraded to MOSQUE_ADMIN     │
│  SUPER_ADMIN                 │              │ (Mosque marked Verified)     │
└──────────────────────────────┘              └──────────────────────────────┘
```

* **Way A (Super Admin API)**: `PATCH /api/v1/admin/users/{id}/role` enables Super Admins to promote or demote any user instantly.
* **Way B (Mosque Claim Flow)**: `POST /api/v1/mosques/{id}/claim` allows committee members to upload proof documents. Once approved by a moderator via `PATCH /api/v1/admin/mosques/claims/{id}/decision`, the system automatically promotes them to `MOSQUE_ADMIN` and marks the mosque as verified.

---

## 5. Data Storage & PostGIS Geospatial Engine

### A. Coordinate Representation
Coordinates are stored as native **`GEOMETRY(Point, 4326)`** columns with a **GIST Spatial Index** (`idx_mosques_location`).
* **SRID 4326 (WGS 84)** is the global standard GPS coordinate system.
* In JTS geometry coordinates: `Point(x = Longitude, y = Latitude)`.

### B. High-Performance Radius Queries
```sql
SELECT m.*
FROM mosques m
WHERE m.status = 'ACTIVE' 
  AND m.is_deleted = false
  AND ST_DWithin(
        m.location::geography, 
        ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 
        :radiusMeters
      )
ORDER BY ST_DistanceSphere(m.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) ASC;
```
* **`ST_DWithin`**: Filters candidate mosques on the spherical earth surface using spatial tree bounding boxes without scanning the entire table.
* **`ST_DistanceSphere`**: Computes accurate spherical distances in meters and kilometers.

---

## 6. Anti-Boilerplate Architecture & Code Reuse Patterns

1. **`BaseEntity` Superclass**: Centralizes UUID generation (`@GeneratedValue(strategy = GenerationType.UUID)`), automated auditing (`createdAt`, `updatedAt`), and soft-deletion (`is_deleted`) across all domain entities.
2. **Universal Response Envelope (`ApiResponse<T>`)**: Standardizes all JSON payloads (`success`, `message`, `data`, `error`, `timestamp`).
3. **Generic Pagination Wrapper (`PageResponse<T>`)**: Wraps Spring Data pagination metadata with zero boilerplate.
4. **Unified Exception Hierarchy & Handler (`GlobalExceptionHandler`)**: Catches 404, 400, 401, 403, 409, and field validation errors with zero controller `try-catch` blocks.

---

## 7. End-to-End Functional Flows & Diagrams

### 7.1. User Registration & Sync Flow
```
[Frontend] ──(1. Login)──► [Firebase Auth] ──(2. Returns JWT)──► [Frontend]
                                                                     │
[Frontend] ──(3. POST /api/v1/users/sync with JWT)───────────────────► [Backend]
                                                                     │
                                                            (4. Upsert User in DB)
                                                                     │
[Frontend] ◄──(5. 200 OK + User UUID, Role: USER, Points: 0)─────────┘
```

### 7.2. PostGIS Nearby Discovery & Spatial Search Flow
```
[User GPS: 51.5173, -0.0658] ──► GET /api/v1/mosques/nearby?latitude=51.5173&longitude=-0.0658&radiusKm=10&facilities=PARKING
                                                  │
                                       (PostGIS Spatial Query)
                                                  │
                                                  ▼
                     [Filter Active Mosques within Radius -> Calculate Distance (km)]
                                                  │
                                                  ▼
[Frontend] ◄── Returns Sorted List of Mosques with Facilities, Distance (km) & Cover Photos
```

### 7.3. Crowdsourcing Submissions & Moderation Flow
```
[Contributor] ──► POST /api/v1/mosques/submissions (Submits Mosque details)
                            │
                   (Status: PENDING)
                            │
                            ▼
[Moderator] ──► GET /api/v1/admin/moderation/submissions?status=PENDING
                            │
               (Review Address & Images)
                            │
[Moderator] ──► PATCH /api/v1/admin/moderation/submissions/{id}/decision (APPROVED)
                            │
               ┌────────────┴────────────┐
               ▼                         ▼
    [Publish Mosque Entry]     [Reward Contributor +100 Points]
    (Status: ACTIVE)           (UserService.rewardPoints)
```

### 7.4. Mosque Claim & Committee Verification Flow
```
[Imam / Trustee] ──► POST /api/v1/mosques/{id}/claim (Submits Proof Document)
                                  │
                         (Status: PENDING)
                                  │
                                  ▼
[Moderator] ──► GET /api/v1/admin/mosques/claims?status=PENDING
                                  │
                     (Review Official Document)
                                  │
[Moderator] ──► PATCH /api/v1/admin/mosques/claims/{id}/decision (APPROVED)
                                  │
               ┌──────────────────┴──────────────────┐
               ▼                                     ▼
    [Upgrade Role: MOSQUE_ADMIN]           [Mark Mosque: is_verified = true]
```

---

## 8. Database Schema & ERD

### All 17 Database Tables:
1. **`users`**: `id`, `firebase_uid`, `email`, `display_name`, `phone_number`, `photo_url`, `role`, `points`, `is_active`, `is_verified`, `created_at`, `updated_at`.
2. **`facilities`**: `id`, `code` (`WUDU_AREA`, `WOMENS_SECTION`, `PARKING`, etc.), `name`, `description`, `icon_name`, `is_active`.
3. **`mosques`**: `id`, `name`, `slug`, `description`, `address`, `city`, `state`, `country`, `postal_code`, `latitude`, `longitude`, `location` (`Point 4326`), `contact_phone`, `contact_email`, `website_url`, `live_stream_url`, `is_verified`, `status`, `created_by`.
4. **`mosque_facilities`**: `id`, `mosque_id`, `facility_id`, `custom_details`.
5. **`mosque_images`**: `id`, `mosque_id`, `image_url`, `caption`, `is_cover`, `display_order`.
6. **`mosque_submissions`**: `id`, `submitter_id`, `target_mosque_id`, `submission_type`, `status`, `name`, `address`, `latitude`, `longitude`, `reviewer_id`, `review_comments`.
7. **`submission_facility_codes` & `submission_image_urls`**: Auxiliary tables for submission amenities and images.
8. **`moderation_logs`**: `id`, `submission_id`, `moderator_id`, `action`, `notes`, `created_at`.
9. **`mosque_claim_requests`**: `id`, `mosque_id`, `claimant_id`, `full_name`, `phone_number`, `official_email`, `position_in_mosque`, `proof_document_url`, `status`, `reviewer_id`, `review_comments`.
10. **`mosque_prayer_configs`**: `id`, `mosque_id`, `calculation_method`, `juristic_school`, `time_zone`, `fajr_angle`, `isha_angle`, `created_at`, `updated_at`.
11. **`mosque_iqamah_schedules`**: `id`, `mosque_id`, `fajr_type`, `fajr_offset_minutes`, `fajr_fixed_time`, `dhuhr_type`, `dhuhr_offset_minutes`, `dhuhr_fixed_time`, `asr_type`, `asr_offset_minutes`, `asr_fixed_time`, `maghrib_type`, `maghrib_offset_minutes`, `maghrib_fixed_time`, `isha_type`, `isha_offset_minutes`, `isha_fixed_time`, `jummah1_time`, `jummah2_time`, `jummah_khutbah_language`, `created_at`, `updated_at`.
12. **`mosque_events`**: `id`, `mosque_id`, `title`, `description`, `event_type`, `audience`, `start_date_time`, `end_date_time`, `location_details`, `speaker_name`, `banner_image_url`, `registration_url`, `is_cancelled`, `created_by_user_id`, `created_at`, `updated_at`.
13. **`mosque_khutbahs`**: `id`, `mosque_id`, `khutbah_date`, `topic`, `khatib_name`, `batch_number`, `khutbah_time`, `adhaan_time`, `iqamah_time`, `language`, `stream_url`, `recording_url`, `notes`, `created_at`, `updated_at`.
14. **`mosque_reviews`**: `id`, `mosque_id`, `user_id`, `rating_overall`, `rating_cleanliness`, `rating_facilities`, `rating_womens_area`, `rating_parking`, `review_text`, `status`, `created_at`, `updated_at`.
15. **`mosque_questions`**: `id`, `mosque_id`, `user_id`, `question_text`, `status`, `created_at`, `updated_at`.
16. **`mosque_answers`**: `id`, `question_id`, `user_id`, `answer_text`, `is_official_mosque_admin`, `status`, `created_at`, `updated_at`.
17. **`community_content_flags`**: `id`, `target_type`, `target_id`, `reporter_id`, `reason`, `status`, `reviewer_id`, `reviewer_notes`, `created_at`, `updated_at`.

---

## 9. Complete REST API Reference Directory (45 Endpoints)

| # | Method | URL Path | Access Level | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| 1 | **GET** | `/v3/api-docs` | Public | OpenAPI 3.0 specification JSON |
| 2 | **GET** | `/swagger-ui.html` | Public | Interactive Swagger API testing UI |
| 3 | **POST** | `/api/v1/users/sync` | Public | Synchronize Firebase user to DB |
| 4 | **GET** | `/api/v1/users/me` | Authenticated | Get logged-in user profile & points |
| 5 | **GET** | `/api/v1/facilities` | Public | Get all active standard Islamic amenities |
| 6 | **GET** | `/api/v1/mosques/nearby` | Public | PostGIS GPS radius search |
| 7 | **GET** | `/api/v1/mosques/search` | Public | Paginated search by name, city, country |
| 8 | **GET** | `/api/v1/mosques/{idOrSlug}` | Public | Full mosque profile, photos & broadcast link |
| 9 | **POST** | `/api/v1/mosques/submissions` | Authenticated | Crowdsource new mosque (+100 pts on approval) |
| 10 | **POST** | `/api/v1/mosques/{id}/suggest-edit` | Authenticated | Suggest corrections to mosque (+50 pts) |
| 11 | **POST** | `/api/v1/mosques/{id}/claim` | Authenticated | Apply for Mosque Admin ownership (Way B) |
| 12 | **GET** | `/api/v1/admin/mosques/claims` | Moderator/Admin | View pending mosque claims queue |
| 13 | **PATCH**| `/api/v1/admin/mosques/claims/{id}/decision` | Moderator/Admin | Approve claim (Promotes to `MOSQUE_ADMIN`) |
| 14 | **PATCH**| `/api/v1/admin/users/{id}/role` | Super Admin | Directly change user role (Way A) |
| 15 | **GET** | `/api/v1/admin/users` | Super Admin | Paginated user search with role filter |
| 16 | **GET** | `/api/v1/admin/moderation/submissions` | Moderator/Admin | View pending mosque submissions queue |
| 17 | **PATCH**| `/api/v1/admin/moderation/submissions/{id}/decision` | Moderator/Admin | Approve/Reject submission & award points |
| 18 | **GET** | `/api/v1/mosques/{idOrSlug}/prayer-times` | Public | Astronomical Adhan times, Iqamah overrides & live countdown |
| 19 | **GET** | `/api/v1/prayer-times/methods` | Public | List all supported calculation methods (ISNA, MWL, etc.) |
| 20 | **GET** | `/api/v1/mosque-admin/mosques/{id}/prayer-config` | Mosque Admin/Super Admin | Retrieve mosque calculation method, school & timezone |
| 21 | **PUT** | `/api/v1/mosque-admin/mosques/{id}/prayer-config` | Mosque Admin/Super Admin | Update calculation method & school (Hanafi/Standard) |
| 22 | **GET** | `/api/v1/mosque-admin/mosques/{id}/iqamah-schedule` | Mosque Admin/Super Admin | Retrieve Iqamah congregation rules & Friday Jumu'ah batches |
| 23 | **PUT** | `/api/v1/mosque-admin/mosques/{id}/iqamah-schedule` | Mosque Admin/Super Admin | Configure Iqamah offsets/fixed times & Friday Jumu'ah batches |
| 24 | **POST**| `/api/v1/media/upload-url` | Authenticated | Generate pre-signed upload URL for direct cloud storage |
| 25 | **PUT** | `/api/v1/media/mock-upload` | Public | Dev mock direct upload destination |
| 26 | **GET** | `/api/v1/mosques/{idOrSlug}/events` | Public | List upcoming mosque events & programs with filters |
| 27 | **GET** | `/api/v1/events/{id}` | Public | Full details of a specific event |
| 28 | **GET** | `/api/v1/mosques/{idOrSlug}/khutbahs` | Public | Upcoming Friday Khutbah topics, times & live stream links |
| 29 | **POST**| `/api/v1/mosque-admin/mosques/{id}/events` | Mosque Admin/Super Admin | Schedule new mosque event or halaqah |
| 30 | **PUT** | `/api/v1/mosque-admin/mosques/{id}/events/{eventId}` | Mosque Admin/Super Admin | Update event details or timing |
| 31 | **DELETE**| `/api/v1/mosque-admin/mosques/{id}/events/{eventId}` | Mosque Admin/Super Admin | Cancel / soft-delete scheduled event |
| 32 | **POST**| `/api/v1/mosque-admin/mosques/{id}/khutbahs` | Mosque Admin/Super Admin | Publish upcoming Friday Khutbah schedule |
| 33 | **PUT** | `/api/v1/mosque-admin/mosques/{id}/khutbahs/{khutbahId}` | Mosque Admin/Super Admin | Update Friday Khutbah schedule |
| 34 | **DELETE**| `/api/v1/mosque-admin/mosques/{id}/khutbahs/{khutbahId}` | Mosque Admin/Super Admin | Remove scheduled Friday Khutbah entry |
| 35 | **GET** | `/api/v1/mosques/{idOrSlug}/reviews` | Public | Paginated community reviews & category breakdown |
| 36 | **GET** | `/api/v1/mosques/{id}/ratings` | Public | Aggregated category rating averages |
| 37 | **GET** | `/api/v1/mosques/{idOrSlug}/questions` | Public | Community Q&A threads with answers |
| 38 | **POST**| `/api/v1/mosques/{id}/reviews` | Authenticated | Submit mosque review and 1-5 star category ratings |
| 39 | **PUT** | `/api/v1/mosques/{id}/reviews/{reviewId}` | Authenticated | Update author's existing review |
| 40 | **DELETE**| `/api/v1/mosques/{id}/reviews/{reviewId}` | Authenticated | Delete author's existing review |
| 41 | **POST**| `/api/v1/mosques/{id}/questions` | Authenticated | Ask a community question |
| 42 | **POST**| `/api/v1/community/questions/{questionId}/answers` | Authenticated | Answer a question (official badge if Imam) |
| 43 | **POST**| `/api/v1/community/flag` | Authenticated | Report inappropriate or abusive content |
| 44 | **GET** | `/api/v1/admin/community/flags` | Moderator/Admin | Moderation review queue for reported content |
| 45 | **PATCH**| `/api/v1/admin/community/flags/{id}/decision` | Moderator/Admin | Resolve/dismiss flag and hide violating content |
| 46 | **POST**| `/api/v1/admin/ingest/osm/city` | Moderator/Admin | Bulk-ingest all mosques in a city via OSM Overpass API |
| 47 | **POST**| `/api/v1/admin/ingest/osm/radius` | Moderator/Admin | Bulk-ingest mosques in a radius around GPS coordinate |
| 48 | **POST**| `/api/v1/admin/ingest/osm/bbox` | Moderator/Admin | Bulk-ingest mosques within a geographic bounding box |

---

## 10. Local Setup, Swagger & Testing Guide

### 1. Prerequisites
* Java 17 LTS (`jdk-17`)
* PostgreSQL 16 or 18 with PostGIS extension (`CREATE EXTENSION postgis;`)
* Maven 3.9+

### 2. Running Locally
```bash
# Start backend on port 8080
mvn spring-boot:run
```

### 3. Running Automated Test Suites
```bash
mvn clean test
```
* Executed **25/25 automated unit and integration tests** across 7 test suites verifying geospatial calculations, crowdsourcing submissions, moderator approval points, prayer timetable calculations, media pre-signed URLs, events/khutbahs, reviews/Q&A, and OpenStreetMap Overpass automated ingestion with **0 failures and 0 errors**.
