# PROJECT SPECIFICATION — Political Intelligence Desk (مكتب الذكاء والمتابعة السياسية)

## 1. Overview & Objectives (الهدف)
Professional Mobile-First Political Intelligence & Tracking Desk for geopolitical and intelligence analysts.
Core Operational Pipeline:
Reliable Sources → News Ingestion → Normalization & Classification → Duplicate Detection → Event Clustering / Dossiers → Summarization → Evidence Analysis → Narrative Comparison → Follow-up of Files/Persons/Topics → Real-time Alerts → Executive Reports → AI Research Assistant.
Designed for single-analyst operation initially with enterprise-grade multi-analyst database and architecture readiness.

## 2. Core Rule of Truth (قاعدة أساسية)
- AI is NOT the source of truth.
- Database, verifiable sources, official records, and documented evidence are the source of truth.
- AI is strictly a tool for: Reading, Summarizing, Categorizing, Extraction, Narrative Comparison, Anomaly Detection, Synthesis, and Q&A.
- AI must NEVER hallucinate or invent news, sources, URLs, quotes, dates, metrics, or events.
- Absence of evidence is not evidence of falsehood.
- Source contradictions must be exposed and visualized, never arbitrarily picked.

## 3. News Ingestion System (نظام مصادر الأخبار)
- RSS: Automatic polling, title, original link, pub date, source attribution, description/snippet, enclosure image, source ID.
- API: Secure credentials, pagination, sync timestamps, error logs, deduplication.
- Official Sites: Government presidencies, ministries, agencies, research centers, political institutes (respecting paywalls, robot restrictions, copyright).
- Manual Ingestion: Custom analyst URL input, optional title, source, publication date, analyst notes, on-demand AI analysis.

## 4. Source Management (إدارة المصادر)
- Source attributes: Name, Country, Language, Source Type, Tier, Website URL, RSS URL, API URL, Ingestion Method, Status (Active/Paused/Error), Update Frequency, Last Sync, Connection Status, Analyst Notes, Administrative Reliability Level.
- Live "Test Source" connectivity and validation probe.

## 5. Source Reliability & Credibility Tiers
- PRIMARY (Official statements, gazettes, direct government sources)
- AGENCY (National/International news agencies e.g. Reuters, SPA, AFP)
- TRUSTED_MEDIA (Established news institutions)
- SECONDARY (Commentary, syndicated outlets, aggregators)
- UNVERIFIED (Social media accounts, unconfirmed reports)
- Evidence Confidence scoring with contextual explanation.

## 6. Articles & Normalization
- Mandatory metadata: Title, Source, Canonical URL, Publication Timestamp, Ingestion Timestamp, Language, Country, Source Tier, Legally accessible snippet/text, AI summary, Categories, Topics, Entities/Persons, Organizations, Linked Political Dossier, Associated Event, Priority Level, Verification Status, Evidence Confidence, Duplicate Status, Recirculation Status.

## 7. Summarization (التلخيص)
- Quick summary (2-5 lines).
- Analytical summary (What happened? Who is involved? Where? When? What is new? What remains unknown?).
- Analyst options: Shorter, Detailed, Bullet Points, Extract Numbers/Metrics, Extract Verbatim Quotes, Translate to Arabic/English.

## 8. Date & Temporal Controls (التحكم بالتاريخ)
- Preset filters: Today, Yesterday, Last 3 Days, Last 7 Days, Last 30 Days, This Month, Last Month, Last 3 Months, This Year, Last Year, Custom Range, All Time.
- Combinable with Topics, Sources, Persons, Countries, Organizations, Political Files, Verification Status, Priority.

## 9. Duplicate & Recirculation Detection
- Canonical URL matching, cross-source syndication detection, verbatim re-publication identification, semantic similarity.
- Distinguishing 10 syndications of a single agency wire from 10 independent sources; attributing original source.

## 10. Event Dossier Clustering (تجميع الأخبار في أحداث)
- Grouping related articles into a single evolving political story/event.
- Timeline of development, all corroborating/dissenting sources, consensus points, diverging points, statements, involved actors.

## 11. Verification & Evidence Analysis
- Statuses: CONFIRMED, SUPPORTED, LIKELY, UNVERIFIED, CONTRADICTED, INSUFFICIENT_EVIDENCE, NOT_APPLICABLE.
- Evidence Confidence level with rationale.

## 12. Narrative Comparison (مقارنة الروايات)
- Common facts, Narrative A, Narrative B, Official Narrative, Contradictions, Unique disclosures, Unknowns, Strongest evidence, Confidence level.

## 13. Political Files (الملفات السياسية)
- Master dossiers tracked by analysts: Name, Description, Status (Active/Monitoring/Archived), Topic, Countries, Key Persons, Organizations, Linked Events, Linked Articles, Timeline, Analyst Notes, Priority, Visibility (Personal vs Shared).

## 14. Persons & Organizations (الأشخاص والمؤسسات)
- Politicians, officials, parties, governments, military/security bodies, corporations, think tanks.
- Linked to news, dossiers, and recorded statements.

## 15. Person Stance Analysis (تحليل مواقف الأشخاص)
- Historical position vs Current position, Delta change, Date of change, Verbatim statement, Source, Context, Evidence, Confidence.

## 16. Dynamic Timeline (الخط الزمني)
- Date → Incident/Development → Source → Statement → Evolution → Impact.

## 17. Search & Retrieval (البحث)
- Keyword search, full-text search, semantic search, faceted search across persons, topics, files, sources, dates.

## 18. "What Changed?" (ماذا تغير؟)
- Delta change tracking since analyst's last visit & since yesterday: New events, new developments, new statements, stance shifts, dossier updates, emerging contradictions.

## 19. Research Workspace (مساحة البحث)
- Multi-item synthesis: Select multiple articles, events, or statements for AI-driven comparison, contradiction matrix, and evidence evaluation.

## 20. AI Political Intelligence Assistant (مساعد الذكاء الاصطناعي)
- Grounded RAG with strict citations, uncertainty flags, key findings, and evidence references.

## 21. Alerts System (التنبيهات)
- Breaking News, Important Development, New Statement, File Update, Person Update, Contradiction, Verification Change, Recirculation, Source Alert.
- Severities: Critical, High, Medium, Low.

## 22. Executive Dashboard (لوحة التحكم)
- High-level intelligence metrics, active dossiers, monitored persons, alerts, unverified claims, contradictory narratives, recent analyst activity.

## 23. Core Navigation & Screens (الصفحات الأساسية)
1. Login / Authentication
2. Executive Dashboard
3. News Feed
4. Article Details
5. Event / Story Dossier
6. Political Files
7. Persons & Entities
8. Topics & Thematic Focus
9. Search / Research Workspace
10. Alerts & Notifications
11. Executive Reports
12. Sources & Ingestion Admin
13. Settings & Preferences

## 24. Executive Reports
- Daily Intelligence Briefing, Event In-Depth Report, Political Dossier Strategic Evolution Report.

## 25. Multi-User & Access Control
- Roles: ANALYST, SENIOR_ANALYST, ADMIN.
- Shared vs Private data separation.

## 26. Language & Localization
- Native Arabic with full RTL support + English toggle.

## 27. Security & Prompt Injection Defense
- Untrusted external news sanitization, SQL injection prevention via Room/parameterized queries, secure API key isolation via BuildConfig.

## 28-32. AI Architecture, Logging, Performance, Cost Control & Mobile First
- Structured audit logs for AI queries, rules-first cheap classification before LLM, Room pagination and indices, mobile-first responsive layout.
