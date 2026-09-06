# CHANGELOG — Political Intelligence Desk

## [v0.8.3] - Automated Android CI Workflow

### Added
- **GitHub Actions Workflow (`.github/workflows/android.yml`)**:
  - Automated CI pipeline to build the Android debug APK (`app-debug.apk`) on pushes and pull requests to `main` and `master`, plus manual execution via `workflow_dispatch`.
  - Configures JDK 17 with Temurin distribution and Gradle via `gradle/actions/setup-gradle@v4`.
  - Automatically recovers and decodes `debug.keystore` from `debug.keystore.base64` if absent on the clean runner.
  - Ensures `.env` exists from `.env.example` to satisfy Secrets Gradle Plugin configuration.
  - Generates Gradle wrapper if not present and executes `assembleDebug`.
  - Uploads the resulting `app-debug.apk` as a downloadable GitHub Actions artifact.

---

## [v0.8.2] - Analyst Management in Admin Desk

### Added
- **"Add Analyst" (إضافة محلل) Action & Modal Dialog**:
  - Prominent button with `PersonAdd` icon in the registered analysts header.
  - Dialog allowing inputs for Arabic full name, English full name, professional email address, and role selector chips (`ANALYST`, `SENIOR_ANALYST`, `ADMIN`).
  - Auto-generation of avatar initials and username from input, persisted directly into the Room database via `insertUser`.
- **"Delete / Remove" (إزالة المحلل) Action per Analyst**:
  - Trash can icon button (`DeleteOutline`) beside each analyst card in the list with `testTag("delete_analyst_button_{id}")`.
  - Confirmation alert dialog protecting against accidental deletion, displaying the analyst's name and username.
  - If the active user is deleted, the session gracefully switches to another registered analyst.
  - Deletion event logged into the audit trail activity log.

---

## [v0.8.1] - Mobile Safe Area Layout & Lead Analyst Identity Alignment

### Fixed & Improved
- **Status Bar Safe Area Insets (`statusBarsPadding`)**:
  - Added safe area top insets to `TopAppBarDesk` content within `Surface`. The header background smoothly extends under the status bar while title, emblem, Arabic/English toggle, and user initials are safely padded below the status bar clock, Wi-Fi, battery, and camera notch.
  - Added top and bottom safe area insets to `WelcomeSplashScreen` to prevent clipping with device system bars on phones with notches or rounded corners.
- **Bottom Navigation Bar Safe Area Insets (`navigationBarsPadding`)**:
  - Wrapped the fixed bottom tab row inside `Box(modifier = Modifier.navigationBarsPadding())` within `Surface`.
  - The surface background extends behind the Android gesture navigation pill / system buttons, while all tabs, titles, and alert badges remain cleanly elevated and never obscured by the system navigation bar.
- **Lead Analyst Identity Alignment**:
  - Replaced the analyst name "د. سارة المنصوري" with "د. بلال اللقيس" (`Dr. Bilal Al-Laqqis`, initials `BL`, email `b.laqqis@desk.intel`).
  - Updated in `DatabaseInitializer.kt` (including live migration check for existing databases) and `AdminDeskScreen.kt`.
  - Updated the active intelligence session fallback in `DashboardScreen.kt`.

---

## [v0.8.0] - Executive Welcome / Splash Screen

### Added
- **Executive Welcome / Splash Screen (`WelcomeSplashScreen`)**:
  - Professional, minimalist, sober onboarding screen honoring the Political Intelligence Desk identity.
  - Strictly limited to 3000ms duration with automatic smooth transition (`Crossfade`, 400ms duration) into the main application layout.
  - Zero initial flash of the main dashboard: state initializes directly in the splash view.
  - Staggered entrance animation:
    1. Intelligence emblem fade-in with subtle scaling (0ms - 600ms).
    2. English & Arabic titles fade-in with elegant letter spacing (350ms - 850ms).
    3. Dynamic device date pill fade-in with live Levantine Arabic month names (700ms - 1200ms).
    4. Personalized welcome greeting for Dr. Bilal Al-Laqqis (1100ms - 1600ms).
    5. Soft fade-out into the main workspace at 2700ms - 3000ms.
  - Dynamic Arabic calendar resolution: computes current day of week, day of month, Levantine Arabic month name (e.g., أيلول), and year from the device system clock.
  - Fully responsive, RTL layout direction, mobile-first design with `Modifier.widthIn(max = 480.dp)` preventing stretch on tablets or desktops.
  - Reusable and decoupled architecture: parameters for title, subtitle, user name, greeting message, and duration for future customization without rewriting code.
  - Modified files:
    - `/app/src/main/java/com/example/ui/screens/WelcomeSplashScreen.kt` (New standalone component)
    - `/app/src/main/java/com/example/MainActivity.kt` (Integration into `PoliticalIntelligenceDeskApp` with `rememberSaveable` state and `Crossfade`)
    - `/app/src/test/java/com/example/WelcomeSplashScreenTest.kt` (Unit and UI tests)

---

