package com.example.data.local

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun seedIfEmpty(database: AppDatabase) = withContext(Dispatchers.IO) {
        val userDao = database.userDao()
        val existingUsers = userDao.getUserById(1)
        if (existingUsers != null) {
            if (existingUsers.fullNameAr.contains("سارة") || existingUsers.username == "lead.analyst") {
                userDao.updateUser(
                    existingUsers.copy(
                        fullNameAr = "د. بلال اللقيس",
                        fullNameEn = "Dr. Bilal Al-Laqqis",
                        email = "b.laqqis@desk.intel",
                        avatarInitials = "BL"
                    )
                )
            }
            return@withContext
        }

        // 1. Initial Analyst Users
        val users = listOf(
            User(
                id = 1,
                username = "lead.analyst",
                fullNameAr = "د. بلال اللقيس",
                fullNameEn = "Dr. Bilal Al-Laqqis",
                email = "b.laqqis@desk.intel",
                role = UserRole.SENIOR_ANALYST,
                avatarInitials = "BL",
                isActive = true
            ),
            User(
                id = 2,
                username = "field.analyst",
                fullNameAr = "طارق الكيلاني",
                fullNameEn = "Tariq Al-Kilani",
                email = "t.kilani@desk.intel",
                role = UserRole.ANALYST,
                avatarInitials = "TK",
                isActive = false
            ),
            User(
                id = 3,
                username = "system.admin",
                fullNameAr = "المهندس عمر الشامي",
                fullNameEn = "Omar Al-Shami",
                email = "admin@desk.intel",
                role = UserRole.ADMIN,
                avatarInitials = "OS",
                isActive = false
            )
        )
        userDao.insertUsers(users)

        // 2. Sovereign Countries
        val countryDao = database.countryDao()
        val countries = listOf(
            Country("SA", "المملكة العربية السعودية", "Saudi Arabia", "Gulf", "🇸🇦", true),
            Country("EG", "جمهورية مصر العربية", "Egypt", "North Africa", "🇪🇬", true),
            Country("AE", "الإمارات العربية المتحدة", "United Arab Emirates", "Gulf", "🇦🇪", true),
            Country("QA", "دولة قطر", "Qatar", "Gulf", "🇶🇦", true),
            Country("JO", "المملكة الأردنية الهاشمية", "Jordan", "Levant", "🇯🇴", true),
            Country("LB", "الجمهورية اللبنانية", "Lebanon", "Levant", "🇱🇧", true),
            Country("IQ", "جمهورية العراق", "Iraq", "Middle East", "🇮🇶", true),
            Country("SY", "الجمهورية العربية السورية", "Syria", "Levant", "🇸🇾", true),
            Country("IR", "الجمهورية الإسلامية الإيرانية", "Iran", "Middle East", "🇮🇷", true),
            Country("TR", "الجمهورية التركية", "Turkey", "Eurasia", "🇹🇷", true),
            Country("US", "الولايات المتحدة الأمريكية", "United States", "North America", "🇺🇸", true),
            Country("FR", "الجمهورية الفرنسية", "France", "Europe", "🇫🇷", false),
            Country("GB", "المملكة المتحدة", "United Kingdom", "Europe", "🇬🇧", false),
            Country("YE", "الجمهورية اليمنية", "Yemen", "Arabian Peninsula", "🇾🇪", true)
        )
        countryDao.insertCountries(countries)

        // 3. Topics
        val topicDao = database.topicDao()
        val topics = listOf(
            Topic(
                id = 1,
                nameAr = "الممرات البحرية وأمن الطاقة",
                nameEn = "Maritime Chokepoints & Energy Security",
                category = TopicCategory.MARITIME_CHOKEPOINTS,
                description = "متابعة حركة التجارة وأمن الملاحة في مضيق هرمز وباب المندب وقناة السويس",
                colorHex = "#0EA5E9"
            ),
            Topic(
                id = 2,
                nameAr = "الدبلوماسية الإقليمية والوساطات",
                nameEn = "Regional Diplomacy & Mediation",
                category = TopicCategory.DIPLOMACY,
                description = "محادثات التهدئة، تبادل السفراء، والقمم الوزارية الثنائية والمتعددة الأطراف",
                colorHex = "#10B981"
            ),
            Topic(
                id = 3,
                nameAr = "الردع العسكري والتحالفات الأمنية",
                nameEn = "Military Deterrence & Defense Alliances",
                category = TopicCategory.SECURITY_DEFENSE,
                description = "مناورات عسكرية، صفقات دفاعية، وتمركز القوات والقدرات الاستراتيجية",
                colorHex = "#EF4444"
            ),
            Topic(
                id = 4,
                nameAr = "الملف النووي والأمن الاستراتيجي",
                nameEn = "Nuclear File & Strategic Deterrence",
                category = TopicCategory.REGIONAL_CONFLICT,
                description = "تقارير وكالة الطاقة الذرية، منشآت التخصيب، والمفاوضات الأمنية غير المباشرة",
                colorHex = "#F59E0B"
            ),
            Topic(
                id = 5,
                nameAr = "الغاز والطاقة والحدود البحرية",
                nameEn = "Gas, Energy & Maritime Borders",
                category = TopicCategory.ENERGY_ECONOMY,
                description = "ترسيم الحدود في شرق المتوسط، عقود الغاز المسال، وخطوط الإمداد الإقليمية",
                colorHex = "#8B5CF6"
            )
        )
        topicDao.insertTopics(topics)

        // 4. Monitored Organizations
        val orgDao = database.organizationDao()
        val organizations = listOf(
            Organization(
                id = 1,
                nameAr = "وزارة الخارجية السعودية",
                nameEn = "Ministry of Foreign Affairs of Saudi Arabia",
                type = OrganizationType.GOVERNMENT,
                countryCode = "SA",
                websiteUrl = "https://www.mofa.gov.sa",
                description = "الجهة المسؤولة عن إدارة العلاقات الخارجية والدبلوماسية للمملكة العربية السعودية",
                isMonitored = true
            ),
            Organization(
                id = 2,
                nameAr = "جامعة الدول العربية",
                nameEn = "League of Arab States",
                type = OrganizationType.REGIONAL_BLOC,
                countryCode = "EG",
                websiteUrl = "http://www.leagueofarabstates.net",
                description = "منظمة إقليمية تضم الدول العربية لتنسيق المواقف السياسية والاقتصادية",
                isMonitored = true
            ),
            Organization(
                id = 3,
                nameAr = "مجلس الأمن التابع للأمم المتحدة",
                nameEn = "United Nations Security Council (UNSC)",
                type = OrganizationType.INTERNATIONAL_BODY,
                countryCode = "US",
                websiteUrl = "https://www.un.org/securitycouncil/",
                description = "الهيئة الدولية المسؤولة عن صون السلم والأمن الدوليين وإصدار القرارات الملزمة",
                isMonitored = true
            ),
            Organization(
                id = 4,
                nameAr = "مجلس التعاون لدول الخليج العربية",
                nameEn = "Gulf Cooperation Council (GCC)",
                type = OrganizationType.REGIONAL_BLOC,
                countryCode = "SA",
                websiteUrl = "https://www.gcc-sg.org",
                description = "تكتل سياسي واقتصادي وأمني يضم الدول الست في شبه الجزيرة العربية",
                isMonitored = true
            )
        )
        orgDao.insertOrganizations(organizations)

        // 5. Monitored Diplomats and Politicians
        val personDao = database.personDao()
        val persons = listOf(
            Person(
                id = 1,
                nameAr = "الأمير فيصل بن فرحان آل سعود",
                nameEn = "Prince Faisal bin Farhan Al Saud",
                currentRoleAr = "وزير خارجية المملكة العربية السعودية",
                currentRoleEn = "Minister of Foreign Affairs of Saudi Arabia",
                previousRoles = "سفير المملكة لدى ألمانيا (2019)",
                organizationId = 1,
                countryCode = "SA",
                bio = "دبلوماسي سعودي يقود التحركات الدبلوماسية النشطة للمملكة في ملفات التهدئة الإقليمية والوساطات وحل النزاعات",
                isMonitored = true,
                tags = "دبلوماسية, وساطة, الخليج, الشرق الأوسط",
                stanceShiftCount = 1,
                lastStatementSummary = "التأكيد على أولوية خفض التصعيد في المنطقة ودعم آليات الحوار البناء وضمان حرية الملاحة."
            ),
            Person(
                id = 2,
                nameAr = "د. بدر عبد العاطي",
                nameEn = "Dr. Badr Abdelatty",
                currentRoleAr = "وزير الخارجية والهجرة وشؤون المصريين بالخارج",
                currentRoleEn = "Minister of Foreign Affairs and Emigration of Egypt",
                previousRoles = "سفير مصر لدى الاتحاد الأوروبي وبلجيكا، متحدث رسمي باسم الخارجية",
                organizationId = null,
                countryCode = "EG",
                bio = "دبلوماسي مصري مخضرم يتولى قيادة الدبلوماسية المصرية في ملفات البحر الأحمر، حوض النيل، وغزة والتهدئة الإقليمية",
                isMonitored = true,
                tags = "دبلوماسية مصرية, البحر الأحمر, حوض النيل, غزة",
                stanceShiftCount = 0,
                lastStatementSummary = "التشديد على خطورة عسكرة البحر الأحمر وتأثيرها المباشر على حركة التجارة وقناة السويس."
            ),
            Person(
                id = 3,
                nameAr = "الشيخ عبد الله بن زايد آل نهيان",
                nameEn = "Sheikh Abdullah bin Zayed Al Nahyan",
                currentRoleAr = "نائب رئيس مجلس الوزراء وزير خارجية دولة الإمارات العربية المتحدة",
                currentRoleEn = "Deputy Prime Minister and Minister of Foreign Affairs of the UAE",
                previousRoles = "وزير الإعلام والثقافة (1997-2006)",
                organizationId = null,
                countryCode = "AE",
                bio = "يقود الدبلوماسية الإماراتية مع التركيز على الاستقرار الاقتصادي والتحالفات الإقليمية والمساعدات الإنسانية",
                isMonitored = true,
                tags = "دبلوماسية, تنمية مستدامة, شراكات دولية",
                stanceShiftCount = 0,
                lastStatementSummary = "التشديد على التعاون الإقليمي كركيزة أساسية لمواجهة التحديات الاقتصادية والأمنية المشتركة."
            ),
            Person(
                id = 4,
                nameAr = "أيمن الصفدي",
                nameEn = "Ayman Safadi",
                currentRoleAr = "نائب رئيس الوزراء ووزير الخارجية وشؤون المغتربين الأردني",
                currentRoleEn = "Deputy Prime Minister and Minister of Foreign Affairs of Jordan",
                previousRoles = "مستشار جلالة الملك عبدالله الثاني",
                organizationId = null,
                countryCode = "JO",
                bio = "مسؤول عن إدارة التنسيق الدبلوماسي الأردني بشأن أمن الحدود الشمالية ومقدسات القدس والجهود الإغاثية",
                isMonitored = true,
                tags = "دبلوماسية, أمن الحدود, التنسيق العربي",
                stanceShiftCount = 1,
                lastStatementSummary = "تحذير من مغبة توسيع دائرة الصراع والتأكيد على حماية حدود المملكة من عمليات التهريب المنظمة."
            ),
            Person(
                id = 5,
                nameAr = "أنتوني بلينكن",
                nameEn = "Antony Blinken",
                currentRoleAr = "وزير خارجية الولايات المتحدة الأمريكية",
                currentRoleEn = "United States Secretary of State",
                previousRoles = "نائب وزير الخارجية (2015-2017)",
                organizationId = 3,
                countryCode = "US",
                bio = "رئيس الدبلوماسية الأمريكية، يقود جولات مكثفة في الشرق الأوسط لدعم الاستقرار والتحالفات الأمنية",
                isMonitored = true,
                tags = "دبلوماسية أمريكية, الشرق الأوسط, البحر الأحمر",
                stanceShiftCount = 2,
                lastStatementSummary = "التزام واشنطن بحرية الملاحة الدولية في البحر الأحمر ومواصلة التنسيق مع الشركاء الإقليميين."
            )
        )
        personDao.insertPersons(persons)

        // Stance Shift Evidence Example
        personDao.insertPosition(
            PersonPosition(
                id = 1,
                personId = 1,
                topicId = 2,
                fileId = 2,
                issueTitle = "العلاقات مع طهران والترتيبات الأمنية في الخليج",
                previousStance = "المطالبة بضمانات دولية صارمة قبل أي انخراط دبلوماسي مباشر (2021).",
                currentStance = "تبادل الزيارات الوزارية وتفعيل اتفاق بكين وبناء آليات حوار أمني ثنائية مباشرة (2024-2026).",
                shiftType = StanceShiftType.SIGNIFICANT_REVERSAL,
                changeDateTimestamp = System.currentTimeMillis() - 86400000L * 180,
                verbatimStatement = "إن استقرار المنطقة يتطلب الحوار المباشر وتغليب المصالح التنموية المشتركة على سياسات المواجهة.",
                sourceName = "وكالة الأنباء السعودية (واس)",
                sourceUrl = "https://www.spa.gov.sa/viewstory.php",
                context = "بيان مشترك في ختام مباحثات الرياض حول تفعيل الاتفاقات الاقتصادية والأمنية الثنائية",
                evidenceConfidence = 95
            )
        )

        // 6. Real Verified News Sources
        val sourceDao = database.sourceDao()
        val sources = listOf(
            Source(
                id = 1,
                nameAr = "وكالة الأنباء السعودية (واس)",
                nameEn = "Saudi Press Agency (SPA)",
                countryCode = "SA",
                language = "ar",
                tier = SourceTier.AGENCY,
                fetchMethod = FetchMethod.RSS,
                websiteUrl = "https://www.spa.gov.sa",
                rssUrl = "https://www.spa.gov.sa/rss.xml",
                status = SourceStatus.ACTIVE,
                updateFrequencyMinutes = 15,
                connectionState = ConnectionState.CONNECTED,
                lastTestMessage = "استجابة خادم HTTP 200 - تغذية XML متوافقة",
                reliabilityLevel = 98,
                notes = "المصدر الرسمي للأوامر الملكية والبيانات الدبلوماسية السعودية الرسمية",
                isOfficial = true,
                totalArticlesIngested = 12
            ),
            Source(
                id = 2,
                nameAr = "وكالة رويترز (الشرق الأوسط)",
                nameEn = "Reuters Middle East",
                countryCode = "GB",
                language = "ar",
                tier = SourceTier.AGENCY,
                fetchMethod = FetchMethod.RSS,
                websiteUrl = "https://www.reuters.com",
                rssUrl = "https://www.reutersagency.com/feed/?best-topics=middle-east",
                status = SourceStatus.ACTIVE,
                updateFrequencyMinutes = 10,
                connectionState = ConnectionState.CONNECTED,
                lastTestMessage = "استجابة سريعة 200 OK - تدفق برقيات متواصل",
                reliabilityLevel = 95,
                notes = "وكالة دولية مرجعية للأخبار العاجلة والأسواق والتحركات الدبلوماسية",
                isOfficial = false,
                totalArticlesIngested = 8
            ),
            Source(
                id = 3,
                nameAr = "وكالة الأنباء الفرنسية (أ ف ب)",
                nameEn = "Agence France-Presse (AFP)",
                countryCode = "FR",
                language = "ar",
                tier = SourceTier.AGENCY,
                fetchMethod = FetchMethod.RSS,
                websiteUrl = "https://www.afp.com/ar",
                rssUrl = "https://www.afp.com/ar/rss",
                status = SourceStatus.ACTIVE,
                updateFrequencyMinutes = 15,
                connectionState = ConnectionState.CONNECTED,
                lastTestMessage = "استجابة 200 OK - تغطية ميدانية موسعة",
                reliabilityLevel = 94,
                notes = "تغطية ميدانية دقيقة للملفات السورية واللبنانية والدولية",
                isOfficial = false,
                totalArticlesIngested = 6
            ),
            Source(
                id = 4,
                nameAr = "وكالة أنباء الإمارات (وام)",
                nameEn = "Emirates News Agency (WAM)",
                countryCode = "AE",
                language = "ar",
                tier = SourceTier.AGENCY,
                fetchMethod = FetchMethod.RSS,
                websiteUrl = "https://www.wam.ae",
                rssUrl = "https://www.wam.ae/ar/rss",
                status = SourceStatus.ACTIVE,
                updateFrequencyMinutes = 20,
                connectionState = ConnectionState.CONNECTED,
                lastTestMessage = "متصل بنجاح HTTP 200",
                reliabilityLevel = 96,
                notes = "المصدر الرسمي للمراسيم الاتحادية والنشاط الدبلوماسي لدولة الإمارات",
                isOfficial = true,
                totalArticlesIngested = 5
            ),
            Source(
                id = 5,
                nameAr = "بوابة وزارة الخارجية المصرية",
                nameEn = "Egypt MFA Official Portal",
                countryCode = "EG",
                language = "ar",
                tier = SourceTier.PRIMARY,
                fetchMethod = FetchMethod.OFFICIAL_SITE,
                websiteUrl = "https://mfa.gov.eg",
                rssUrl = null,
                status = SourceStatus.ACTIVE,
                updateFrequencyMinutes = 30,
                connectionState = ConnectionState.CONNECTED,
                lastTestMessage = "الموقع الرسمي متاح - تم استخراج البيانات الصحفية",
                reliabilityLevel = 97,
                notes = "البيانات الصحفية والتحركات الدبلوماسية للوزير والناطق الرسمي",
                isOfficial = true,
                totalArticlesIngested = 3
            ),
            Source(
                id = 6,
                nameAr = "بي بي سي عربي",
                nameEn = "BBC Arabic",
                countryCode = "GB",
                language = "ar",
                tier = SourceTier.TRUSTED_MEDIA,
                fetchMethod = FetchMethod.RSS,
                websiteUrl = "https://www.bbc.com/arabic",
                rssUrl = "https://feeds.bbci.co.uk/arabic/rss.xml",
                status = SourceStatus.ACTIVE,
                updateFrequencyMinutes = 15,
                connectionState = ConnectionState.CONNECTED,
                lastTestMessage = "تغذية RSS متطابقة مع المعايير",
                reliabilityLevel = 90,
                notes = "تغطية مستقلة وتحليلات سياقية متوازنة",
                isOfficial = false,
                totalArticlesIngested = 4
            ),
            Source(
                id = 7,
                nameAr = "أخبار الأمم المتحدة (عربي)",
                nameEn = "UN News Arabic",
                countryCode = "US",
                language = "ar",
                tier = SourceTier.PRIMARY,
                fetchMethod = FetchMethod.RSS,
                websiteUrl = "https://news.un.org/ar/",
                rssUrl = "https://news.un.org/feed/subscribe/ar/news/all/rss.xml",
                status = SourceStatus.ACTIVE,
                updateFrequencyMinutes = 30,
                connectionState = ConnectionState.CONNECTED,
                lastTestMessage = "بيانات مجلس الأمن والجمعية العامة متزامنة",
                reliabilityLevel = 99,
                notes = "التقارير الميدانية لفرق التفتيش وجلسات مجلس الأمن الدولي",
                isOfficial = true,
                totalArticlesIngested = 4
            )
        )
        sourceDao.insertSources(sources)

        // 7. Political Files (الملفات السياسية الاستراتيجية)
        val fileDao = database.politicalFileDao()
        val files = listOf(
            PoliticalFile(
                id = 1,
                titleAr = "أمن الملاحة في البحر الأحمر ومضيق باب المندب",
                titleEn = "Red Sea & Bab el-Mandeb Maritime Security",
                description = "متابعة الهجمات البحرية، عمليات تحالف حارس الازدهار ومهمة أسبيدس الأوروبية، وتأثير تحويل مسارات الشحن حول رأس الرجاء الصالح على عائدات قناة السويس واستقرار سلاسل الإمداد العالمية.",
                status = FileStatus.ACTIVE,
                priority = FilePriority.CRITICAL,
                topicId = 1,
                primaryCountryCode = "YE",
                linkedCountryCodes = "YE,EG,SA,US,GB",
                linkedPersonNames = "أنتوني بلينكن, د. بدر عبد العاطي",
                linkedOrgNames = "مجلس الأمن, تحالف حارس الازدهار",
                analystNotes = "التركيز على رصد مؤشرات استهداف ناقلات النفط والغاز المسال، ومقارنة تقديرات خسائر الشحن البحري بين المؤسسات الدولية.",
                recentDevelopments = "تسجيل تحذيرات ملاحية بريطانية (UKMTO) جديدة جنوب غرب الحديدة وتأكيد مصري على ضرورة التهدئة الشاملة.",
                whatChangedDelta = "مصر تعلن مباحثات مكثفة مع شركاء الملاحة الدولية وإصدار تحديث أمني لحركة القوافل في قناة السويس."
            ),
            PoliticalFile(
                id = 2,
                titleAr = "مسارات التهدئة الدبلوماسية والتفاهمات الإقليمية",
                titleEn = "Regional Diplomatic De-escalation & Normalization",
                description = "رصد آليات تطبيق اتفاق بكين بين الرياض وطهران، محادثات التطبيع والحوارات الأمنية الاستراتيجية في بغداد ومسقط ومخرجات القمم الوزارية المشتركة.",
                status = FileStatus.ACTIVE,
                priority = FilePriority.HIGH,
                topicId = 2,
                primaryCountryCode = "SA",
                linkedCountryCodes = "SA,IR,EG,QA,AE",
                linkedPersonNames = "الأمير فيصل بن فرحان, الشيخ عبد الله بن زايد",
                linkedOrgNames = "وزارة الخارجية السعودية, مجلس التعاون",
                analystNotes = "ضرورة عزل التصريحات الاستهلاكية عن خطوات إعادة فتح الممثليات والتبادل التجاري والاستثماري الفعلي.",
                recentDevelopments = "انعقاد جولة مباحثات ثنائية ركزت على ملفات الأمن الإقليمي والتنسيق في منظمة التعاون الإسلامي.",
                whatChangedDelta = "صدور تأكيدات رسمية على التزام الأطراف بمواصلة التنسيق الدبلوماسي وتفادي التصعيد في الممرات البحرية."
            ),
            PoliticalFile(
                id = 3,
                titleAr = "أمن الحدود الأردنية السورية ومكافحة شبكات التهريب",
                titleEn = "Jordanian-Syrian Border Security & Counter-Smuggling",
                description = "متابعة تطورات الوضع الأمني على طول الحدود الشمالية للأردن، عمليات إحباط تهريب الأسلحة والمخدرات، والاتصالات الدبلوماسية مع دمشق والقوى الفاعلة لتأمين المعابر الرسمية.",
                status = FileStatus.ACTIVE,
                priority = FilePriority.HIGH,
                topicId = 3,
                primaryCountryCode = "JO",
                linkedCountryCodes = "JO,SY",
                linkedPersonNames = "أيمن الصفدي",
                linkedOrgNames = "القوات المسلحة الأردنية, وزارة الخارجية الأردنية",
                analystNotes = "متابعة تصريحات المسؤولين الأردنيين حول مسؤولية الجماعات المنظمة على الجانب السوري.",
                recentDevelopments = "سلاح الجو الأردني وقوات حرس الحدود يعلنون إحباط محاولة تسلل منظمة واستمرار تسيير الدوريات الآلية والمسيرة.",
                whatChangedDelta = "دعوة أردنية لاجتماع تنسيقي رباعي إقليمي لضبط أمن الحدود الشمالية."
            ),
            PoliticalFile(
                id = 4,
                titleAr = "اقتصاد غاز شرق المتوسط وترسيم الحدود البحرية",
                titleEn = "East Mediterranean Gas Economics & Maritime Demarcation",
                description = "رصد عمليات الاستكشاف والتطوير لحقول الغاز (ظهر، كاريش، تمار، حقل نرجس)، مشروعات إسالة وتصدير الغاز إلى أوروبا، ومنتدى غاز شرق المتوسط.",
                status = FileStatus.MONITORING,
                priority = FilePriority.MEDIUM,
                topicId = 5,
                primaryCountryCode = "EG",
                linkedCountryCodes = "EG,LB,CY,GR",
                linkedPersonNames = "د. بدر عبد العاطي",
                linkedOrgNames = "منتدى غاز شرق المتوسط",
                analystNotes = "مقارنة العقود المعلنة مع الكميات الفعلية المصدرة لمحطات الإسالة في إدكو ودمياط.",
                recentDevelopments = "إعلان بدء حفر آبار تنموية جديدة لزيادة إنتاج الغاز الطبيعي الموجه للتصدير والاستهلاك المحلي.",
                whatChangedDelta = "مصر والاتحاد الأوروبي يوقعان مذكرة تفاهم لتوسيع التعاون الاستراتيجي في قطاع الطاقة النظيفة والغاز."
            )
        )
        fileDao.insertFiles(files)

        // 8. Phase 2: Seed Verified Real News Articles (Demonstrating Source Independence, Duplicate & Recirculated Detection)
        val articleDao = database.articleDao()
        val now = System.currentTimeMillis()

        val articles = listOf(
            Article(
                id = 1,
                title = "وزير الخارجية السعودي يبحث مع نظيره الأمريكي هاتفياً مستجدات أمن البحر الأحمر وجهود خفض التصعيد",
                normalizedTitle = "وزير الخارجيه السعودي يبحث مع نظيره الامريكي هاتفيا مستجدات امن البحر الاحمر وجهود خفض التصعيد",
                originalUrl = "https://www.spa.gov.sa/viewstory.php?newsid=2489012",
                canonicalUrl = "https://www.spa.gov.sa/viewstory.php?newsid=2489012",
                contentHash = "hash_spa_2489012_red_sea",
                snippet = "تلقى صاحب السمو الأمير فيصل بن فرحان بن عبد الله وزير الخارجية، اتصالاً هاتفياً من وزير خارجية الولايات المتحدة الأمريكية أنتوني بلينكن. وجرى خلال الاتصال بحث تطورات الأوضاع في المنطقة والممرات البحرية.",
                fullText = "تلقى صاحب السمو الأمير فيصل بن فرحان بن عبد الله وزير الخارجية، اتصالاً هاتفياً، اليوم، من معالي وزير خارجية الولايات المتحدة الأمريكية أنتوني بلينكن. وجرى خلال الاتصال بحث تطورات الأوضاع في المنطقة، والجهود المبذولة لخفض حدة التصعيد، إضافة إلى مناقشة حماية حركة الملاحة الدولية في البحر الأحمر ومضيق باب المندب، بما يضمن تدفق التجارة العالمية بأمان وسلامة.",
                author = "واس - الرياض",
                imageUrl = "https://images.unsplash.com/photo-1541872703-74c5e44368f9?w=600",
                sourceId = 1,
                sourceName = "وكالة الأنباء السعودية (واس)",
                sourceTier = SourceTier.AGENCY,
                language = "ar",
                publishedAt = now - 3600000L * 2, // 2 hours ago
                fetchedAt = now - 3600000L * 2,
                ingestionMethod = FetchMethod.RSS,
                isDuplicate = false,
                classification = ArticleClassification.NEW,
                originalAgency = "واس (وكالة الأنباء السعودية)",
                primaryCountryCode = "SA",
                linkedCountryCodes = "SA,US,YE",
                linkedPersonNames = "الأمير فيصل بن فرحان آل سعود, أنتوني بلينكن",
                linkedOrgNames = "وزارة الخارجية السعودية",
                topicId = 1,
                topicName = "الممرات البحرية وأمن الطاقة",
                politicalFileId = 1,
                politicalFileTitle = "أمن الملاحة في البحر الأحمر ومضيق باب المندب",
                importanceScore = 95,
                isVerified = true
            ),
            Article(
                id = 2,
                title = "الرياض وواشنطن تبحثان هاتفياً تأمين الملاحة الدولية في البحر الأحمر",
                normalizedTitle = "الرياض وواشنطن تبحثان هاتفيا تامين الملاحه الدوليه في البحر الاحمر",
                originalUrl = "https://www.reuters.com/world/middle-east/saudi-us-foreign-ministers-call-red-sea-2026",
                canonicalUrl = "https://www.reuters.com/world/middle-east/saudi-us-foreign-ministers-call-red-sea-2026",
                contentHash = "hash_reuters_saudi_us_call",
                snippet = "نقلاً عن وكالة الأنباء السعودية (واس): بحث وزيرا الخارجية السعودي والأمريكي في اتصال هاتفي جهود حماية سلاسل الإمداد ومسارات الشحن في البحر الأحمر.",
                author = "رويترز",
                sourceId = 2,
                sourceName = "وكالة رويترز (الشرق الأوسط)",
                sourceTier = SourceTier.AGENCY,
                language = "ar",
                publishedAt = now - 3600000L * 1, // 1 hour ago
                fetchedAt = now - 3600000L * 1,
                ingestionMethod = FetchMethod.RSS,
                isDuplicate = false,
                classification = ArticleClassification.DEVELOPMENT,
                originalAgency = "واس (وكالة الأنباء السعودية)", // Demonstrating Source Independence attribution
                primaryCountryCode = "SA",
                linkedCountryCodes = "SA,US",
                linkedPersonNames = "الأمير فيصل بن فرحان آل سعود, أنتوني بلينكن",
                topicId = 1,
                topicName = "الممرات البحرية وأمن الطاقة",
                politicalFileId = 1,
                politicalFileTitle = "أمن الملاحة في البحر الأحمر ومضيق باب المندب",
                importanceScore = 80,
                isVerified = true
            ),
            Article(
                id = 3,
                title = "مجلس الأمن يجدد دعوته للوقف الفوري لجميع الهجمات التي تستهدف سفن الشحن التجاري في باب المندب",
                normalizedTitle = "مجلس الامن يجدد دعوته للوقف الفوري لجميع الهجمات التي تستهدف سفن الشحن التجاري في باب المندب",
                originalUrl = "https://news.un.org/ar/story/2026/09/unsc-red-sea-resolution",
                canonicalUrl = "https://news.un.org/ar/story/2026/09/unsc-red-sea-resolution",
                contentHash = "hash_un_news_red_sea_statement",
                snippet = "أكد أعضاء مجلس الأمن الدولي على أهمية احترام القانون الدولي وحرية الملاحة البحرية لجميع السفن العابرة في البحر الأحمر وخليج عدن وفق اتفاقية الأمم المتحدة لقانون البحار.",
                author = "نيويورك - أخبار الأمم المتحدة",
                sourceId = 7,
                sourceName = "أخبار الأمم المتحدة (عربي)",
                sourceTier = SourceTier.PRIMARY,
                language = "ar",
                publishedAt = now - 3600000L * 5, // 5 hours ago
                fetchedAt = now - 3600000L * 5,
                ingestionMethod = FetchMethod.RSS,
                isDuplicate = false,
                classification = ArticleClassification.CONFIRMATION,
                primaryCountryCode = "YE",
                linkedCountryCodes = "YE,US,GB,EG",
                linkedOrgNames = "مجلس الأمن التابع للأمم المتحدة (UNSC)",
                topicId = 1,
                topicName = "الممرات البحرية وأمن الطاقة",
                politicalFileId = 1,
                politicalFileTitle = "أمن الملاحة في البحر الأحمر ومضيق باب المندب",
                importanceScore = 90,
                isVerified = true
            ),
            Article(
                id = 4,
                title = "وزير الخارجية المصري يؤكد: أمن الملاحة في البحر الأحمر خط أحمر مرتبط مباشرة بالأمن القومي لمصر وعائدات قناة السويس",
                normalizedTitle = "وزير الخارجيه المصري يؤكد امن الملاحه في البحر الاحمر خط احمر مرتبط مباشره بالامن القومي لمصر وعائدات قناه السويس",
                originalUrl = "https://mfa.gov.eg/press-releases/red-sea-security-suez-canal-2026",
                canonicalUrl = "https://mfa.gov.eg/press-releases/red-sea-security-suez-canal-2026",
                contentHash = "hash_mfa_eg_badr_abdelatty_suez",
                snippet = "صرح السفير د. بدر عبد العاطي وزير الخارجية والهجرة أن مصر تجري اتصالات مكثفة مع كافة الأطراف الإقليمية والدولية الفاعلة لتطويق أي تداعيات سلبية على سلامة الممرات البحرية وقناة السويس.",
                author = "الناطق الرسمي - القاهرة",
                sourceId = 5,
                sourceName = "بوابة وزارة الخارجية المصرية",
                sourceTier = SourceTier.PRIMARY,
                language = "ar",
                publishedAt = now - 3600000L * 12, // 12 hours ago
                fetchedAt = now - 3600000L * 12,
                ingestionMethod = FetchMethod.OFFICIAL_SITE,
                isDuplicate = false,
                classification = ArticleClassification.NEW,
                primaryCountryCode = "EG",
                linkedCountryCodes = "EG,YE",
                linkedPersonNames = "د. بدر عبد العاطي",
                linkedOrgNames = "وزارة الخارجية المصرية",
                topicId = 1,
                topicName = "الممرات البحرية وأمن الطاقة",
                politicalFileId = 1,
                politicalFileTitle = "أمن الملاحة في البحر الأحمر ومضيق باب المندب",
                importanceScore = 92,
                isVerified = true
            ),
            Article(
                id = 5,
                title = "القوات المسلحة الأردنية تحبط محاولة تهريب كميات كبيرة من المواد المخدرة على الحدود الشمالية",
                normalizedTitle = "القوات المسلحه الاردنيه تحبط محاوله تهريب كميات كبيره من المواد المخدره علي الحدود الشماليه",
                originalUrl = "https://www.afp.com/ar/news/jordan-border-intercept-smuggling-2026",
                canonicalUrl = "https://www.afp.com/ar/news/jordan-border-intercept-smuggling-2026",
                contentHash = "hash_afp_jordan_border_smuggling",
                snippet = "صرح مصدر عسكري مسؤول في القيادة العامة للقوات المسلحة الأردنية - الجيش العربي، أن المنطقة العسكرية الشرقية أحبطت فجر اليوم عملية نوعية للتسلل والتهريب قادمة من الأراضي السورية.",
                author = "عمان - أ ف ب",
                sourceId = 3,
                sourceName = "وكالة الأنباء الفرنسية (أ ف ب)",
                sourceTier = SourceTier.AGENCY,
                language = "ar",
                publishedAt = now - 3600000L * 20, // 20 hours ago
                fetchedAt = now - 3600000L * 20,
                ingestionMethod = FetchMethod.RSS,
                isDuplicate = false,
                classification = ArticleClassification.NEW,
                primaryCountryCode = "JO",
                linkedCountryCodes = "JO,SY",
                linkedPersonNames = "أيمن الصفدي",
                topicId = 3,
                topicName = "الردع العسكري والتحالفات الأمنية",
                politicalFileId = 3,
                politicalFileTitle = "أمن الحدود الأردنية السورية ومكافحة شبكات التهريب",
                importanceScore = 85,
                isVerified = true
            ),
            Article(
                id = 6,
                title = "إعادة تدوير تقرير قديم: تقديرات خسائر مسار رأس الرجاء الصالح مقارنة بالعبور في قناة السويس",
                normalizedTitle = "اعاده تدوير تقرير قديم تقديرات خسائر مسار راس الرجاء الصالح مقارنه بالعبور في قناه السويس",
                originalUrl = "https://www.bbc.com/arabic/articles/recirculated-cape-good-hope-archive",
                canonicalUrl = "https://www.bbc.com/arabic/articles/recirculated-cape-good-hope-archive",
                contentHash = "hash_bbc_recirculated_cape_good_hope",
                snippet = "تقرير تحليلي يعيد تلخيص الأرقام الملاحية المعلنة قبل أشهر حول تكلفة وقود الشحن البحري الإضافية دون استناد لأي بيانات مالية جديدة للأسبوع الجاري.",
                author = "قسم الاقتصاد - بي بي سي",
                sourceId = 6,
                sourceName = "بي بي سي عربي",
                sourceTier = SourceTier.TRUSTED_MEDIA,
                language = "ar",
                publishedAt = now - 86400000L * 4, // 4 days ago
                fetchedAt = now - 3600000L * 6,
                ingestionMethod = FetchMethod.RSS,
                isDuplicate = false,
                classification = ArticleClassification.RECIRCULATED, // Demonstrates recirculated classification
                analystNotes = "خبر معاد التدوير: يعيد تكرار أرقام الربع السابق دون وجود أي بيانات جديدة.",
                primaryCountryCode = "EG",
                topicId = 1,
                politicalFileId = 1,
                importanceScore = 40,
                isVerified = true
            )
        )
        articleDao.insertArticles(articles)

        // 9. Phase 2: Seed Initial Ingestion Logs
        val ingestionLogDao = database.ingestionLogDao()
        val seedLogs = listOf(
            IngestionLog(
                id = 1,
                sourceId = 1,
                sourceName = "وكالة الأنباء السعودية (واس)",
                ingestionMethod = FetchMethod.RSS,
                startedAt = now - 3600000L * 2 - 4000,
                completedAt = now - 3600000L * 2,
                status = IngestionStatus.SUCCESS,
                fetchedCount = 10,
                insertedCount = 1,
                duplicateCount = 0,
                errorCount = 0
            ),
            IngestionLog(
                id = 2,
                sourceId = 2,
                sourceName = "وكالة رويترز (الشرق الأوسط)",
                ingestionMethod = FetchMethod.RSS,
                startedAt = now - 3600000L * 1 - 3200,
                completedAt = now - 3600000L * 1,
                status = IngestionStatus.SUCCESS,
                fetchedCount = 12,
                insertedCount = 1,
                duplicateCount = 2, // Demonstrates duplicate tracking in logs
                errorCount = 0
            ),
            IngestionLog(
                id = 3,
                sourceId = 5,
                sourceName = "بوابة وزارة الخارجية المصرية",
                ingestionMethod = FetchMethod.OFFICIAL_SITE,
                startedAt = now - 3600000L * 12 - 5100,
                completedAt = now - 3600000L * 12,
                status = IngestionStatus.SUCCESS,
                fetchedCount = 1,
                insertedCount = 1,
                duplicateCount = 0,
                errorCount = 0
            )
        )
        for (l in seedLogs) {
            ingestionLogDao.insertLog(l)
        }

        // 10. Initial Activity Logs
        val logDao = database.activityLogDao()
        logDao.insertLog(
            ActivityLog(
                userId = 1,
                actionType = "SYSTEM_INITIALIZED",
                entityTitle = "مكتب الذكاء والمتابعة السياسية",
                details = "تم تهيئة قاعدة البيانات المحلية ومحرك جلب الأخبار (Phase 2 Ingestion Engine) بنجاح."
            )
        )

        // 11. Phase 3: Seed Political Events
        val eventDao = database.politicalEventDao()
        val seedEvents = listOf(
            PoliticalEvent(
                id = 1,
                titleAr = "محادثات التنسيق الأمني والملاحي لأمن البحر الأحمر وباب المندب",
                titleEn = "Red Sea & Bab el-Mandeb Maritime Security Coordination Talks",
                summaryAr = "مشاورات مكثفة تقودها الرياض والقاهرة والدول الإقليمية الشاطئية لبلورة مظلة أمنية وبحرية لحماية الملاحة التجارية، وتفعيل الاتفاقيات المشتركة في مجلس الدول المشاطئة للبحر الأحمر.",
                summaryEn = "Intensive consultations led by Riyadh and Cairo to establish a joint security mechanism for international shipping lanes in the Red Sea.",
                description = "استمرار المشاورات التنسيقية على مستوى وزراء الخارجية وقادة القوات البحرية لبحث سبل تعزيز تأمين الممرات الحيوية للتجارة العالمية ومواجهة التهديدات غير التقليدية.",
                status = EventStatus.DEVELOPING,
                importance = EventImportance.CRITICAL,
                startDate = now - 86400000L * 4,
                latestUpdate = now - 3600000L * 2,
                location = "الرياض / القاهرة / البحر الأحمر",
                primaryCountryCode = "SA",
                linkedCountryCodes = "SA,EG,YE",
                linkedOrgNames = "مجلس الدول المطلة على البحر الأحمر, القوات البحرية الملكية السعودية",
                linkedPersonNames = "الأمير فيصل بن فرحان, بدر عبد العاطي",
                topicId = 2,
                topicName = "الأمن القومي والدفاع",
                politicalFileId = 2,
                politicalFileTitle = "ملف أمن الملاحة والممرات البحرية في البحر الأحمر",
                articleCount = 3,
                latestDevelopmentAr = "واس: اتصال وزاري رفيع المستوى بين الرياض والقاهرة لتنسيق الموقف المشترك",
                whatChangedAr = "تطابق في الموقف الدبلوماسي بين البلدين مع إعلان تأكيد الحماية المشتركة ودعوة لعقد اجتماع للمجلس الوزاري الإقليمي",
                independentSourceCount = 3,
                isVerified = true,
                verificationSummary = "مدعوم ببيانات رسمية مباشرة من وزارتي الخارجية السعودية والمصرية"
            ),
            PoliticalEvent(
                id = 2,
                titleAr = "المسار التفاوضي للتهدئة الإقليمية وخفض التصعيد والملف الإنساني",
                titleEn = "Regional De-escalation and Humanitarian Negotiation Track",
                summaryAr = "جولات مشاورات غير مباشرة تقودها مصر وقطر بمشاركة دولية لتقريب وجهات النظر والتوصل إلى اتفاق تهدئة شامل وتبادل الأسرى والمحتجزين وإدخال المساعدات الإنسانية.",
                summaryEn = "Indirect mediation talks facilitated by Egypt and Qatar aiming for a regional ceasefire and humanitarian aid access.",
                description = "مباحثات مستمرة تتنقل بين العواصم الإقليمية لبلورة صيغ توافقية تضمن الوقف الدائم للعمليات وإعادة فتح المعابر الحدودية بشكل مستدام.",
                status = EventStatus.ACTIVE,
                importance = EventImportance.CRITICAL,
                startDate = now - 86400000L * 10,
                latestUpdate = now - 3600000L * 5,
                location = "الدوحة / القاهرة",
                primaryCountryCode = "EG",
                linkedCountryCodes = "EG,QA",
                linkedOrgNames = "جامعة الدول العربية, الأمم المتحدة",
                linkedPersonNames = "بدر عبد العاطي, الشيخ محمد بن عبد الرحمن آل ثاني",
                topicId = 1,
                topicName = "العلاقات الدبلوماسية والمفاوضات",
                politicalFileId = 1,
                politicalFileTitle = "ملف المفاوضات والوساطة الدبلوماسية وخفض التصعيد",
                articleCount = 4,
                latestDevelopmentAr = "الخارجية المصرية: لقاء تشاوري مع وفد الوسطاء لمراجعة بنود المقترح المحدث",
                whatChangedAr = "طرح تعديلات إضافية حول آلية دخول الإغاثة ومراقبة الانسحاب التدريجي",
                independentSourceCount = 3,
                isVerified = true,
                verificationSummary = "مدعوم بتصريحات علنية للوزراء والوسطاء الرسميين"
            ),
            PoliticalEvent(
                id = 3,
                titleAr = "تفعيل الشراكة الاستثمارية والصناعية ومشاريع الربط الكهربائي بين السعودية ومصر",
                titleEn = "Saudi-Egyptian Investment Partnership and Energy Interconnection",
                summaryAr = "بدء مرحلة التنفيذ الفعلي لاتفاقية حماية وتشجيع الاستثمارات المتبادلة بين الرياض والقاهرة وضخ استثمارات استراتيجية لصندوق الاستثمارات العامة في البنية التحتية والطاقة المتجددة.",
                summaryEn = "Implementation phase of mutual investment protection pact and sovereign fund investments in green energy.",
                description = "اجتماعات تنسيقية لمجلس الأعمال المشترك واللجنة الوزارية لمتابعة الربط الكهربائي بقدرة 3000 ميجاوات وتسهيل تدفق رؤوس الأموال للقطاعات الإنتاجية.",
                status = EventStatus.RESOLVED,
                importance = EventImportance.HIGH,
                startDate = now - 86400000L * 20,
                latestUpdate = now - 86400000L * 1,
                location = "القاهرة",
                primaryCountryCode = "EG",
                linkedCountryCodes = "EG,SA",
                linkedOrgNames = "صندوق الاستثمارات العامة (PIF), الهيئة العامة للاستثمار والمناطق الحرة (GAFI)",
                linkedPersonNames = "الأمير محمد بن سلمان, مصطفى مدبولي",
                topicId = 4,
                topicName = "الاقتصاد السياسي والطاقة",
                politicalFileId = 3,
                politicalFileTitle = "ملف الشراكة الاقتصادية السعودية المصرية ومشاريع الطاقة",
                articleCount = 2,
                latestDevelopmentAr = "مجلس الوزراء المصري يقر الحوافز الخاصة بالشركات السعودية المشتركة",
                whatChangedAr = "إصدار القرارات التنفيذية للحوافز الضريبية والجمركية",
                independentSourceCount = 2,
                isVerified = true,
                verificationSummary = "وثائق وقرارات حكومية منشورة في الجريدة الرسمية"
            )
        )
        eventDao.insertEvents(seedEvents)

        // 12. Link Seed Articles to Events
        articleDao.linkArticleToEvent(1, 1, "محادثات التنسيق الأمني والملاحي لأمن البحر الأحمر وباب المندب")
        articleDao.linkArticleToEvent(2, 2, "المسار التفاوضي للتهدئة الإقليمية وخفض التصعيد والملف الإنساني")
        articleDao.linkArticleToEvent(3, 3, "تفعيل الشراكة الاستثمارية والصناعية ومشاريع الربط الكهربائي بين السعودية ومصر")

        eventDao.insertArticleEventCrossRef(ArticleEventCrossRef(articleId = 1, eventId = 1, isPrimary = true))
        eventDao.insertArticleEventCrossRef(ArticleEventCrossRef(articleId = 2, eventId = 2, isPrimary = true))
        eventDao.insertArticleEventCrossRef(ArticleEventCrossRef(articleId = 3, eventId = 3, isPrimary = true))

        // 13. Seed Verbatim Statements (Quotes)
        val statementDao = database.statementDao()
        val seedStatements = listOf(
            Statement(
                id = 1,
                personId = 1,
                personName = "الأمير فيصل بن فرحان",
                roleTitle = "وزير الخارجية السعودي",
                quoteText = "أمن البحر الأحمر وسلامة الملاحة في مضيق باب المندب يمثلان أولوية استراتيجية عليا للمملكة ولمنظومة التجارة والاقتصاد العالمي، ولا يمكن التهاون مع أي تهديد للممرات الحيوية.",
                sourceId = 1,
                sourceName = "وكالة الأنباء السعودية (واس)",
                statementDate = now - 3600000L * 2,
                eventId = 1,
                articleId = 1,
                language = "ar",
                originalUrl = "https://www.spa.gov.sa/redsea-statement",
                context = "مؤتمر صحفي مشترك عقب جلسة المشاورات التنسيقية بالرياض",
                isOfficialDeclaration = true
            ),
            Statement(
                id = 2,
                personId = 2,
                personName = "د. بدر عبد العاطي",
                roleTitle = "وزير الخارجية المصري",
                quoteText = "مصر تؤكد أن حرية الملاحة الدولية في البحر الأحمر ترتبط ارتباطًا وثيقًا بالأمن القومي لمصر وعائدات قناة السويس، وندعو لحلول سياسية شاملة تعالج الجذور الإقليمية للأزمة.",
                sourceId = 5,
                sourceName = "بوابة وزارة الخارجية المصرية",
                statementDate = now - 3600000L * 12,
                eventId = 1,
                articleId = 1,
                language = "ar",
                originalUrl = "https://www.mfa.gov.eg/redsea-security",
                context = "بيان رسمي صادر عن وزارة الخارجية عقب الاتصال بنظيره السعودي",
                isOfficialDeclaration = true
            ),
            Statement(
                id = 3,
                personId = 4,
                personName = "الشيخ محمد بن عبد الرحمن آل ثاني",
                roleTitle = "رئيس مجلس الوزراء وزير الخارجية القطري",
                quoteText = "الجهود الدبلوماسية المشتركة مع الأشقاء في مصر وشركائنا الدوليين مستمرة دون انقطاع لتذليل آخر العقبات الإجرائية أمام مسار التهدئة المستدامة.",
                sourceId = 2,
                sourceName = "رويترز (Reuters Arabic)",
                statementDate = now - 3600000L * 6,
                eventId = 2,
                articleId = 2,
                language = "ar",
                originalUrl = "https://ara.reuters.com/article/qatar-gaza-talks",
                context = "تصريحات للصحفيين بمقر وزارة الخارجية بالدوحة",
                isOfficialDeclaration = true
            )
        )
        statementDao.insertStatements(seedStatements)

        // 14. Seed Claims with Confidence Scores & Evidentiary Rationales
        val claimDao = database.claimDao()
        val seedClaims = listOf(
            Claim(
                id = 1,
                statement = "المملكة ومصر تبحثان إنشاء آلية بحرية مشتركة لتبادل المعلومات الرادارية وتأمين حركة السفن التجارية بالبحر الأحمر",
                articleId = 1,
                eventId = 1,
                personId = 1,
                personName = "الأمير فيصل بن فرحان",
                organizationId = 2,
                organizationName = "مجلس الدول المطلة على البحر الأحمر",
                claimDate = now - 3600000L * 2,
                verificationStatus = VerificationStatus.CONFIRMED,
                confidenceScore = 95,
                confidenceReasonAr = "مدعوم ببيان وزاري رسمي مباشر صادر عن وكالتي الأنباء الرسميتين في البلدين (واس وأ ش أ)",
                evidenceCount = 2
            ),
            Claim(
                id = 2,
                statement = "حجم انخفاض حركة عبور السفن التجارية عبر مضيق باب المندب بلغ 58% مقارنة بمعدلات العام الماضي",
                articleId = 1,
                eventId = 1,
                organizationId = 2,
                organizationName = "المنظمة البحرية الدولية",
                claimDate = now - 3600000L * 8,
                verificationStatus = VerificationStatus.SUPPORTED,
                confidenceScore = 88,
                confidenceReasonAr = "مطابق لبيانات الملاحة المستقلة الصادرة عن هيئة قناة السويس ومؤشرات منصة Lloyd's List الاستخباراتية",
                evidenceCount = 2
            ),
            Claim(
                id = 3,
                statement = "جولة المحادثات القادمة في الدوحة ستشهد حضور وفود رفيعة المستوى لإقرار المرحلة التنفيذية للتهدئة",
                articleId = 2,
                eventId = 2,
                personId = 4,
                personName = "الشيخ محمد بن عبد الرحمن آل ثاني",
                claimDate = now - 3600000L * 6,
                verificationStatus = VerificationStatus.LIKELY,
                confidenceScore = 78,
                confidenceReasonAr = "تطابق تقارير رويترز والشرق الأوسط، مع انتظار صدور جدول المواعيد النهائي الرسمي",
                evidenceCount = 2
            )
        )
        claimDao.insertClaims(seedClaims)

        // 15. Seed Evidence Items (Official Docs, Statements, Independent Reports)
        val evidenceDao = database.evidenceDao()
        val seedEvidence = listOf(
            EvidenceItem(
                id = 1,
                claimId = 1,
                eventId = 1,
                articleId = 1,
                sourceId = 1,
                sourceName = "وكالة الأنباء السعودية (واس)",
                sourceTier = SourceTier.PRIMARY,
                evidenceType = EvidenceType.OFFICIAL_DOCUMENT,
                evidenceStrength = EvidenceStrength.STRONG,
                date = now - 3600000L * 2,
                url = "https://www.spa.gov.sa/redsea-bilateral",
                excerpt = "عقد وزيرا خارجية المملكة ومصر اجتماعًا لبحث التنسيق الأمني والبحري المشترك، ومتابعة تفعيل اتفاقيات المجلس الإقليمي للدول المطلة.",
                notes = "بيان رسمي أصيل ومباشر لا يقبل التأويل",
                isDirectOrigin = true
            ),
            EvidenceItem(
                id = 2,
                claimId = 1,
                eventId = 1,
                articleId = 1,
                sourceId = 5,
                sourceName = "بوابة وزارة الخارجية المصرية",
                sourceTier = SourceTier.PRIMARY,
                evidenceType = EvidenceType.OFFICIAL_STATEMENT,
                evidenceStrength = EvidenceStrength.STRONG,
                date = now - 3600000L * 2,
                url = "https://www.mfa.gov.eg/redsea-security-cooperation",
                excerpt = "تأكيد مشترك على أهمية ضمان سلامة الملاحة الدولية في ممرات البحر الأحمر وتوحيد الرؤى الدفاعية للدول الشاطئية.",
                notes = "تطابق تام مع الإفادة الرسمية السعودية الصادرة في التوقيت ذاته",
                isDirectOrigin = true
            ),
            EvidenceItem(
                id = 3,
                claimId = 2,
                eventId = 1,
                articleId = 1,
                sourceId = 2,
                sourceName = "رويترز (Reuters Arabic)",
                sourceTier = SourceTier.AGENCY,
                evidenceType = EvidenceType.INDEPENDENT_REPORT,
                evidenceStrength = EvidenceStrength.STRONG,
                date = now - 3600000L * 8,
                url = "https://ara.reuters.com/article/shipping-redsea-stats",
                excerpt = "التقارير الملاحية التتبعية تظهر تحول أكثر من نصف شاحنات الحاويات نحو طريق رأس الرجاء الصالح، مسببة تراجعًا بنحو 58% في عبور باب المندب.",
                notes = "تقرير مستقل يستند لبيانات تتبع AIS العالمية",
                isDirectOrigin = true
            )
        )
        evidenceDao.insertAllEvidence(seedEvidence)

        // 16. Seed Event Timelines (Chronological Milestones)
        val timelineDao = database.timelineDao()
        val seedTimelines = listOf(
            TimelineItem(
                id = 1,
                eventId = 1,
                timestamp = now - 86400000L * 4,
                titleAr = "إعلان التحركات التنسيقية الأولية لحماية خطوط الملاحة",
                titleEn = "Initial Coordination Announced",
                descriptionAr = "دعوة مشتركة من الرياض والقاهرة لعقد مشاورات أمنية عاجلة بين وزراء خارجية الدول المطلة على البحر الأحمر.",
                sourceName = "واس",
                sourceTier = SourceTier.PRIMARY,
                importance = EventImportance.HIGH,
                isMilestone = true
            ),
            TimelineItem(
                id = 2,
                eventId = 1,
                timestamp = now - 86400000L * 2,
                titleAr = "تبادل تقارير الرصد الملاحي المشترك",
                titleEn = "Joint Maritime Intelligence Exchange",
                descriptionAr = "استعراض تقارير تقييم المخاطر البحرية حول مضيق باب المندب ومداخل خليج عدن بمشاركة هيئات الموانئ.",
                sourceName = "الخارجية المصرية",
                sourceTier = SourceTier.PRIMARY,
                importance = EventImportance.MEDIUM,
                isMilestone = false
            ),
            TimelineItem(
                id = 3,
                eventId = 1,
                timestamp = now - 3600000L * 2,
                titleAr = "جلسة مباحثات استراتيجية رفيعة وتأكيد جاهزية المظلة المشتركة",
                titleEn = "High-Level Bilateral Talks",
                descriptionAr = "اتصال وزاري وتوافق كامل على دعم أمن الملاحة وعقد اجتماع استثنائي للمجلس المشاطئ في الرياض.",
                sourceName = "واس / الخارجية المصرية",
                sourceTier = SourceTier.PRIMARY,
                importance = EventImportance.CRITICAL,
                isMilestone = true
            )
        )
        timelineDao.insertTimelineItems(seedTimelines)

        // 17. Seed Contradiction Item (Cross-Source Discrepancy under review)
        val contradictionDao = database.contradictionDao()
        val seedContradiction = ContradictionItem(
            id = 1,
            eventId = 1,
            claimId = 1,
            contradictionType = ContradictionType.DATE_TIME,
            claimAStatement = "مصادر دبلوماسية ترجح عقد الاجتماع الوزاري الطارئ يوم الثلاثاء في الرياض",
            claimASource = "صحيفة الشرق الأوسط",
            claimADate = now - 3600000L * 8,
            claimBStatement = "وكالة الأنباء الرسمية تؤكد أن موعد الاجتماع لم يتحدد رسميًا بعد وبانتظار استكمال المشاورات",
            claimBSource = "وكالة الأنباء السعودية (واس)",
            claimBDate = now - 3600000L * 3,
            descriptionAr = "تعارض في تحديد موعد الاجتماع الوزاري الطارئ لمجلس الدول المطلة على البحر الأحمر؛ صحيفة الشرق الأوسط حددته بيوم الثلاثاء بينما تنفي واس حسم الموعد حتى اللحظة.",
            status = ContradictionStatus.OPEN,
            analystNotes = "الاعتماد على إفادة واس الرسمية كمرجع نهائي، مع إبقاء الرصد مفتوحًا لتأكيد الموعد النهائي."
        )
        contradictionDao.insertContradiction(seedContradiction)

        // 18. Seed Smart Alert Rules (Configured by Analyst)
        val alertDao = database.alertDao()
        val seedRules = listOf(
            AlertRule(
                id = 1,
                userId = 1,
                name = "رصد تطورات ملف أمن البحر الأحمر",
                alertType = AlertType.IMPORTANT_DEVELOPMENT,
                politicalFileId = 2,
                politicalFileTitle = "ملف أمن الملاحة والممرات البحرية في البحر الأحمر",
                minSeverity = AlertSeverity.HIGH,
                enabled = true
            ),
            AlertRule(
                id = 2,
                userId = 1,
                name = "متابعة تصريحات وزير الخارجية السعودي",
                alertType = AlertType.NEW_STATEMENT,
                personName = "فيصل بن فرحان",
                minSeverity = AlertSeverity.MEDIUM,
                enabled = true
            ),
            AlertRule(
                id = 3,
                userId = 1,
                name = "رصد التناقضات بين الروايات الإخبارية",
                alertType = AlertType.CONTRADICTION,
                minSeverity = AlertSeverity.HIGH,
                enabled = true
            )
        )
        for (r in seedRules) {
            alertDao.insertAlertRule(r)
        }

        // 19. Seed Active Alerts
        val seedAlerts = listOf(
            AlertItem(
                id = 1,
                userId = 1,
                type = AlertType.IMPORTANT_DEVELOPMENT,
                severity = AlertSeverity.CRITICAL,
                titleAr = "تطور حرج: أمن البحر الأحمر",
                messageAr = "تطابق في الموقف السعودي المصري وتوافق على عقد اجتماع وزاري استثنائي لمجلس الدول المطلة على البحر الأحمر.",
                eventId = 1,
                articleId = 1,
                politicalFileId = 2,
                isRead = false,
                channel = NotificationChannel.IN_APP
            ),
            AlertItem(
                id = 2,
                userId = 1,
                type = AlertType.CONTRADICTION,
                severity = AlertSeverity.HIGH,
                titleAr = "رصد تناقض بين المصادر",
                messageAr = "تعارض في موعد انعقاد الاجتماع الوزاري الطارئ بين وكالة واس وصحيفة الشرق الأوسط.",
                eventId = 1,
                isRead = false,
                channel = NotificationChannel.IN_APP
            ),
            AlertItem(
                id = 3,
                userId = 1,
                type = AlertType.NEW_STATEMENT,
                severity = AlertSeverity.HIGH,
                titleAr = "تصريح رسمي: د. بدر عبد العاطي",
                messageAr = "مصر تؤكد أن حرية الملاحة الدولية في البحر الأحمر ترتبط بالأمن القومي وعائدات قناة السويس.",
                eventId = 1,
                personId = 2,
                isRead = true,
                channel = NotificationChannel.IN_APP
            )
        )
        alertDao.insertAlerts(seedAlerts)

        // 20. Seed Phase 6: Political Relationships Graph
        val relationshipDao = database.politicalRelationshipDao()
        val seedRelationships = listOf(
            PoliticalRelationship(
                id = 1,
                sourceEntityType = EntityType.PERSON,
                sourceEntityId = 1,
                sourceEntityName = "الأمير فيصل بن فرحان",
                targetEntityType = EntityType.PERSON,
                targetEntityId = 2,
                targetEntityName = "د. بدر عبد العاطي",
                relationshipType = RelationshipType.ALLIED_WITH,
                confidence = 0.95f,
                evidenceNature = EvidenceNature.DIRECT_EVIDENCE,
                sourceArticleId = 1,
                sourceEventId = 1,
                sourceFileId = 1,
                sourceName = "وكالة الأنباء السعودية (واس)",
                evidenceSnippet = "اتفاق رسمي وتنسيق استراتيجي مستمر بين وزيري خارجية المملكة ومصر بشأن أمن الملاحة.",
                discoveryMethod = "OFFICIAL_COMMUNIQUE",
                discoveryStatus = DiscoveryStatus.CONFIRMED,
                discoveredAt = now - 86400000L * 2,
                confirmedAt = now - 86400000L * 2,
                validFromUtc = now - 86400000L * 30
            ),
            PoliticalRelationship(
                id = 2,
                sourceEntityType = EntityType.PERSON,
                sourceEntityId = 1,
                sourceEntityName = "الأمير فيصل بن فرحان",
                targetEntityType = EntityType.PERSON,
                targetEntityId = 3,
                targetEntityName = "أنتوني بلينكن",
                relationshipType = RelationshipType.NEGOTIATES_WITH,
                confidence = 0.90f,
                evidenceNature = EvidenceNature.DIRECT_EVIDENCE,
                sourceArticleId = 3,
                sourceEventId = 1,
                sourceFileId = 1,
                sourceName = "رويترز (عربي)",
                evidenceSnippet = "مباحثات هاتفية متكررة وجولات إقليمية لبحث سبل خفض التصعيد في البحر الأحمر.",
                discoveryMethod = "OFFICIAL_COMMUNIQUE",
                discoveryStatus = DiscoveryStatus.CONFIRMED,
                discoveredAt = now - 86400000L * 3,
                confirmedAt = now - 86400000L * 3,
                validFromUtc = now - 86400000L * 60
            ),
            PoliticalRelationship(
                id = 3,
                sourceEntityType = EntityType.PERSON,
                sourceEntityId = 2,
                sourceEntityName = "د. بدر عبد العاطي",
                targetEntityType = EntityType.POLITICAL_FILE,
                targetEntityId = 1,
                targetEntityName = "أمن الملاحة في البحر الأحمر ومضيق باب المندب",
                relationshipType = RelationshipType.INVOLVED_IN,
                confidence = 0.98f,
                evidenceNature = EvidenceNature.DIRECT_EVIDENCE,
                sourceArticleId = 2,
                sourceFileId = 1,
                sourceName = "الأهرام",
                evidenceSnippet = "تصريحات مشددة حول تأثير التهديدات البحرية على قناة السويس والأمن القومي المصري.",
                discoveryMethod = "ANALYST_TAGGING",
                discoveryStatus = DiscoveryStatus.CONFIRMED,
                discoveredAt = now - 86400000L * 5,
                confirmedAt = now - 86400000L * 5,
                validFromUtc = now - 86400000L * 90
            ),
            PoliticalRelationship(
                id = 4,
                sourceEntityType = EntityType.PERSON,
                sourceEntityId = 4,
                sourceEntityName = "أيمن الصفدي",
                targetEntityType = EntityType.PERSON,
                targetEntityId = 1,
                targetEntityName = "الأمير فيصل بن فرحان",
                relationshipType = RelationshipType.AGREES_WITH,
                confidence = 0.88f,
                evidenceNature = EvidenceNature.DIRECT_EVIDENCE,
                sourceArticleId = 5,
                sourceName = "وكالة الأنباء الأردنية (بترا)",
                evidenceSnippet = "تطابق في وجهات النظر خلال أعمال اللجنة الوزارية العربية الإسلامية المشتركة.",
                discoveryMethod = "JOINT_STATEMENT",
                discoveryStatus = DiscoveryStatus.CONFIRMED,
                discoveredAt = now - 86400000L * 4,
                confirmedAt = now - 86400000L * 4,
                validFromUtc = now - 86400000L * 45
            ),
            PoliticalRelationship(
                id = 5,
                sourceEntityType = EntityType.ORGANIZATION,
                sourceEntityId = 1,
                sourceEntityName = "وزارة الخارجية السعودية",
                targetEntityType = EntityType.ORGANIZATION,
                targetEntityId = 2,
                targetEntityName = "وزارة الخارجية المصرية",
                relationshipType = RelationshipType.ALLIED_WITH,
                confidence = 0.96f,
                evidenceNature = EvidenceNature.DIRECT_EVIDENCE,
                sourceName = "بيان مشترك",
                evidenceSnippet = "لجنة المتابعة والتشاور السياسي المشتركة بين البلدين.",
                discoveryMethod = "TREATY_RECORD",
                discoveryStatus = DiscoveryStatus.CONFIRMED,
                discoveredAt = now - 86400000L * 10,
                confirmedAt = now - 86400000L * 10,
                validFromUtc = now - 86400000L * 180
            ),
            PoliticalRelationship(
                id = 6,
                sourceEntityType = EntityType.PERSON,
                sourceEntityId = 3,
                sourceEntityName = "أنتوني بلينكن",
                targetEntityType = EntityType.POLITICAL_FILE,
                targetEntityId = 1,
                targetEntityName = "أمن الملاحة في البحر الأحمر ومضيق باب المندب",
                relationshipType = RelationshipType.INVOLVED_IN,
                confidence = 0.92f,
                evidenceNature = EvidenceNature.DIRECT_EVIDENCE,
                sourceName = "رويترز (عربي)",
                evidenceSnippet = "قيادة التحركات الدبلوماسية الأمريكية لتأمين الملاحة وحشد الشركاء الدوليين.",
                discoveryMethod = "POLICY_RECORD",
                discoveryStatus = DiscoveryStatus.CONFIRMED,
                discoveredAt = now - 86400000L * 8,
                confirmedAt = now - 86400000L * 8,
                validFromUtc = now - 86400000L * 120
            ),
            PoliticalRelationship(
                id = 7,
                sourceEntityType = EntityType.PERSON,
                sourceEntityId = 4,
                sourceEntityName = "أيمن الصفدي",
                targetEntityType = EntityType.POLITICAL_FILE,
                targetEntityId = 3,
                targetEntityName = "أمن الحدود الأردنية السورية ومكافحة شبكات التهريب",
                relationshipType = RelationshipType.INVOLVED_IN,
                confidence = 0.94f,
                evidenceNature = EvidenceNature.DIRECT_EVIDENCE,
                sourceName = "بترا",
                evidenceSnippet = "التنسيق الدبلوماسي والأمني لفرض الاستقرار على الحدود الشمالية ومنع التهريب المنظم.",
                discoveryMethod = "OFFICIAL_STATEMENT",
                discoveryStatus = DiscoveryStatus.CONFIRMED,
                discoveredAt = now - 86400000L * 7,
                confirmedAt = now - 86400000L * 7,
                validFromUtc = now - 86400000L * 80
            ),
            // AI-Discovered candidate relationship for Human-in-the-Loop review
            PoliticalRelationship(
                id = 8,
                sourceEntityType = EntityType.PERSON,
                sourceEntityId = 2,
                sourceEntityName = "د. بدر عبد العاطي",
                targetEntityType = EntityType.PERSON,
                targetEntityId = 3,
                targetEntityName = "أنتوني بلينكن",
                relationshipType = RelationshipType.NEGOTIATES_WITH,
                confidence = 0.79f,
                evidenceNature = EvidenceNature.AI_INFERENCE,
                sourceArticleId = 3,
                sourceName = "رويترز (عربي)",
                evidenceSnippet = "تم رصد تقاطع في الاتصالات الثنائية حول سبل التهدئة وممرات الشحن الدولي.",
                discoveryMethod = "AI_CO_OCCURRENCE_EXTRACTION",
                discoveryStatus = DiscoveryStatus.DISCOVERED,
                discoveredAt = now - 3600000L * 6,
                confirmedAt = null,
                validFromUtc = now - 3600000L * 6
            )
        )
        relationshipDao.insertRelationships(seedRelationships)

        // 21. Seed Phase 6: Early Signals
        val earlySignalDao = database.earlySignalDao()
        val seedSignals = listOf(
            EarlySignalItem(
                id = 1,
                signalTitleAr = "مؤشرات على تكثيف قنوات الاتصال والتنسيق الدبلوماسي في البحر الأحمر",
                signalDescriptionAr = "تم رصد نمط غير معتاد في وتيرة المشاورات السعودية المصرية الأمريكية، ما قد يشير إلى تحضير مبادرة مشتركة لحماية حركة التجارة الدولية.",
                signalCategory = "MEETING_FREQUENCY",
                historicalPattern = "تكرار اللقاءات الثنائية في الأزمات البحرية السابقة مهد لصدور بيان وزاري رباعي وتدشين غرفة عمليات مشتركة.",
                currentEvidence = "توثيق 4 اتصالات ومحادثات تنسيقية خلال 72 ساعة عبر وكالات واس والأهرام ورويترز.",
                alternativeExplanations = "قد تقتصر المحادثات على تبادل التقييمات الأمنية الروتينية دون وجود مسار سياسي تنفيذي وشيك.",
                confidence = 0.76f,
                unknowns = "مدى جاهزية الأطراف الأخرى لتقديم التزامات ملموسة بوقف العمليات العدائية.",
                suggestedMonitoring = "متابعة البيانات الصادرة عقب الاجتماع الوزاري المرتقب لمجلس الدول المطلة على البحر الأحمر.",
                associatedFileId = 1,
                associatedEventId = 1,
                detectedAt = now - 3600000L * 5
            ),
            EarlySignalItem(
                id = 2,
                signalTitleAr = "تحول تدريجي نحو خطاب التهدئة والمفاوضات الاقتصادية الإقليمية",
                signalDescriptionAr = "رصد تراجع نسبي في التصريحات الصدامية وتصاعد مفردات الأمن التشاركي وممرات التنمية الاقتصادية في تصريحات الأطراف الإقليمية.",
                signalCategory = "NARRATIVE_SHIFT",
                historicalPattern = "في المحطات التفاوضية السابقة، سبق تعديل النبرة الإعلامية الشروع في جولات الحوار الأمني غير المعلنة.",
                currentEvidence = "تصريحات الأمير فيصل بن فرحان ود. بدر عبد العاطي التي تركز على 'الحلول المستدامة ومصالح الشعوب الإقليمية'.",
                alternativeExplanations = "مناورة إعلامية مرحلية لاستيعاب الضغوط الدولية قبل استئناف التموضع التفاوضي المتشدد.",
                confidence = 0.70f,
                unknowns = "موقف القوى الإقليمية الموازية وجماعات الضغط الداخلية في كل بلد.",
                suggestedMonitoring = "رصد مضامين افتتاحيات الصحف الرسمية في عواصم المنطقة خلال الأسبوع الجاري.",
                associatedFileId = 2,
                detectedAt = now - 3600000L * 12
            )
        )
        earlySignalDao.insertSignals(seedSignals)

        // 22. Seed Phase 6: Detected Changes
        val detectedChangeDao = database.detectedChangeDao()
        val seedChanges = listOf(
            DetectedChangeItem(
                id = 1,
                entityType = EntityType.PERSON,
                entityId = 1,
                entityName = "الأمير فيصل بن فرحان",
                changeType = "STANCE_SHIFT",
                whatChangedAr = "تركيز مباشر ومكثف على استقرار الممرات المائية وتوسيع إطار التنسيق مع دول البحر الأحمر وخليج عدن",
                whenTimestamp = now - 3600000L * 4,
                comparedWithWhatAr = "مقارنة بالتصريحات السابقة التي ركزت حصراً على التهدئة السياسية والمسارات الإنسانية",
                evidenceSnippet = "التصريح الأخير في المؤتمر الصحفي المشترك: 'استقرار الملاحة التزام سيادي جماعي لجميع الدول المشاطئة'.",
                sourceName = "وكالة الأنباء السعودية (واس)",
                confidence = 0.92f,
                magnitude = 0.80f
            ),
            DetectedChangeItem(
                id = 2,
                entityType = EntityType.POLITICAL_FILE,
                entityId = 1,
                entityName = "أمن الملاحة في البحر الأحمر ومضيق باب المندب",
                changeType = "ESCALATION",
                whatChangedAr = "ارتفاع ملحوظ في الأولوية الاستراتيجية وتكثيف الاتصالات العربية مع كبرى شركات الشحن والمنظمات البحرية",
                whenTimestamp = now - 3600000L * 9,
                comparedWithWhatAr = "مقارنة بالمرحلة السابقة التي اتسمت بالرصد والمتابعة الفردية لكل دولة على حدة",
                evidenceSnippet = "صدور بيانات متزامنة من القاهرة والرياض وعمان تدعو لتنسيق شامل لحماية التجارة البحرية.",
                sourceName = "الأهرام",
                confidence = 0.88f,
                magnitude = 0.75f
            )
        )
        detectedChangeDao.insertChanges(seedChanges)

        // 23. Seed Phase 6: Information Gaps
        val informationGapDao = database.informationGapDao()
        val seedGaps = listOf(
            InformationGapItem(
                id = 1,
                titleAr = "حجم التواجد العسكري غير المعلن في جزر البحر الأحمر الجنوبية",
                descriptionAr = "نقص في البيانات الموثقة المستقلة حول القواعد ونقاط الرصد المتقدمة على الجزر المقابلة لمضيق باب المندب.",
                status = InformationGapStatus.UNKNOWN,
                linkedEntityType = EntityType.POLITICAL_FILE,
                linkedEntityId = 1,
                criticality = FilePriority.HIGH,
                missingDataNeeded = "صور أقمار صناعية وتقارير خبراء أمنيين موثوقين تؤكد تمركز المعدات.",
                identifiedAt = now - 86400000L * 2
            ),
            InformationGapItem(
                id = 2,
                titleAr = "الموعد الدقيق والجدول النهائي للاجتماع الوزاري لمجلس البحر الأحمر",
                descriptionAr = "تعارض بين تصريحات مصادر صحفية وتأكيدات الوكالة الرسمية بشأن موعد انعقاد الاجتماع.",
                status = InformationGapStatus.CONTRADICTED,
                linkedEntityType = EntityType.EVENT,
                linkedEntityId = 1,
                criticality = FilePriority.MEDIUM,
                missingDataNeeded = "بيان رسمي مؤكد من أمانة المجلس أو وزارة الخارجية في دولة الاستضافة.",
                identifiedAt = now - 3600000L * 8
            ),
            InformationGapItem(
                id = 3,
                titleAr = "حجم الخسائر الاقتصادية غير المباشرة لتحويل مسارات الشحن",
                descriptionAr = "تفاوت في التقديرات بين تقارير البنك الدولي، المنظمة البحرية الدولية، والشركات المشغلة.",
                status = InformationGapStatus.PARTIALLY_KNOWN,
                linkedEntityType = EntityType.POLITICAL_FILE,
                linkedEntityId = 1,
                criticality = FilePriority.MEDIUM,
                missingDataNeeded = "تقارير مالية مدققة للربع الأول من العام الجاري لكبرى خطوط الملاحة العالمية.",
                identifiedAt = now - 86400000L * 4
            )
        )
        informationGapDao.insertGaps(seedGaps)

        // 24. Seed Phase 6: Smart Monitor Rules
        val smartMonitorRuleDao = database.smartMonitorRuleDao()
        val seedSmartRules = listOf(
            SmartMonitorRule(
                id = 1,
                nameAr = "مراقبة حركات التحالف والاجتماعات البحرية",
                targetType = EntityType.POLITICAL_FILE,
                targetId = 1,
                targetName = "أمن الملاحة في البحر الأحمر",
                keywordsCommaSeparated = "باب المندب, حارس الازدهار, أسبيدس, ناقلة, استهداف, مضيق",
                sourcesCommaSeparated = "واس, رويترز, الأهرام, بترا",
                minImportance = FilePriority.HIGH,
                timeframeDays = 30,
                isActive = true,
                createdAt = now - 86400000L * 10
            ),
            SmartMonitorRule(
                id = 2,
                nameAr = "رصد التحولات في تصريحات وزراء الخارجية",
                targetType = EntityType.PERSON,
                targetId = 1,
                targetName = "فيصل بن فرحان",
                keywordsCommaSeparated = "مباحثات, هدنة, وساطة, تفاهمات, اجتماع طارئ",
                sourcesCommaSeparated = "واس, الشرق الأوسط",
                minImportance = FilePriority.MEDIUM,
                timeframeDays = 30,
                isActive = true,
                createdAt = now - 86400000L * 8
            )
        )
        smartMonitorRuleDao.insertRules(seedSmartRules)

        // =========================================================================
        // Seed Interviews (Interview Intelligence)
        // =========================================================================
        val interviewDao = database.interviewDao()
        val interviewLinkDao = database.interviewLinkDao()
        val transcriptDao = database.interviewTranscriptDao()

        val seedInterviews = listOf(
            Interview(
                id = 1,
                userId = 1,
                dateUtc = now + 86400000L, // Tomorrow
                startTime = "21:00",
                endTime = "21:45",
                channel = "قناة الحدث",
                program = "نقطة حوار",
                interviewer = "طاهر بركة",
                interviewType = InterviewType.LIVE,
                location = "دبي / استوديو البث الحي",
                subject = "مسارات التهدئة البحرية في البحر الأحمر وانعكاساتها على التسويات الإقليمية",
                description = "مداخلة رئيسية لقراءة الاتصالات الدبلوماسية الخلفية وخريطة انتشار القوات البحرية الدولية ومستقبل الوساطات الخليجية.",
                notes = "المحاور يركز على سرعة الإيقاع والمقارنات الإقليمية. جهز أرقام الشحن وحركة السفن بدقة.",
                status = InterviewStatus.CONFIRMED,
                isPrivate = false,
                linkedFileIds = "1,2",
                linkedEventIds = "1",
                linkedTopicIds = "1,2",
                linkedPersonIds = "1,2",
                linkedCountryCodes = "SA,YE,US,EG",
                durationMinutes = 45,
                createdAt = now - 86400000L * 2,
                updatedAt = now - 86400000L * 2
            ),
            Interview(
                id = 2,
                userId = 1,
                dateUtc = now + 86400000L * 4, // in 4 days
                startTime = "19:30",
                endTime = "20:30",
                channel = "تلفزيون LBCI",
                program = "صالون التحرير",
                interviewer = "جورج صليبي",
                interviewType = InterviewType.TV,
                location = "بيروت - استوديو كفرياسين",
                subject = "المأزق الدستوري اللبناني وتأثير التجاذبات الإقليمية على ملف الرئاسة والقرار 1701",
                description = "حوار معمق حول مصير الترتيبات الأمنية على الحدود اللبنانية، وقنوات التفاوض الأمريكية والفرنسية.",
                notes = "أسئلة مرتقبة حول موقف القوى السياسية من المقترحات الدولية. التمسك بالحياد العلمي والتحليلي.",
                status = InterviewStatus.PLANNED,
                isPrivate = false,
                linkedFileIds = "3",
                linkedEventIds = "2",
                linkedTopicIds = "3",
                linkedPersonIds = "3,4",
                linkedCountryCodes = "LB,US,FR",
                durationMinutes = 60,
                createdAt = now - 86400000L,
                updatedAt = now - 86400000L
            ),
            Interview(
                id = 3,
                userId = 1,
                dateUtc = now - 86400000L * 10, // 10 days ago
                startTime = "20:00",
                endTime = "20:45",
                channel = "قناة الجزيرة",
                program = "ما وراء الخبر",
                interviewer = "جمال ريان",
                interviewType = InterviewType.LIVE,
                location = "عبر الأقمار الاصطناعية / بيروت",
                subject = "قراءة في جولة المباحثات الدبلوماسية غير المعلنة وموازين القوى في الممرات المائية",
                description = "تحليل وقائع الاجتماعات السرية في العواصم الإقليمية وتأثيرها على استقرار التجارة العالمية.",
                notes = "تمت المقابلة بنجاح. ركز المحاور على الخلافات البينية بين الحلفاء.",
                status = InterviewStatus.COMPLETED,
                isPrivate = false,
                linkedFileIds = "1,2",
                linkedEventIds = "1",
                linkedTopicIds = "1",
                linkedPersonIds = "1",
                linkedCountryCodes = "SA,IR,US",
                durationMinutes = 45,
                createdAt = now - 86400000L * 12,
                updatedAt = now - 86400000L * 10
            ),
            Interview(
                id = 4,
                userId = 1,
                dateUtc = now - 86400000L * 22, // 22 days ago
                startTime = "21:00",
                endTime = "21:50",
                channel = "قناة العربية",
                program = "البعد الآخر",
                interviewer = "منتهى الرمحي",
                interviewType = InterviewType.TV,
                location = "دبي",
                subject = "أمن الطاقة وسلاسل الإمداد العالمية في ظل التوترات الجيوسياسية",
                description = "حلقة خاصة حول تكلفة التأمين البحري وتأثير التوترات على أسواق النفط ومشاريع البنية التحتية المشتركة.",
                notes = "تم استخدام بيانات وإحصائيات موثقة من نظام Desk. التقييم ممتاز.",
                status = InterviewStatus.COMPLETED,
                isPrivate = false,
                linkedFileIds = "2",
                linkedTopicIds = "2",
                linkedCountryCodes = "SA,AE,EG",
                durationMinutes = 50,
                createdAt = now - 86400000L * 25,
                updatedAt = now - 86400000L * 22
            )
        )
        seedInterviews.forEach { interviewDao.insertInterview(it) }

        // Seed Interview Links
        val seedLinks = listOf(
            InterviewLink(
                id = 1,
                interviewId = 3,
                url = "https://www.youtube.com/watch?v=mock_intel_interview_01",
                title = "تسجيل الحلقة الكاملة على يوتيوب: ما وراء الخبر",
                source = "قناة الجزيرة",
                type = InterviewLinkType.YOUTUBE,
                dateStr = "قبل 10 أيام",
                notes = "تضمنت 3 مداخلات رئيسية واقتباسات متعددة."
            ),
            InterviewLink(
                id = 2,
                interviewId = 3,
                url = "https://aljazeera.net/programs/behindthenews/interview-102",
                title = "تقرير المحطة النصي وملخص النقاش",
                source = "موقع الجزيرة نت",
                type = InterviewLinkType.CHANNEL_SITE,
                dateStr = "قبل 10 أيام",
                notes = "نص المقال المرافق للحلقة."
            ),
            InterviewLink(
                id = 3,
                interviewId = 4,
                url = "https://www.alarabiya.net/programs/the-other-dimension/energy-security",
                title = "فيديو حلقة البعد الآخر: أمن الطاقة وسلاسل الإمداد",
                source = "قناة العربية",
                type = InterviewLinkType.VIDEO,
                dateStr = "قبل 22 يوماً",
                notes = "تسجيل عالي الدقة متوفر للمشاهدة والتحليل."
            )
        )
        seedLinks.forEach { interviewLinkDao.insertLink(it) }

        // Seed Transcript for Interview 3
        val sampleTranscript = """
المحاور: نرحب بكم مشاهدينا الكرام. أهلاً بضيفنا الدكتور في الاستوديو. أبدأ معك دكتور من التطور الأبرز اليوم: هل تعتقد أن ما يجري في البحر الأحمر تجاوز قواعد الاشتباك المألوفة؟
المحلل: أهلاً بك وبالمشاهدين. في الواقع، إذا قرأنا حركة السفن والبيانات الرسمية لغرف الملاحة الدولية، نجد أن التصعيد محسوب بدقة ومحدد الأهداف. الأطراف تسعى لتحسين أوراقها التفاوضية قبل التوصل إلى التفاهمات الإقليمية الكبرى، ولا أحد يرغب في خوض حرب بحرية مفتوحة.
المحاور: ولكن كيف تفسر استمرار الهجمات رغم وجود تحالفات الحماية الدولية؟
المحلل: الردع البحري في المضائق الضيقة معقد تقنياً، والتجارب التاريخية كأزمة 2019 تثبت أن الحماية العسكرية وحدها لا تكفي ما لم تترافق مع تفاهمات سياسية واقتصادية تعالج جذور التوتر.
المحاور: كلمة أخيرة دكتور، إلى أين تتجه الأمور خلال الأيام القادمة؟
المحلل: المؤشرات المستقاة من قنوات الاتصال الخلفية تؤكد أن مسار الدبلوماسية الهادئة يتقدم، وسنرى قريباً مبادرات لخفض التوتر بدعم من العواصم الوسيطة.
        """.trimIndent()

        transcriptDao.insertTranscript(
            InterviewTranscript(
                id = 1,
                interviewId = 3,
                transcriptText = sampleTranscript,
                language = "ar",
                extractedQuestionsJson = """["هل تعتقد أن ما يجري في البحر الأحمر تجاوز قواعد الاشتباك المألوفة؟","كيف تفسر استمرار الهجمات رغم وجود تحالفات الحماية الدولية؟","إلى أين تتجه الأمور خلال الأيام القادمة؟"]""",
                extractedAnswersJson = """["التصعيد محسوب بدقة ومحدد الأهداف ويهدف لتحسين الأوراق التفاوضية.","الردع البحري في المضائق يحتاج لتفاهمات سياسية كما أثبتت تجربة 2019.","المؤشرات تؤكد تقدم الدبلوماسية الهادئة ومبادرات خفض التوتر."]""",
                extractedKeyPointsJson = """["قواعد الاشتباك البحرية","التفاوض الإقليمي","سلاسل الإمداد","المسار الدبلوماسي"]""",
                createdAt = now - 86400000L * 10
            )
        )
    }
}

