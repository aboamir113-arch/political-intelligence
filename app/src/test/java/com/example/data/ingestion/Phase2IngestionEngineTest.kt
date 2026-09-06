package com.example.data.ingestion

import com.example.data.local.ArticleDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase2IngestionEngineTest {

    private lateinit var mockArticleDao: TestArticleDao
    private lateinit var duplicateDetector: DuplicateDetectionEngine
    private lateinit var normalizer: ArticleNormalizer
    private lateinit var rssParser: RssParser
    private lateinit var htmlExtractor: HtmlMetadataExtractor

    @Before
    fun setUp() {
        mockArticleDao = TestArticleDao()
        duplicateDetector = DuplicateDetectionEngine(mockArticleDao)
        normalizer = ArticleNormalizer()
        rssParser = RssParser()
        htmlExtractor = HtmlMetadataExtractor()
    }

    @Test
    fun testRssIngestionXmlParsing() {
        val sampleRssXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <title>وكالة الأنباء السعودية - واس</title>
                    <link>https://www.spa.gov.sa</link>
                    <description>أخبار المملكة العربية السعودية والعالم</description>
                    <item>
                        <title>سمو ولي العهد يستقبل رئيس وزراء مصر لبحث العلاقات الثنائية</title>
                        <link>https://www.spa.gov.sa/viewstory.php?newsid=2415600</link>
                        <description><![CDATA[استقبل صاحب السمو الملكي الأمير محمد بن سلمان بن عبدالعزيز، دولة رئيس وزراء مصر لبحث تعزيز الشراكة الاقتصادية وممرات البحر الأحمر.]]></description>
                        <pubDate>Fri, 04 Sep 2026 12:00:00 GMT</pubDate>
                        <guid>spa_2415600</guid>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val source = Source(
            id = 1L,
            nameAr = "واس",
            nameEn = "SPA",
            countryCode = "SA",
            tier = SourceTier.PRIMARY,
            fetchMethod = FetchMethod.RSS,
            websiteUrl = "https://www.spa.gov.sa",
            rssUrl = "https://www.spa.gov.sa/rss.xml"
        )

        val rawArticles = rssParser.parseXml(sampleRssXml, source)
        println("DEBUG: parsed count = ${rawArticles.size}, items = $rawArticles")
        assertEquals(1, rawArticles.size)

        val item = rawArticles.first()
        assertTrue(item.title.contains("ولي العهد يستقبل"))
        assertTrue(item.snippet.contains("رئيس وزراء مصر"))
        assertEquals("https://www.spa.gov.sa/viewstory.php?newsid=2415600", item.originalUrl)
        assertTrue(item.publishedAt > 0)
    }

    @Test
    fun testOfficialSiteHtmlExtraction() {
        val sampleHtml = """
            <!DOCTYPE html>
            <html lang="ar">
            <head>
                <meta charset="UTF-8">
                <title>وزارة الخارجية السعودية تصدر بياناً بشأن استقرار الملاحة</title>
                <meta property="og:title" content="وزارة الخارجية: نؤكد على أهمية حرية الملاحة البحرية الدولية" />
                <meta property="og:description" content="أعربت وزارة الخارجية السعودية عن قلقها إزاء التطورات في مضيق باب المندب، مؤكدة ضرورة حماية الملاحة." />
                <meta property="og:url" content="https://www.mofa.gov.sa/statements/2026/09/maritime-security" />
                <meta name="author" content="واس" />
                <meta property="article:published_time" content="2026-09-04T10:30:00Z" />
            </head>
            <body>
                <main>
                    <p>أكدت وزارة الخارجية في بيان رسمي حرص المملكة التام على أمن ممرات التجارة الدولية في البحر الأحمر.</p>
                </main>
            </body>
            </html>
        """.trimIndent()

        val raw = htmlExtractor.parseHtml(sampleHtml, "https://www.mofa.gov.sa/statements/2026/09/maritime-security")
        assertTrue(raw.title.contains("وزارة الخارجية"))
        assertTrue(raw.description.contains("حماية الملاحة"))
        assertEquals("https://www.mofa.gov.sa/statements/2026/09/maritime-security", raw.originalUrl)
        assertEquals("واس", raw.author)
    }

    @Test
    fun testDuplicateDetectionByUrlMatch() = runBlocking {
        val existing = createSampleArticle(
            id = 101L,
            title = "مباحثات سعودية مصرية لتعزيز أمن البحر الأحمر",
            url = "https://www.spa.gov.sa/news/12345",
            content = "الرياض - عقدت جلسة مباحثات رسمية."
        )
        mockArticleDao.articles.add(existing)

        val candidate = NormalizedArticlePayload(
            title = "مباحثات سعودية مصرية لتعزيز أمن البحر الأحمر",
            normalizedTitle = "مباحثات سعودية مصرية لتعزيز أمن البحر الأحمر",
            originalUrl = "https://www.spa.gov.sa/news/12345",
            canonicalUrl = "https://www.spa.gov.sa/news/12345",
            contentHash = "unique_hash_1",
            snippet = "الرياض - عقدت جلسة مباحثات رسمية.",
            fullText = null,
            author = null,
            imageUrl = null,
            publishedAt = System.currentTimeMillis(),
            sourceId = 2L,
            sourceName = "صحيفة الشرق",
            sourceTier = SourceTier.TRUSTED_MEDIA,
            language = "ar",
            originalAgency = null,
            primaryCountryCode = "SA",
            linkedCountryCodes = "SA,EG",
            linkedPersonNames = "",
            linkedOrgNames = "",
            topicId = null,
            topicName = null,
            politicalFileId = null,
            politicalFileTitle = null,
            importanceScore = 70,
            rawMetadataJson = null
        )

        val resolution = duplicateDetector.evaluate(candidate)
        assertTrue(resolution is DuplicateResolution.Duplicate)
        val duplicate = resolution as DuplicateResolution.Duplicate
        assertEquals(101L, duplicate.primaryArticle.id)
        assertTrue(duplicate.updatedRepublishingJson.contains("صحيفة الشرق"))
    }

    @Test
    fun testDuplicateDetectionByContentHash() = runBlocking {
        val hash = "hash_sha256_exact_match"
        val existing = createSampleArticle(
            id = 102L,
            title = "قناة السويس تعلن حوافز للناقلات",
            url = "https://reuters.com/article/1",
            content = "القاهرة - أعلنت هيئة قناة السويس عن تقديم حوافز جديدة للناقلات البحرية.",
            contentHash = hash
        )
        mockArticleDao.articles.add(existing)

        val candidate = NormalizedArticlePayload(
            title = "حوافز جديدة لناقلات النفط في قناة السويس",
            normalizedTitle = "حوافز جديدة لناقلات النفط في قناة السويس",
            originalUrl = "https://othernews.com/article/different-url",
            canonicalUrl = "https://othernews.com/article/different-url",
            contentHash = hash,
            snippet = "القاهرة - أعلنت هيئة قناة السويس عن تقديم حوافز جديدة للناقلات البحرية.",
            fullText = null,
            author = null,
            imageUrl = null,
            publishedAt = System.currentTimeMillis(),
            sourceId = 3L,
            sourceName = "بلومبرغ الشرق",
            sourceTier = SourceTier.TRUSTED_MEDIA,
            language = "ar",
            originalAgency = null,
            primaryCountryCode = "EG",
            linkedCountryCodes = "EG",
            linkedPersonNames = "",
            linkedOrgNames = "",
            topicId = null,
            topicName = null,
            politicalFileId = null,
            politicalFileTitle = null,
            importanceScore = 65,
            rawMetadataJson = null
        )

        val resolution = duplicateDetector.evaluate(candidate)
        assertTrue(resolution is DuplicateResolution.Duplicate)
        val duplicate = resolution as DuplicateResolution.Duplicate
        assertEquals(102L, duplicate.primaryArticle.id)
    }

    @Test
    fun testDuplicateDetectionByTitleSimilarity() = runBlocking {
        val existing = createSampleArticle(
            id = 103L,
            title = "وزارة الطاقة السعودية ترفع أسعار النفط لشهر أكتوبر لآسيا",
            url = "https://asharq.com/news/1",
            content = "قررت أرامكو السعودية زيادة أسعار البيع الرسمية لآسيا."
        )
        mockArticleDao.articles.add(existing)

        val candidate = NormalizedArticlePayload(
            title = "وزارة الطاقة السعودية ترفع أسعار النفط لشهر أكتوبر لآسيا",
            normalizedTitle = "وزارة الطاقة السعودية ترفع أسعار النفط لشهر أكتوبر لآسيا",
            originalUrl = "https://another-outlet.com/news/99",
            canonicalUrl = "https://another-outlet.com/news/99",
            contentHash = "random_new_hash",
            snippet = "الرياض - زيادة في أسعار بيع النفط.",
            fullText = null,
            author = null,
            imageUrl = null,
            publishedAt = System.currentTimeMillis(),
            sourceId = 4L,
            sourceName = "سكاي نيوز عربية",
            sourceTier = SourceTier.TRUSTED_MEDIA,
            language = "ar",
            originalAgency = null,
            primaryCountryCode = "SA",
            linkedCountryCodes = "SA",
            linkedPersonNames = "",
            linkedOrgNames = "",
            topicId = null,
            topicName = null,
            politicalFileId = null,
            politicalFileTitle = null,
            importanceScore = 75,
            rawMetadataJson = null
        )

        val resolution = duplicateDetector.evaluate(candidate)
        assertTrue(resolution is DuplicateResolution.Duplicate)
        val duplicate = resolution as DuplicateResolution.Duplicate
        assertEquals(103L, duplicate.primaryArticle.id)
    }

    @Test
    fun testRecirculatedNewsDetection() = runBlocking {
        val threeDaysAgo = System.currentTimeMillis() - (72 * 3600 * 1000L) // 72 hours ago
        val existing = createSampleArticle(
            id = 104L,
            title = "تحالف حارس الازدهار ينفذ دوريات بحرية مشتركة في مضيق باب المندب",
            url = "https://news.com/patrols",
            content = "قوات التحالف البحري تنفذ مناورات روتينية.",
            publishedAt = threeDaysAgo
        )
        mockArticleDao.articles.add(existing)

        val candidate = NormalizedArticlePayload(
            title = "تحالف حارس الازدهار ينفذ دوريات بحرية مشتركة في مضيق باب المندب",
            normalizedTitle = "تحالف حارس الازدهار ينفذ دوريات بحرية مشتركة في مضيق باب المندب",
            originalUrl = "https://recirculated-source.com/news/recirc",
            canonicalUrl = "https://recirculated-source.com/news/recirc",
            contentHash = "hash_recirc_999",
            snippet = "يعيد فتح ملف الدوريات البحرية السابقة لتحالف حارس الازدهار دون حدوث وقائع جديدة.",
            fullText = null,
            author = null,
            imageUrl = null,
            publishedAt = System.currentTimeMillis(), // 72 hours later, exact same headline
            sourceId = 5L,
            sourceName = "صحيفة إعادة النشر",
            sourceTier = SourceTier.SECONDARY,
            language = "ar",
            originalAgency = null,
            primaryCountryCode = "YE",
            linkedCountryCodes = "YE",
            linkedPersonNames = "",
            linkedOrgNames = "",
            topicId = null,
            topicName = null,
            politicalFileId = null,
            politicalFileTitle = null,
            importanceScore = 50,
            rawMetadataJson = null
        )

        val resolution = duplicateDetector.evaluate(candidate)
        assertTrue(resolution is DuplicateResolution.Recirculated)
        val recirculated = resolution as DuplicateResolution.Recirculated
        assertEquals(104L, recirculated.originalArticle.id)
    }

    @Test
    fun testArticleNormalizerEntityExtraction() {
        val raw = RawArticle(
            title = "الرياض: وزير الخارجية الأمير فيصل بن فرحان يستقبل مبعوث الأمم المتحدة",
            originalUrl = "https://www.spa.gov.sa/111",
            snippet = "استعرض الجانبان الجهود المشتركة لدعم مسار السلام وتأمين الملاحة في باب المندب.",
            publishedAt = System.currentTimeMillis(),
            sourceId = 1L,
            sourceName = "واس",
            method = FetchMethod.RSS
        )

        val knownCountries = listOf(Country(code = "SA", nameAr = "السعودية", nameEn = "Saudi Arabia", region = "الشرق الأوسط", flagEmoji = "🇸🇦", isStrategicFocus = true))
        val knownPersons = listOf(
            Person(id = 1L, nameAr = "فيصل بن فرحان", nameEn = "Faisal bin Farhan", currentRoleAr = "وزير الخارجية", currentRoleEn = "Minister of Foreign Affairs", countryCode = "SA")
        )
        val knownOrgs = listOf(
            Organization(id = 1L, nameAr = "وزارة الخارجية", nameEn = "Ministry of Foreign Affairs", type = OrganizationType.FOREIGN_MINISTRY, countryCode = "SA", description = "وزارة الخارجية")
        )
        val knownTopics = listOf(
            Topic(id = 1L, nameAr = "باب المندب", nameEn = "Bab el-Mandeb", category = TopicCategory.MARITIME_CHOKEPOINTS, description = "الملاحة والأمن البحري")
        )
        val knownFiles = listOf(
            PoliticalFile(id = 1L, titleAr = "ملف أمن البحر الأحمر", titleEn = "Red Sea Security", description = "متابعة الملاحة", priority = FilePriority.HIGH, status = FileStatus.ACTIVE, topicId = 1L, primaryCountryCode = "SA")
        )

        val normalized = normalizer.normalize(
            raw = raw,
            sourceTier = SourceTier.PRIMARY,
            knownCountries = knownCountries,
            knownPersons = knownPersons,
            knownOrgs = knownOrgs,
            knownTopics = knownTopics,
            knownFiles = knownFiles
        )

        assertEquals("SA", normalized.primaryCountryCode)
        assertTrue(normalized.linkedPersonNames.contains("فيصل بن فرحان"))
        assertEquals(1L, normalized.topicId)
        assertTrue(normalized.contentHash.isNotBlank())
    }

    @Test
    fun testDateFilterCalculations() {
        val now = 1757000000000L
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.timeInMillis
        val last24h = now - (24 * 3600 * 1000L)
        val last48h = now - (48 * 3600 * 1000L)
        val lastWeek = now - (7 * 24 * 3600 * 1000L)

        assertTrue(todayStart <= now)
        assertTrue(last24h < now)
        assertTrue(last48h < last24h)
        assertTrue(lastWeek < last48h)
    }

    private fun createSampleArticle(
        id: Long,
        title: String,
        url: String,
        content: String,
        publishedAt: Long = System.currentTimeMillis(),
        contentHash: String = ""
    ): Article =
        Article(
            id = id,
            sourceId = 1L,
            sourceName = "واس",
            sourceTier = SourceTier.PRIMARY,
            title = title,
            normalizedTitle = title.lowercase(Locale.ROOT),
            snippet = content,
            fullText = content,
            originalUrl = url,
            canonicalUrl = url,
            contentHash = contentHash.ifBlank { "hash_$id" },
            publishedAt = publishedAt,
            fetchedAt = System.currentTimeMillis(),
            classification = ArticleClassification.NEW
        )

    class TestArticleDao : ArticleDao {
        val articles = mutableListOf<Article>()

        override fun getAllArticlesPaged(limit: Int, offset: Int): Flow<List<Article>> = flowOf(articles)

        override fun getPrimaryArticlesPaged(limit: Int, offset: Int): Flow<List<Article>> = flowOf(articles.filter { !it.isDuplicate })

        override fun getFilteredArticles(
            query: String?,
            startUtc: Long,
            endUtc: Long,
            sourceId: Long?,
            topicId: Long?,
            sourceTier: SourceTier?,
            classification: ArticleClassification?,
            countryCode: String?,
            politicalFileId: Long?,
            onlySaved: Int,
            includeDuplicates: Int,
            limit: Int,
            offset: Int
        ): Flow<List<Article>> = flowOf(articles)

        override suspend fun getArticleById(id: Long): Article? = articles.firstOrNull { it.id == id }

        override suspend fun findByOriginalUrl(url: String): Article? = articles.firstOrNull { it.originalUrl == url || it.canonicalUrl == url }

        override suspend fun findByContentHash(contentHash: String): Article? = articles.firstOrNull { it.contentHash == contentHash }

        override suspend fun getRecentArticles(sinceUtc: Long): List<Article> = articles.filter { it.publishedAt >= sinceUtc }

        override suspend fun getAllArticles(limit: Int): List<Article> = articles.take(limit)

        override fun getTotalArticlesCount(): Flow<Int> = flowOf(articles.size)

        override fun getArticlesCountToday(todayStartUtc: Long): Flow<Int> = flowOf(articles.count { it.publishedAt >= todayStartUtc })

        override fun getDuplicatesCount(): Flow<Int> = flowOf(articles.count { it.isDuplicate })

        override suspend fun insertArticle(article: Article): Long {
            val id = if (article.id != 0L) article.id else (articles.size + 1).toLong()
            val toSave = article.copy(id = id)
            articles.add(toSave)
            return id
        }

        override suspend fun insertArticles(articles: List<Article>): List<Long> {
            val ids = mutableListOf<Long>()
            for (a in articles) {
                ids.add(insertArticle(a))
            }
            return ids
        }

        override suspend fun updateArticle(article: Article) {
            val index = articles.indexOfFirst { it.id == article.id }
            if (index >= 0) {
                articles[index] = article
            }
        }

        override suspend fun setArticleSaved(id: Long, isSaved: Boolean) {
            val a = articles.firstOrNull { it.id == id }
            if (a != null) {
                updateArticle(a.copy(isSaved = isSaved))
            }
        }

        override suspend fun linkArticleToFile(articleId: Long, fileId: Long?, fileTitle: String?) {
            val a = articles.firstOrNull { it.id == articleId }
            if (a != null) {
                updateArticle(a.copy(politicalFileId = fileId, politicalFileTitle = fileTitle))
            }
        }

        override suspend fun linkArticleToEvent(articleId: Long, eventId: Long?, eventTitle: String?) {
            val a = articles.firstOrNull { it.id == articleId }
            if (a != null) {
                updateArticle(a.copy(eventId = eventId, eventTitle = eventTitle))
            }
        }

        override fun getArticlesForFile(fileId: Long): Flow<List<Article>> =
            flowOf(articles.filter { it.politicalFileId == fileId })

        override fun getArticlesForEvent(eventId: Long): Flow<List<Article>> =
            flowOf(articles.filter { it.eventId == eventId })

        override fun getArticlesForPerson(personName: String): Flow<List<Article>> =
            flowOf(articles.filter { it.linkedPersonNames?.contains(personName) == true })

        override fun getArticlesForOrg(orgName: String): Flow<List<Article>> =
            flowOf(articles.filter { it.linkedOrgNames?.contains(orgName) == true })

        override suspend fun deleteArticle(article: Article) {
            articles.removeIf { it.id == article.id }
        }

        override suspend fun clearAllArticles() {
            articles.clear()
        }
    }
}