## [v0.7.0] - Phase 7 Interview Intelligence — Calendar, Grounded AI Preparation & Post-Analysis

### Added
- **Complete Interview Intelligence Module (`InterviewsScreen`)**:
  - Full lifecycle management for political TV and media appearances: pre-interview preparation, in-studio quick briefs, live in-context assistance, and post-interview analysis.
  - Interactive multi-view calendar: Month grid with interview badges/dots, Week timeline, Day schedule, and Chronological searchable list.
  - Video and media library tracking interview recordings, YouTube links, channel sources, and verbatim transcripts.
  - Cross-interview comparative tool for evaluating stance evolution, recurring questions, and evidence attribution over time.
- **Strictly Grounded Interview Intelligence Engine (`InterviewIntelligenceEngine`)**:
  - **Comprehensive Pre-Interview Brief**:
    - **What Changed (ماذا تغير؟)**: Real-time delta tracking against monitored files, events, and statements since previous appearances.
    - **Key Facts (أهم المعلومات)**: Ranked empirical facts with confidence indicators and primary sources.
    - **Likely Questions & Defense Tactics (الأسئلة المحتملة وتكتيكات الرد)**: Probable interviewer angles, suggested counter-responses, historical contexts, and supporting evidence.
    - **Suggested Talking Points (نقاط الحديث المقترحة)**: Formulated phrasing with historical anchors.
    - **Historical Hooks (الشواهد التاريخية)**: Precedents, similarities, differences, and utility in live dialogue.
    - **Performance Trends (نقاط القوة والضعف)**: Objective self-assessment pointers to maintain clarity and avoid unsubstantiated claims.
    - **Previous Positions (المواقف والتصريحات السابقة)**: Cross-reference with analyst's past statements to ensure consistency and explain legitimate evolution.
    - **Challenge Points & Pressure Angles (نقاط الضغط المحتملة)**: Difficult confrontation tactics and recommended de-escalation answers.
    - **Strongest Verified Evidence (أقوى الأدلة الموثقة)**: Highest-confidence proof citations ready for live citation.
    - **Media Framing Comparison (زوايا التناول الإعلامي)**: Arab, Western, and regional narrative frameworks.
    - **3x3x3 Studio Strategy (استراتيجية 3×3×3)**: 3 Core messages, 3 supporting facts, and 3 pitfalls to avoid.
  - **One-Page Studio Brief (إيجاز الاستوديو — ورقة واحدة)**:
    - High-density monospace summary designed for rapid glance-and-speak in television studios with one-tap clipboard copy.
  - **In-Context AI Advisor (المستشار اللحظي)**:
    - Live query assistant specifically tailored for the active interview subject, answering tough questions grounded strictly in local database facts.
  - **Post-Interview Performance Report (تقرير أداء ما بعد المقابلة)**:
    - Transcript ingestion and automated extraction of strong responses, evidence cited, potential contradictions, and notable soundbites.
- **Database & Architecture Integration**:
  - `Interview`, `InterviewLink`, `InterviewTranscript`, `InterviewAnalysis` Room entities (Database v7).
  - TypeConverters for `InterviewType`, `InterviewStatus`, `InterviewLinkType`, and `InterviewAnalysisType`.
  - Comprehensive seed data representing realistic live television appearances on Al-Hadath and Al-Jazeera.
  - Native Arabic first-class UI with Material 3 styling and responsive layout.

---

## [v0.5.0] - Phase 5 AI Research Workspace, Grounded RAG & Executive Reports

### Added
- **AI Research Workspace (`ResearchWorkspaceScreen`)**:
  - Interactive multi-source intelligence synthesis bench enabling analysts to assemble customized dossiers consisting of articles, political events, strategic files, persons, sources, and topics.
  - Full persistence of research sessions via `SavedResearchDao` and `SavedResearch` Room entities.
  - Workspace report generation directly synthesizing the active analyst working set into verifiable executive briefings.
- **Strictly Grounded Anti-Hallucination RAG Engine (`AiRagEngine`)**:
  - Direct grounding against Room database records, verified statements, claims, and corroborated evidence.
  - Automatic prompt injection defense (`sanitizeUntrustedText`) neutralizing instruction hijacking, delimiter escapes, and privilege escalation attempts.
  - Verifiable citation generation attaching primary sources, credibility tiers, dates, and excerpt quotes.
- **Conversational Intelligence Assistant (`AiAssistantScreen`)**:
  - Multi-turn conversational threads (`AiConversation` and `AiMessage`) grounded in the intelligence knowledge graph.
  - Session management, thread switching, quick prompts, and copy-to-clipboard facilities.
- **Executive Reports Engine & Center (`ExecutiveReportsEngine` & `ReportsScreen`)**:
  - Multi-tier report generation: Daily Political Brief (`DAILY_BRIEF`), Event Comprehensive Report (`EVENT_REPORT`), Strategic Dossier Report (`FILE_REPORT`), and Entity Monitoring Report (`MONITORING_REPORT`).
  - Report versioning (`version` counter), executive summaries, structured thematic sections, verified citations, and metadata badges.
  - One-tap clipboard export formatted for executive briefings and intelligence memorandums.

