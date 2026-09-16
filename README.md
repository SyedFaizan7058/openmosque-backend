# 🕌 OpenMosque Backend API

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6.3.3-6DB33F?logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![PostGIS](https://img.shields.io/badge/PostGIS-3.3+-00758F?logo=postgresql&logoColor=white)](https://postgis.net/)
[![Redis](https://img.shields.io/badge/Redis-7.0+-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![Firebase](https://img.shields.io/badge/Firebase_Admin-9.3.0-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com/)
[![OpenAPI 3.0](https://img.shields.io/badge/OpenAPI-3.0-85EA2D?logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui.html)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

**OpenMosque Backend** is an enterprise-grade, geospatial REST API service for global mosque discovery, real-time prayer calculations, crowdsourced contributions, community governance, and verified mosque administration.

Frontend Repository: [SyedFaizan7058/openmosque-frontend](https://github.com/SyedFaizan7058/openmosque-frontend)

---

## 🌟 Architecture & Core Capabilities

### 🌍 PostGIS Geospatial Engine
- **Proximity Queries**: High-performance spatial indexing using `ST_DWithin` and `ST_DistanceSphere` over PostGIS geography geometries.
- **Dynamic Radius Filtering**: Discovers mosques within arbitrary radii (1–50 km) while filtering by facility codes (e.g., `WOMENS_SECTION`, `PARKING`).
- **OpenStreetMap Bulk Ingestion**: Overpass API integration to ingest mosques across entire cities or bounding boxes into PostgreSQL.

### 🕋 Astronomical Prayer Calculation & Custom Iqamah Schedules
- **Multi-Method Engine**: Integrated astronomical calculation supporting 14 international conventions (ISNA, MWL, Makkah, Egypt, Karachi, etc.).
- **Live Fiqh Timings**: Real-time evaluation of Ishraaq, Chaasht, Zawaal, Sunset, Iftaar, Tahajjud, and Sahoor End times.
- **Mosque Administrator Customization**: Mosque admins can configure custom Adhan minute offsets and configure daily congregational Iqamah times (fixed time or offset after Adhan).

### 🛡️ Multi-Tier Security & Authentication
- **Firebase Authentication Sync**: Validates Firebase JWT tokens and synchronizes user profiles into PostgreSQL.
- **HttpOnly Cookie Session**: Issues SameSite=Lax HttpOnly session cookies for browser clients with automatic Bearer token fallback.
- **Two-Factor Authentication (2FA)**: Built-in TOTP engine with QR code generation and single-use emergency recovery codes.
- **Defense-in-Depth RBAC**: 5 distinct permission levels:
  - `PUBLIC`: Unauthenticated read access (discovery, prayer times, reviews, facilities).
  - `USER`: Authenticated user actions (reviews, Q&A, mosque submissions, ownership claims, favorites).
  - `MOSQUE_ADMIN`: Mosque-specific management (prayer config, Iqamah times, events, Friday Khutbahs).
  - `MODERATOR`: Platform-wide content moderation, crowdsource reviews, and claim approval.
  - `SUPER_ADMIN`: User role assignment, global analytics, and administrative controls.
- **Rate Limiting & Hardened Headers**: Token Bucket rate-limiting filter with HSTS, CSP, X-Content-Type-Options, X-Frame-Options, and Referrer policies.

### 🔔 Notifications & Real-Time Push Engine
- **In-App Notification Center**: Tracks submission decisions, claim approvals, badge achievements, and community responses.
- **Firebase Cloud Messaging (FCM)**: Dispatches push notifications to registered Web, Android, and iOS devices.
- **Sidebar Badges**: Real-time moderation count API (`GET /api/v1/admin/moderation/counts`) displaying pending Submissions, Claims, and Flags.

---

## 📊 API Surface Overview

| Module | Endpoints | Primary Function |
|---|---|---|
| **Documentation & Health** | 4 | OpenAPI 3.0 spec, Swagger UI dashboard, Actuator health & info |
| **User & Profile** | 9 | Firebase sync, `/users/me` profile, preferred GPS location, session cookies |
| **Two-Factor Authentication** | 6 | TOTP status, setup, verify, enable, disable, backup code regeneration |
| **Facilities Catalog** | 1 | Active facility/amenity codes (Wudu, Parking, Wheelchair, etc.) |
| **Mosque Directory** | 5 | Geospatial radius search, keyword/city filters, full profiles |
| **Favorites & Bookmarks** | 4 | Add/remove bookmarks, list favorites, favorite status check |
| **Prayer & Iqamah** | 6 | Public prayer times, calculation methods, admin prayer & Iqamah configs |
| **Events & Khutbahs** | 9 | Upcoming events, Friday Jumu'ah schedules, mosque admin CRUD |
| **Community & Reviews** | 14 | 5-star category ratings, user reviews, Q&A forum, content flagging |
| **Crowdsourcing** | 2 | Submit new mosque listing (+100 pts), suggest profile edits (+50 pts) |
| **Mosque Claiming** | 3 | Trustee ownership claims with 5MB proof upload, admin claim review |
| **Media & Asset Storage** | 4 | Pre-signed upload URLs, direct multipart 5MB uploads, local storage sink |
| **Notifications & Push** | 7 | In-app alerts, unread counts, mark read, FCM device token registration |
| **OSM Bulk Ingestion** | 3 | Overpass API ingestion by city, radius, or bounding box |
| **Content Moderation** | 5 | Live moderation counts, submission queue, claim queue, flag queue |
| **Platform Analytics** | 2 | Mosque-level admin stats, platform-wide ecosystem analytics |
| **User Administration** | 2 | Super Admin user search, pagination, and role promotion/demotion |
| **Total** | **91** | **All 91 endpoints verified across 24 REST controllers** |

For the complete, line-by-line endpoint catalog with request/response schemas and authorization requirements, see [`all_api_urls.txt`](./all_api_urls.txt).

---

## 📁 Repository Structure

```
D:/Open-Mosque/
├── src/
│   ├── main/
│   │   ├── java/com/openmosque/
│   │   │   ├── common/             # Global ApiResponse, PageResponse, Exceptions
│   │   │   ├── config/             # Redis, OpenAPI, WebMvc configuration
│   │   │   ├── modules/            # Domain-driven feature modules
│   │   │   │   ├── claim/          # Mosque ownership claims & admin review
│   │   │   │   ├── community/      # Reviews, category ratings, Q&A, flags
│   │   │   │   ├── event/          # Community events & Friday Khutbahs
│   │   │   │   ├── ingestion/      # OpenStreetMap Overpass bulk ingestion
│   │   │   │   ├── media/          # Pre-signed upload URLs & multipart upload
│   │   │   │   ├── moderation/     # Submission queues & live badge counts
│   │   │   │   ├── mosque/         # PostGIS radius search, directory, favorites
│   │   │   │   ├── notification/   # Notification center & FCM push dispatch
│   │   │   │   ├── prayer/         # Prayer calculations & Iqamah schedules
│   │   │   │   └── user/           # User profiles, 2FA, badges, role administration
│   │   │   └── security/           # SecurityConfig, FirebaseAuthFilter, Rate Limiting
│   │   └── resources/
│   │       ├── application.yml     # Spring Boot configuration
│   │       └── db/migration/       # Flyway database migration scripts
│   └── test/                       # Unit and integration test suites
├── all_api_urls.txt                # Complete 91-endpoint API directory
├── documentation.md                # 42-section technical architecture manual
├── pom.xml                         # Maven dependencies and build configuration
└── .gitignore                      # Hardened git ignore rules
```

---

## 🚀 Getting Started

### Prerequisites
- **Java**: `JDK 17` or higher
- **Maven**: `3.8+` (or use included `./mvnw`)
- **PostgreSQL**: `15+` with `PostGIS 3.3+` extension enabled
- **Redis**: `7.0+` (for caching prayer times and rate limiting)
- **Firebase Project**: Service account credentials for Firebase Authentication & FCM

### 1. Database Setup
Create the PostgreSQL database and enable PostGIS:
```sql
CREATE DATABASE openmosque;
\c openmosque;
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

### 2. Configure Environment Variables
Set the required environment variables (or configure in `application-local.yml`):
```bash
# Database Configuration
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/openmosque
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_postgres_password

# Redis Configuration
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_DATA_REDIS_PORT=6379

# Firebase Admin SDK Credentials
export FIREBASE_CREDENTIALS_PATH=file:/path/to/firebase-service-account.json

# CORS Configuration
export APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

### 3. Build the Application
```bash
mvn clean package -DskipTests
```

### 4. Run the Application
```bash
mvn spring-boot:run
```
The server will start on `http://localhost:8080`.

---

## 📖 Interactive API Documentation
Once running, explore and test all APIs interactively:
- **Swagger UI Dashboard**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI 3.0 Raw JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Health Endpoint**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 🔒 Security Hardening
- **Stateless JWT + HttpOnly Cookies**: Token verification via Firebase Admin SDK with secure SameSite cookies.
- **Zero Secrets Tracked**: All credentials, keystores, and service-account files are barred from version control via `.gitignore`.
- **Pre-Signed Uploads**: Upload URLs expire after a short duration; file uploads enforce strict 5 MB limits and MIME type whitelisting.

---

## 📄 License
This project is licensed under the **MIT License**.
