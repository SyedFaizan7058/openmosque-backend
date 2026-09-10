# Product Requirements Document (PRD)
# OpenMosque Web Frontend Platform

**Document Version:** 1.0.0  
**Project:** OpenMosque Global Platform  
**Target Platform:** Web (Desktop, Tablet, Mobile Responsive PWA)  
**Backend Compatibility:** OpenMosque REST API v1.0.0 (Spring Boot 3 + PostgreSQL/PostGIS)  
**Status:** Approved for Implementation  

---

## 1. Executive Summary & Product Vision

### 1.1. Product Vision
**OpenMosque Web** is an open-source, community-driven global web platform engineered to eliminate the fragmentation of mosque directories, prayer timetables, Islamic amenities, and local community broadcasts worldwide.

### 1.2. Core Objectives
1. **Effortless Discovery**: Deliver sub-second geospatial discovery of nearby mosques with real-time GPS positioning, interactive maps, and amenity filters.
2. **Crowdsourcing & Verification**: Enable worshippers worldwide to propose new mosques and suggest corrections, rewarded through contributor gamification.
3. **Official Mosque Administration**: Provide Imams and mosque trustees a dedicated portal to manage Iqamah prayer times, broadcasts, and verified profiles.
4. **Community Moderation**: Empower trusted moderators with an intuitive queue to review submissions, inspect photos, and maintain directory integrity.

---