---

## [v0.2.0] - Phase 2 Ingestion, Deduplication, Normalization & News Feed

### Added
- **Multi-Source Ingestion Architecture (`IngestionEngine`)**:
  - **RSS / Atom Parser (`RssParser`)**: Handles standard RSS 2.0 and Atom feeds, CDATA blocks, nested images, multi-format pubDates, and content sanitization.
  - **REST API Connector (`GenericRestApiConnector`)**: Fetches articles from JSON APIs, supports custom headers/API tokens, and flexible field mapping.
  - **Official Web Extractor (`HtmlMetadataExtractor`)**: Scrapes official government portals and news sites using OpenGraph, Twitter Cards, semantic HTML, and publication metadata without violating copyright or paywalls.
  - **Manual Article Import (`ManualArticleImportDialog`)**: Direct URL entry allowing analysts to fetch, preview, assign importance, override classifications, link to dossiers, and append confidential analyst notes.
- **Intelligent 4-Level Duplicate Detection Engine (`DuplicateDetectionEngine`)**:
  - **Level 1 (Exact URL Match)**: Canonical and original URL matching with query-string sanitization.
  - **Level 2 (Exact Content Hash)**: SHA-256 fingerprint matching across stripped article bodies.
  - **Level 3 (Normalized Title Similarity & Jaccard Scoring)**: Tokenized title similarity (threshold >= 0.82) within a rolling 7-day window.
  - **Level 4 (Recirculation & Development Detection)**: Distinguishes genuine breaking updates (`DEVELOPMENT`) from repackaged old stories (`RECIRCULATED`) and official statements (`CONFIRMATION`).
- **Entity Linker & Semantic Normalizer (`ArticleNormalizer`)**:
  - Automatically tags extracted news with primary sovereign country codes, linked politicians, monitored organizations, geopolitical topics, and strategic dossiers.
- **Executive News Feed Screen (`NewsFeedScreen`)**:
  - Multi-criteria filtering: Time horizon (Today, Last 24 Hours, Last 48 Hours, Past Week, Past Month, Custom Date Range), Source Tier, Primary Topic, Classification, Dossier, and Saved articles.
  - Live search bar with instant matching across title, body, source, linked persons, and files.
  - Interactive Article Detail sheet (`ArticleDetailDialog`) with republishing history, source independence verification, and analyst bookmarking.
- **Database & Data Layer Enhancements**:
  - `Article` entity and `ArticleDao` with multi-index acceleration and filtered queries.
  - `IngestionLog` tracking batch timestamps, imported count, duplicates count, error breakdowns, and execution latency.
  - Seeded verified real-world Middle East & international news articles spanning maritime chokepoints, bilateral diplomacy, energy markets, and sovereign statements.
- **Comprehensive Unit & Integration Test Suite (`Phase2IngestionEngineTest`)**:
  - Verified RSS XML parsing, HTML metadata extraction, URL/Hash/Similarity deduplication, recirculation classification, entity linking, and date filtering logic.

---

## [v0.1.0] - Phase 1 Foundation & Core Entities
### Added
- Created complete Room Database architecture (`AppDatabase`) with TypeConverters and DAOs.
- Implemented entities: `User`, `Source`, `Country`, `Organization`, `Person`, `Topic`, `PoliticalFile`, `PersonPosition`, `ActivityLog`.
- Implemented DAOs: `UserDao`, `SourceDao`, `CountryDao`, `OrganizationDao`, `PersonDao`, `TopicDao`, `PoliticalFileDao`, `ActivityLogDao`.
- Pre-populated real verified sovereign countries, monitored entities, diplomats, news agencies, and strategic geopolitical dossiers.
- Implemented Repository pattern (`DeskRepository`) abstracting all database operations using Kotlin coroutines and reactive `Flow`.
- Implemented `DeskViewModel` managing reactive UI state, role-based authorization (Analyst, Senior Analyst, Admin), source connection testing, dossier creation, person tracking, and language switching.
- Built Executive Intelligence UI with Material 3 dark/light high-contrast theme, RTL layout direction, adaptive navigation, and responsive mobile-first screens:
  - **Executive Dashboard**: Key geopolitical intelligence metrics, active files breakdown, monitored persons roster, network status.
  - **Sources Desk**: Comprehensive source management, source tier badges (PRIMARY, AGENCY, TRUSTED_MEDIA, etc.), real HTTP connection probing, add/edit source dialog.
  - **Political Dossiers**: Track strategic files with priority badges, status filters, country/topic linkages, and analyst notes.
  - **Entities & Persons Desk**: Monitor key politicians, diplomats, organizations, and stance tracking.
  - **Admin & Security Console**: Role switcher, activity audit logs, database reset/reseed utilities, and system health status.
- Added custom adaptive launcher icon for Political Intelligence Desk.
- Declared INTERNET permission in `AndroidManifest.xml` for source validation probes.
