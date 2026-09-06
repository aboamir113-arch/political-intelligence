package com.example.data.ingestion

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

data class TestResult(
    val state: ConnectionState,
    val message: String,
    val latencyMs: Long = 0,
    val itemsFound: Int = 0
)

class IngestionEngine(
    private val database: AppDatabase,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    private val rssParser = RssParser(httpClient)
    private val htmlExtractor = HtmlMetadataExtractor(httpClient)
    private val apiConnector = GenericRestApiConnector(httpClient)
    private val normalizer = ArticleNormalizer()
    private val duplicateDetector = DuplicateDetectionEngine(database.articleDao())

    suspend fun testSource(source: Source): TestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            when (source.fetchMethod) {
                FetchMethod.RSS -> {
                    val result = rssParser.fetchFeed(source)
                    val latency = System.currentTimeMillis() - startTime
                    if (result.isSuccess) {
                        val items = result.getOrNull() ?: emptyList()
                        val state = if (items.isNotEmpty()) ConnectionState.CONNECTED else ConnectionState.NO_NEW_ITEMS
                        val msg = if (items.isNotEmpty()) "تم جلب ${items.size} خبراً صالحاً بنجاح ($latency ms)" else "تغذية صالحة ولكن لا توجد عناصر حالياً ($latency ms)"
                        database.sourceDao().updateConnectionStatus(source.id, state, msg, System.currentTimeMillis())
                        TestResult(state, msg, latency, items.size)
                    } else {
                        val ex = result.exceptionOrNull()
                        val state = categorizeException(ex)
                        val msg = "فشل فحص RSS: ${ex?.message ?: "خطأ غير معروف"}"
                        database.sourceDao().updateConnectionStatus(source.id, state, msg, System.currentTimeMillis())
                        TestResult(state, msg, latency, 0)
                    }
                }
                FetchMethod.API -> {
                    val result = apiConnector.fetchArticles(source)
                    val latency = System.currentTimeMillis() - startTime
                    if (result.isSuccess) {
                        val items = result.getOrNull() ?: emptyList()
                        val state = if (items.isNotEmpty()) ConnectionState.CONNECTED else ConnectionState.NO_NEW_ITEMS
                        val msg = "واجهة API متصلة بنجاح (${items.size} عناصر، $latency ms)"
                        database.sourceDao().updateConnectionStatus(source.id, state, msg, System.currentTimeMillis())
                        TestResult(state, msg, latency, items.size)
                    } else {
                        val ex = result.exceptionOrNull()
                        val state = if (ex?.message?.contains("AUTHENTICATION_ERROR") == true) ConnectionState.AUTHENTICATION_ERROR else categorizeException(ex)
                        val msg = "فشل اختبار API: ${ex?.message ?: "خطأ غير معروف"}"
                        database.sourceDao().updateConnectionStatus(source.id, state, msg, System.currentTimeMillis())
                        TestResult(state, msg, latency, 0)
                    }
                }
                FetchMethod.OFFICIAL_SITE -> {
                    val result = htmlExtractor.extractFromUrl(source.websiteUrl)
                    val latency = System.currentTimeMillis() - startTime
                    if (result.isSuccess) {
                        val state = ConnectionState.CONNECTED
                        val msg = "الموقع الرسمي متاح وقابل للاستخراج ($latency ms)"
                        database.sourceDao().updateConnectionStatus(source.id, state, msg, System.currentTimeMillis())
                        TestResult(state, msg, latency, 1)
                    } else {
                        val ex = result.exceptionOrNull()
                        val state = categorizeException(ex)
                        val msg = "فشل فحص الموقع الرسمي: ${ex?.message ?: "خطأ غير معروف"}"
                        database.sourceDao().updateConnectionStatus(source.id, state, msg, System.currentTimeMillis())
                        TestResult(state, msg, latency, 0)
                    }
                }
                FetchMethod.MANUAL -> {
                    // Manual source verifies root website URL reachability
                    val result = htmlExtractor.extractFromUrl(source.websiteUrl)
                    val latency = System.currentTimeMillis() - startTime
                    val state = if (result.isSuccess) ConnectionState.CONNECTED else ConnectionState.SLOW
                    val msg = "المصدر اليدوي جاهز لاستقبال الروابط ($latency ms)"
                    database.sourceDao().updateConnectionStatus(source.id, state, msg, System.currentTimeMillis())
                    TestResult(state, msg, latency, 0)
                }
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            val state = categorizeException(e)
            val msg = "خطأ فحص: ${e.message}"
            database.sourceDao().updateConnectionStatus(source.id, state, msg, System.currentTimeMillis())
            TestResult(state, msg, latency, 0)
        }
    }

    private fun categorizeException(ex: Throwable?): ConnectionState {
        if (ex == null) return ConnectionState.FAILED
        val msg = ex.message?.lowercase() ?: ""
        return when {
            ex is SocketTimeoutException || ex is InterruptedIOException || msg.contains("timeout") -> ConnectionState.TIMEOUT
            msg.contains("403") || msg.contains("blocked") || msg.contains("cloudflare") -> ConnectionState.BLOCKED
            msg.contains("401") || msg.contains("unauthorized") -> ConnectionState.AUTHENTICATION_ERROR
            msg.contains("xml") || msg.contains("parse") || msg.contains("format") -> ConnectionState.INVALID_FEED
            else -> ConnectionState.FAILED
        }
    }

    suspend fun ingestSource(source: Source): IngestionLog = withContext(Dispatchers.IO) {
        val startedAt = System.currentTimeMillis()
        var fetchedCount = 0
        var insertedCount = 0
        var duplicateCount = 0
        var errorCount = 0
        var errorDetails: String? = null

        if (source.status == SourceStatus.PAUSED) {
            val log = IngestionLog(
                sourceId = source.id,
                sourceName = source.nameAr,
                ingestionMethod = source.fetchMethod,
                startedAt = startedAt,
                completedAt = System.currentTimeMillis(),
                status = IngestionStatus.NO_NEW_ITEMS,
                errorDetails = "المصدر متوقف مؤقتاً (PAUSED)"
            )
            database.ingestionLogDao().insertLog(log)
            return@withContext log
        }

        try {
            val rawArticles: List<RawArticle> = when (source.fetchMethod) {
                FetchMethod.RSS -> {
                    val res = rssParser.fetchFeed(source)
                    if (res.isFailure) {
                        errorCount++
                        errorDetails = res.exceptionOrNull()?.message
                        emptyList()
                    } else {
                        res.getOrDefault(emptyList())
                    }
                }
                FetchMethod.API -> {
                    val res = apiConnector.fetchArticles(source)
                    if (res.isFailure) {
                        errorCount++
                        errorDetails = res.exceptionOrNull()?.message
                        emptyList()
                    } else {
                        res.getOrDefault(emptyList())
                    }
                }
                FetchMethod.OFFICIAL_SITE -> {
                    val res = htmlExtractor.extractFromUrl(source.websiteUrl)
                    if (res.isFailure) {
                        errorCount++
                        errorDetails = res.exceptionOrNull()?.message
                        emptyList()
                    } else {
                        val meta = res.getOrNull()
                        if (meta != null) {
                            listOf(
                                RawArticle(
                                    title = meta.title,
                                    originalUrl = meta.originalUrl,
                                    snippet = meta.description,
                                    fullText = meta.availableText,
                                    author = meta.author,
                                    imageUrl = meta.imageUrl,
                                    publishedAt = meta.publishedAt,
                                    sourceId = source.id,
                                    sourceName = source.nameAr,
                                    method = FetchMethod.OFFICIAL_SITE,
                                    language = source.language
                                )
                            )
                        } else emptyList()
                    }
                }
                FetchMethod.MANUAL -> emptyList()
            }

            fetchedCount = rawArticles.size

            if (rawArticles.isNotEmpty()) {
                val countries = database.countryDao().getAllCountriesList()
                val persons = database.personDao().getAllPersonsList()
                val orgs = database.organizationDao().getAllOrganizationsList()
                val topics = database.topicDao().getAllTopicsList()
                val files = database.politicalFileDao().getAllFilesList()

                for (raw in rawArticles) {
                    try {
                        val payload = normalizer.normalize(
                            raw = raw,
                            sourceTier = source.tier,
                            knownCountries = countries,
                            knownPersons = persons,
                            knownOrgs = orgs,
                            knownTopics = topics,
                            knownFiles = files
                        )

                        when (val resolution = duplicateDetector.evaluate(payload)) {
                            is DuplicateResolution.Unique -> {
                                val article = Article(
                                    title = payload.title,
                                    normalizedTitle = payload.normalizedTitle,
                                    originalUrl = payload.originalUrl,
                                    canonicalUrl = payload.canonicalUrl,
                                    contentHash = payload.contentHash,
                                    snippet = payload.snippet,
                                    fullText = payload.fullText,
                                    author = payload.author,
                                    imageUrl = payload.imageUrl,
                                    sourceId = payload.sourceId,
                                    sourceName = payload.sourceName,
                                    sourceTier = payload.sourceTier,
                                    language = payload.language,
                                    publishedAt = payload.publishedAt,
                                    fetchedAt = System.currentTimeMillis(),
                                    ingestionMethod = raw.method,
                                    isDuplicate = false,
                                    classification = resolution.classification,
                                    originalAgency = payload.originalAgency,
                                    primaryCountryCode = payload.primaryCountryCode,
                                    linkedCountryCodes = payload.linkedCountryCodes,
                                    linkedPersonNames = payload.linkedPersonNames,
                                    linkedOrgNames = payload.linkedOrgNames,
                                    topicId = payload.topicId,
                                    topicName = payload.topicName,
                                    politicalFileId = payload.politicalFileId,
                                    politicalFileTitle = payload.politicalFileTitle,
                                    importanceScore = payload.importanceScore,
                                    rawMetadataJson = payload.rawMetadataJson
                                )
                                database.articleDao().insertArticle(article)
                                insertedCount++
                            }
                            is DuplicateResolution.Duplicate -> {
                                duplicateCount++
                                // Update primary article's republishing sources
                                val updatedPrimary = resolution.primaryArticle.copy(
                                    republishingSourcesJson = resolution.updatedRepublishingJson,
                                    duplicateCount = resolution.primaryArticle.duplicateCount + 1
                                )
                                database.articleDao().updateArticle(updatedPrimary)

                                // Also record duplicate shadow copy linked to primary
                                val dupArticle = Article(
                                    title = payload.title,
                                    normalizedTitle = payload.normalizedTitle,
                                    originalUrl = payload.originalUrl,
                                    canonicalUrl = payload.canonicalUrl,
                                    contentHash = payload.contentHash,
                                    snippet = payload.snippet,
                                    fullText = payload.fullText,
                                    sourceId = payload.sourceId,
                                    sourceName = payload.sourceName,
                                    sourceTier = payload.sourceTier,
                                    publishedAt = payload.publishedAt,
                                    fetchedAt = System.currentTimeMillis(),
                                    ingestionMethod = raw.method,
                                    isDuplicate = true,
                                    primaryArticleId = resolution.primaryArticle.id,
                                    classification = ArticleClassification.DUPLICATE,
                                    originalAgency = payload.originalAgency
                                )
                                database.articleDao().insertArticle(dupArticle)
                            }
                            is DuplicateResolution.Recirculated -> {
                                // Old news rehashed as new -> Record as RECIRCULATED
                                val recircArticle = Article(
                                    title = payload.title,
                                    normalizedTitle = payload.normalizedTitle,
                                    originalUrl = payload.originalUrl,
                                    canonicalUrl = payload.canonicalUrl,
                                    contentHash = payload.contentHash,
                                    snippet = payload.snippet,
                                    fullText = payload.fullText,
                                    sourceId = payload.sourceId,
                                    sourceName = payload.sourceName,
                                    sourceTier = payload.sourceTier,
                                    publishedAt = payload.publishedAt,
                                    fetchedAt = System.currentTimeMillis(),
                                    ingestionMethod = raw.method,
                                    isDuplicate = false,
                                    primaryArticleId = resolution.originalArticle.id,
                                    classification = ArticleClassification.RECIRCULATED,
                                    analystNotes = "خبر معاد التدوير: يتطابق مع خبر نشر بتاريخ سابق دون تطورات جوهرية",
                                    originalAgency = payload.originalAgency,
                                    primaryCountryCode = payload.primaryCountryCode,
                                    topicId = payload.topicId,
                                    politicalFileId = payload.politicalFileId
                                )
                                database.articleDao().insertArticle(recircArticle)
                                insertedCount++
                            }
                        }
                    } catch (itemEx: Exception) {
                        errorCount++
                    }
                }
            }

            val status = when {
                errorCount > 0 && insertedCount > 0 -> IngestionStatus.PARTIAL
                errorCount > 0 && insertedCount == 0 -> IngestionStatus.FAILED
                fetchedCount == 0 -> IngestionStatus.NO_NEW_ITEMS
                else -> IngestionStatus.SUCCESS
            }

            val completedAt = System.currentTimeMillis()
            val log = IngestionLog(
                sourceId = source.id,
                sourceName = source.nameAr,
                ingestionMethod = source.fetchMethod,
                startedAt = startedAt,
                completedAt = completedAt,
                status = status,
                fetchedCount = fetchedCount,
                insertedCount = insertedCount,
                duplicateCount = duplicateCount,
                errorCount = errorCount,
                errorDetails = errorDetails
            )
            database.ingestionLogDao().insertLog(log)

            // Update source sync timestamp and article total
            val updatedSource = source.copy(
                lastSyncAt = completedAt,
                totalArticlesIngested = source.totalArticlesIngested + insertedCount,
                connectionState = if (status == IngestionStatus.FAILED) ConnectionState.FAILED else ConnectionState.CONNECTED,
                lastTestMessage = "آخر جلب: تم إدراج $insertedCount مقال ($duplicateCount مكرر)"
            )
            database.sourceDao().updateSource(updatedSource)

            log
        } catch (e: Exception) {
            val completedAt = System.currentTimeMillis()
            val log = IngestionLog(
                sourceId = source.id,
                sourceName = source.nameAr,
                ingestionMethod = source.fetchMethod,
                startedAt = startedAt,
                completedAt = completedAt,
                status = IngestionStatus.FAILED,
                fetchedCount = fetchedCount,
                insertedCount = insertedCount,
                duplicateCount = duplicateCount,
                errorCount = errorCount + 1,
                errorDetails = e.message ?: "خطأ أثناء عملية الجلب"
            )
            database.ingestionLogDao().insertLog(log)
            database.sourceDao().updateConnectionStatus(source.id, ConnectionState.FAILED, "فشل الجلب: ${e.message}", completedAt)
            log
        }
    }

    suspend fun ingestAllActiveSources(): List<IngestionLog> = withContext(Dispatchers.IO) {
        val sources = database.sourceDao().getAllSourcesList().filter { it.status == SourceStatus.ACTIVE }
        val logs = mutableListOf<IngestionLog>()
        for (source in sources) {
            try {
                val log = ingestSource(source)
                logs.add(log)
            } catch (_: Exception) {
                // Individual source failure does not break ingestion loop!
            }
        }
        logs
    }

    suspend fun importManualArticle(
        url: String,
        sourceId: Long,
        customTitle: String? = null,
        customSnippet: String? = null,
        customPublishedAt: Long? = null,
        analystNotes: String = ""
    ): Result<Article> = withContext(Dispatchers.IO) {
        val source = database.sourceDao().getSourceById(sourceId)
            ?: return@withContext Result.failure(IllegalArgumentException("المصدر المختار غير موجود"))

        // Extract metadata from web page
        val extractRes = htmlExtractor.extractFromUrl(url)
        val meta = extractRes.getOrNull()

        val finalTitle = customTitle?.takeIf { it.isNotBlank() } ?: meta?.title
        if (finalTitle.isNullOrBlank()) {
            return@withContext Result.failure(Exception("تعذر استخراج عنوان الخبر. يرجى إدخال العنوان يدوياً."))
        }

        val finalSnippet = customSnippet?.takeIf { it.isNotBlank() } ?: meta?.description ?: ""
        val finalPublishedAt = customPublishedAt ?: meta?.publishedAt ?: System.currentTimeMillis()

        val raw = RawArticle(
            title = finalTitle,
            originalUrl = url,
            snippet = finalSnippet,
            fullText = meta?.availableText,
            author = meta?.author,
            imageUrl = meta?.imageUrl,
            publishedAt = finalPublishedAt,
            sourceId = source.id,
            sourceName = source.nameAr,
            method = FetchMethod.MANUAL,
            language = source.language
        )

        val countries = database.countryDao().getAllCountriesList()
        val persons = database.personDao().getAllPersonsList()
        val orgs = database.organizationDao().getAllOrganizationsList()
        val topics = database.topicDao().getAllTopicsList()
        val files = database.politicalFileDao().getAllFilesList()

        val payload = normalizer.normalize(
            raw = raw,
            sourceTier = source.tier,
            knownCountries = countries,
            knownPersons = persons,
            knownOrgs = orgs,
            knownTopics = topics,
            knownFiles = files
        )

        val resolution = duplicateDetector.evaluate(payload)
        val classification = when (resolution) {
            is DuplicateResolution.Unique -> resolution.classification
            is DuplicateResolution.Duplicate -> ArticleClassification.DUPLICATE
            is DuplicateResolution.Recirculated -> ArticleClassification.RECIRCULATED
        }

        val article = Article(
            title = payload.title,
            normalizedTitle = payload.normalizedTitle,
            originalUrl = payload.originalUrl,
            canonicalUrl = payload.canonicalUrl,
            contentHash = payload.contentHash,
            snippet = payload.snippet,
            fullText = payload.fullText,
            author = payload.author,
            imageUrl = payload.imageUrl,
            sourceId = payload.sourceId,
            sourceName = payload.sourceName,
            sourceTier = payload.sourceTier,
            language = payload.language,
            publishedAt = payload.publishedAt,
            fetchedAt = System.currentTimeMillis(),
            ingestionMethod = FetchMethod.MANUAL,
            isDuplicate = resolution is DuplicateResolution.Duplicate,
            primaryArticleId = if (resolution is DuplicateResolution.Duplicate) resolution.primaryArticle.id else null,
            classification = classification,
            analystNotes = analystNotes,
            originalAgency = payload.originalAgency,
            primaryCountryCode = payload.primaryCountryCode,
            linkedCountryCodes = payload.linkedCountryCodes,
            linkedPersonNames = payload.linkedPersonNames,
            linkedOrgNames = payload.linkedOrgNames,
            topicId = payload.topicId,
            topicName = payload.topicName,
            politicalFileId = payload.politicalFileId,
            politicalFileTitle = payload.politicalFileTitle,
            importanceScore = payload.importanceScore,
            rawMetadataJson = payload.rawMetadataJson
        )

        val id = database.articleDao().insertArticle(article)
        val saved = article.copy(id = id)

        // Log manual import
        val log = IngestionLog(
            sourceId = source.id,
            sourceName = source.nameAr,
            ingestionMethod = FetchMethod.MANUAL,
            startedAt = System.currentTimeMillis(),
            completedAt = System.currentTimeMillis(),
            status = IngestionStatus.SUCCESS,
            fetchedCount = 1,
            insertedCount = 1,
            duplicateCount = if (article.isDuplicate) 1 else 0
        )
        database.ingestionLogDao().insertLog(log)

        // Record activity log
        database.activityLogDao().insertLog(
            ActivityLog(
                userId = 1,
                actionType = "MANUAL_IMPORT",
                entityTitle = saved.title,
                details = "إدخال خبر يدوي من المصدر ${source.nameAr}"
            )
        )

        Result.success(saved)
    }
}