## 2. Target User Personas

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             OpenMosque Personas                             │
├───────────────────┬───────────────────┬───────────────────┬─────────────────┤
│ 1. Everyday       │ 2. Community      │ 3. Mosque Admin   │ 4. Platform     │
│    Worshipper     │    Contributor    │    (Imam/Trustee) │    Moderator    │
├───────────────────┼───────────────────┼───────────────────┼─────────────────┤
│ • Finds closest   │ • Adds unlisted   │ • Claims mosque   │ • Approves/     │
│   mosques         │   mosques         │   ownership       │   rejects new   │
│ • Checks next     │ • Suggests edit   │ • Updates daily   │   submissions   │
│   prayer & Iqamah │   corrections     │   Iqamah times    │ • Verifies      │
│ • Filters women's │ • Earns reward    │ • Embeds live     │   committee     │
│   section/parking │   points & badges │   khutbah stream  │   claim proofs  │
└───────────────────┴───────────────────┴───────────────────┴─────────────────┘
```

---

## 3. Recommended Frontend Tech Stack

| Layer | Technology | Rationale |
| :--- | :--- | :--- |
| **Framework** | **React 18+ (Vite) / Next.js 14** | Lightning fast HMR, component modularity, and SEO-friendly rendering. |
| **Language** | **TypeScript 5.x** | Strict typing across all backend DTOs and API responses. |
| **Styling & UI** | **Tailwind CSS 3.4 + Radix UI / shadcn/ui** | Clean, accessible, responsive design with effortless Dark/Light mode. |
| **Icons** | **Lucide React** | Consistent, lightweight SVG icon system. |
| **Maps & Geospatial** | **Leaflet + React-Leaflet + OpenStreetMap** | Open-source, zero API cost map engine with customizable pins and popups. |
| **State & Data Fetching** | **TanStack React Query v5 + Zustand** | Automatic cache invalidation, optimistic updates, and clean global auth store. |
| **HTTP Client** | **Axios with Interceptors** | Automatic Bearer JWT attachment and centralized 401/403/500 error toast handling. |
| **Authentication** | **Firebase Web SDK (v10) + Dev Mock Switcher** | Google Auth, Email/Password, Apple Sign-in with local dev mock token support. |

---

## 4. Information Architecture & Sitemap

```
OpenMosque Web App
├── 1. Home / Map Discovery (/)
│   ├── Interactive OpenStreetMap Canvas
│   ├── GPS Location Button ("Locate Me")
│   ├── Radius Slider (1 km - 50 km)
│   ├── Islamic Amenities Filter Drawer (Wudu, Women's Section, Parking, etc.)
│   └── Split View / Drawer: Mosque Cards List
├── 2. Mosque Search & Directory (/mosques)
│   ├── Keyword, City & Country Search Bar
│   ├── Paginated Results Grid
│   └── Verification & Status Badges
├── 3. Mosque Profile & Prayer Details (/mosques/:idOrSlug)
│   ├── Cover Photo Banner & Gallery Modal
│   ├── Next Prayer Countdown Clock
│   ├── Daily Prayer & Iqamah Timetable
│   ├── Facilities Checklist with Icons
│   ├── Google Maps / Apple Maps Navigation Link
│   ├── Live Khutbah YouTube/Stream Player
│   └── Actions: "Claim Mosque", "Suggest Edit", "Share"
├── 4. Crowdsource & Submissions (/contribute)
│   ├── "Add a New Mosque" Wizard (Location Pin Drop, Details, Facilities, Photos)
│   └── "Suggest Mosque Correction" Form
├── 5. Mosque Claim Portal (/mosques/:id/claim)
│   └── Trustee/Imam Proof Upload & Verification Request
├── 6. Moderator & Admin Dashboard (/admin)
│   ├── Pending Mosque Submissions Review Queue
│   ├── Mosque Claims Verification Queue
│   └── User Role Management Directory (Super Admin)
└── 7. User Profile & Gamification (/profile)
    ├── Account Settings & Role Badge
    ├── Points Ledger & Contributor Tier Badge (Bronze, Silver, Gold)
    └── My Submissions & Claim History
```

---

## 5. Detailed Feature Specifications

### 5.1. Module 1: Interactive Geospatial Discovery & Map Explorer (Core)
* **Live GPS Radius Search**:
  * Users can click `"Use My Current GPS"` or drag the map.
  * Dynamically queries `GET /api/v1/mosques/nearby?latitude={lat}&longitude={lng}&radiusKm={radius}&facilities={codes}`.
  * Mosque markers on Leaflet display custom green minaret pins.
  * Clicking a pin highlights the mosque card with distance in kilometers (`2.4 km away`).
* **Facility Badges Filter**:
  * Multi-select pills: `🕌 Wudu Area`, `🧕 Women's Section`, `🚗 Parking`, `♿ Wheelchair Accessible`, `📚 Library`, `❄️ Air Conditioning`.

### 5.2. Module 2: Mosque Detail Page & Prayer Timetable
* **Live Prayer Widget**:
  * Dynamic countdown clock: `"Asr in 42 mins (Iqamah: 5:15 PM)"`.
  * Table displaying: Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha, Jumu'ah.
* **Amenities & Details**:
  * Full contact info (Phone click-to-call, Website link, Email).
  * Verified checkmark badge for officially claimed mosques (`isVerified: true`).
  * Embedded YouTube live stream / Mixlr audio broadcast for Friday Khutbahs.
* **Photo Carousel**:
  * Lightbox modal viewing full resolution photos.

### 5.3. Module 3: Crowdsourcing & Submissions (User Flow)
* **Step-by-Step Submission Wizard**:
  1. **Step 1: Location**: Click on an interactive map to pinpoint exact coordinates ($latitude, longitude$) + auto-reverse geocode address.
  2. **Step 2: Basic Info**: Mosque name, description, address, city, country, contact numbers.
  3. **Step 3: Facilities**: Checkboxes for available amenities.
  4. **Step 4: Photos**: Image URLs / uploads.
* **Feedback**:
  * Displays success screen: *"Your submission is under review. You will earn +100 points upon approval!"*.

### 5.4. Module 4: Mosque Claim Workflow (Way B - Imam Portal)
* Accessible via `"Are you an official representative of this Mosque? Claim it here"`.
* Form collecting: Full Name, Official Email, Phone Number, Position (e.g. Head Imam, Trustee), and Charity/Official Registration Document URL.
* Status tracking: `PENDING` ➔ `APPROVED` (Upgrades user to `MOSQUE_ADMIN`).

### 5.5. Module 5: Moderation & Admin Dashboard
* **Submissions Queue**:
  * Card-by-card diff comparing proposed data vs current data.
  * Quick-action buttons: `[Approve]` (triggers live publication + awards contributor points) / `[Reject]` (with mandatory rejection reason).
* **Claims Review Queue**:
  * View uploaded proof documents, verify Imam identity, and click `[Grant Mosque Admin]`.
* **Super Admin User Directory**:
  * Search users by email or role, promote/demote roles with a dropdown (`USER`, `MOSQUE_ADMIN`, `MODERATOR`, `SUPER_ADMIN`).

### 5.6. Module 6: Authentication & Contributor Gamification
* **Firebase Social Auth**: One-click Google Login, Email/Password login.
* **Dev Mock Switcher (Development Mode)**:
  * Floating debug badge at bottom-left to toggle between:
    * 👤 `Contributor User (mock-contributor-1)`
    * 🛡️ `Moderator (mock-moderator-1)`
    * 👑 `Super Admin (mock-admin-1)`
    * 🕌 `Imam (mock-imam-1)`
* **Points & Badges**:
  * Level 1: *Neighborhood Scout* (0–200 pts)
  * Level 2: *Community Pillar* (200–500 pts)
  * Level 3: *Master Chronicler* (500+ pts)

---

## 6. UI/UX Design System & Color Palette

```
🎨 Color Palette Tokens:
• Primary Brand (Islamic Emerald):   #059669 (Emerald 600) / #10B981 (Emerald 500)
• Secondary / Gold Accent:           #D97706 (Amber 600)   / #F59E0B (Amber 500)
• Background (Light Mode):           #F8FAFC (Slate 50)
• Background (Dark Mode):            #0F172A (Slate 900)
• Surface Cards:                     #FFFFFF (Light)       / #1E293B (Dark)
• Text Primary:                      #0F172A (Slate 900)   / #F1F5F9 (Slate 100)
• Text Muted:                        #64748B (Slate 500)
```

* **Typography**: Clean modern sans-serif (Inter / Plus Jakarta Sans) paired with Arabic-friendly calligraphy font for Bismillah header accents (Amiri / Scheherazade New).
* **Responsive Breakpoints**:
  * Mobile (`< 640px`): Fullscreen map with bottom sheet / swipeable drawer for mosque cards.
  * Tablet (`640px - 1024px`): 50/50 split view (Left: List, Right: Map).
  * Desktop (`> 1024px`): 40/60 split view with top navigation bar and floating filter pill bar.

---

## 7. Backend REST API Integration Directory

| Frontend Feature | HTTP Method | Backend API Endpoint | Auth Requirement |
| :--- | :--- | :--- | :--- |
| User Sync & Onboard | `POST` | `/api/v1/users/sync` | Public |
| Current User Profile & Points | `GET` | `/api/v1/users/me` | Bearer Token |
| Facilities Filter Catalog | `GET` | `/api/v1/facilities` | Public |
| GPS Nearby Discovery | `GET` | `/api/v1/mosques/nearby` | Public |
| Text / City Mosque Search | `GET` | `/api/v1/mosques/search` | Public |
| Full Mosque Profile | `GET` | `/api/v1/mosques/{idOrSlug}` | Public |
| Submit New Mosque | `POST` | `/api/v1/mosques/submissions` | Authenticated |
| Suggest Edit Correction | `POST` | `/api/v1/mosques/{id}/suggest-edit` | Authenticated |
| Submit Mosque Claim | `POST` | `/api/v1/mosques/{id}/claim` | Authenticated |
| Review Claims Queue | `GET` | `/api/v1/admin/mosques/claims` | Moderator / Admin |
| Approve / Reject Claim | `PATCH`| `/api/v1/admin/mosques/claims/{id}/decision` | Moderator / Admin |
| Submissions Moderation Queue | `GET` | `/api/v1/admin/moderation/submissions` | Moderator / Admin |
| Approve / Reject Submission | `PATCH`| `/api/v1/admin/moderation/submissions/{id}/decision` | Moderator / Admin |
| Super Admin User Directory | `GET` | `/api/v1/admin/users` | Super Admin |
| Super Admin Change Role | `PATCH`| `/api/v1/admin/users/{id}/role` | Super Admin |

---

## 8. Development Phases & Milestones

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       Web Frontend Delivery Roadmap                         │
├───────────────────┬───────────────────┬───────────────────┬─────────────────┤
│ Milestone 1       │ Milestone 2       │ Milestone 3       │ Milestone 4     │
│ Foundation & Maps │ Profiles & Submits│ Moderation Portal │ PWA & Polish    │
├───────────────────┼───────────────────┼───────────────────┼─────────────────┤
│ • Vite + Tailwind │ • Mosque Profile  │ • Pending reviews │ • Mobile PWA    │
│ • Leaflet map     │   & countdown     │   dashboard       │   installable   │
│ • Nearby search   │ • Add mosque form │ • Mosque claims   │ • Dark/light    │
│ • Auth & persona  │ • Suggest edit    │ • Admin role      │   theme toggle  │
│   switcher        │   modal           │   management      │ • Production build│
└───────────────────┴───────────────────┴───────────────────┴─────────────────┘
```

---

## 9. Success Metrics (KPIs)
1. **Discovery Speed**: Nearby radius search returns results and renders map pins in `< 300ms`.
2. **Mobile Usability**: 100% of workflows (discovery, submission, claiming) fully operable on mobile touch devices.
3. **Accessibility**: WCAG 2.1 AA compliant contrast and keyboard navigation.
