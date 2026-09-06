package com.example.data.ingestion

import com.example.data.model.FetchMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

data class ExtractedArticleMetadata(
    val title: String,
    val description: String,
    val availableText: String,
    val author: String?,
    val imageUrl: String?,
    val publishedAt: Long,
    val siteName: String?,
    val originalUrl: String,
    val canonicalUrl: String?
)

class HtmlMetadataExtractor(private val httpClient: OkHttpClient = OkHttpClient()) {

    private val titleTagPattern = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
    private val metaPropertyPattern = Pattern.compile("<meta[^>]+(?:property|name)\\s*=\\s*['\"]([^'\"]+)['\"][^>]+content\\s*=\\s*['\"]([^'\"]*)['\"]", Pattern.CASE_INSENSITIVE)
    private val metaContentFirstPattern = Pattern.compile("<meta[^>]+content\\s*=\\s*['\"]([^'\"]*)['\"][^>]+(?:property|name)\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE)
    private val canonicalPattern = Pattern.compile("<link[^>]+rel\\s*=\\s*['\"]canonical['\"][^>]+href\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE)
    private val paragraphPattern = Pattern.compile("<p[^>]*>(.*?)</p>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
    private val htmlTagPattern = Pattern.compile("<[^>]+>")

    suspend fun extractFromUrl(url: String): Result<ExtractedArticleMetadata> = withContext(Dispatchers.IO) {
        if (url.isBlank() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            return@withContext Result.failure(IllegalArgumentException("رابط غير صالح: يجب أن يبدأ بـ http أو https"))
        }

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "PoliticalDesk-IntelProbe/2.0 (Android; Strategic Political Intelligence)")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("فشل الخادم HTTP ${response.code}: ${response.message}"))
            }

            // Check content type
            val contentType = response.header("Content-Type") ?: ""
            if (!contentType.contains("html") && !contentType.contains("xml") && !contentType.contains("text")) {
                return@withContext Result.failure(Exception("نوع المحتوى غير مدعوم: $contentType"))
            }

            val html = response.body?.string() ?: ""
            if (html.isBlank()) {
                return@withContext Result.failure(Exception("محتوى الصفحة فارغ"))
            }

            val result = parseHtml(html, url)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseHtml(html: String, url: String): ExtractedArticleMetadata {
        val metaMap = mutableMapOf<String, String>()

        // Property then Content
        val m1 = metaPropertyPattern.matcher(html)
        while (m1.find()) {
            val prop = m1.group(1)?.lowercase(Locale.ROOT) ?: ""
            val content = m1.group(2) ?: ""
            if (prop.isNotBlank() && content.isNotBlank()) {
                metaMap[prop] = content
            }
        }

        // Content then Property
        val m2 = metaContentFirstPattern.matcher(html)
        while (m2.find()) {
            val content = m2.group(1) ?: ""
            val prop = m2.group(2)?.lowercase(Locale.ROOT) ?: ""
            if (prop.isNotBlank() && content.isNotBlank() && !metaMap.containsKey(prop)) {
                metaMap[prop] = content
            }
        }

        // Extract title
        val ogTitle = metaMap["og:title"] ?: metaMap["twitter:title"]
        val tagTitle = extractTitleTag(html)
        val finalTitle = (ogTitle ?: tagTitle ?: "").trim()
        val cleanedTitle = if (finalTitle.isNotBlank()) cleanTitle(finalTitle) else "خبر غير معنون"

        // Extract description / snippet
        val ogDesc = metaMap["og:description"] ?: metaMap["twitter:description"] ?: metaMap["description"] ?: ""
        val cleanDesc = stripHtml(ogDesc).trim()

        // Extract canonical link
        val canMatcher = canonicalPattern.matcher(html)
        val canonicalUrl = if (canMatcher.find()) canMatcher.group(1) else metaMap["og:url"]

        // Extract image
        val ogImage = metaMap["og:image"] ?: metaMap["twitter:image"]

        // Extract author
        val author = metaMap["author"] ?: metaMap["article:author"] ?: metaMap["dc.creator"]

        // Extract publication date
        val pubDateStr = metaMap["article:published_time"] ?: metaMap["pubdate"] ?: metaMap["date"] ?: metaMap["og:published_time"]
        val publishedAt = parseMetaDate(pubDateStr)

        // Extract readable text paragraphs (respecting privacy & non-paywall snippets)
        val paragraphs = mutableListOf<String>()
        val pMatcher = paragraphPattern.matcher(html)
        var count = 0
        while (pMatcher.find() && count < 6) { // Extract up to first 6 paragraphs
            val pText = stripHtml(pMatcher.group(1)).trim()
            if (pText.length > 40 && !pText.contains("cookie", ignoreCase = true) && !pText.contains("javascript", ignoreCase = true)) {
                paragraphs.add(pText)
                count++
            }
        }
        val availableText = if (paragraphs.isNotEmpty()) paragraphs.joinToString("\n\n") else cleanDesc

        return ExtractedArticleMetadata(
            title = cleanedTitle,
            description = cleanDesc.ifBlank { availableText.take(280) },
            availableText = availableText,
            author = author?.trim(),
            imageUrl = ogImage?.trim(),
            publishedAt = publishedAt,
            siteName = metaMap["og:site_name"],
            originalUrl = url,
            canonicalUrl = canonicalUrl ?: url
        )
    }

    private fun extractTitleTag(html: String): String? {
        val matcher = titleTagPattern.matcher(html)
        return if (matcher.find()) stripHtml(matcher.group(1)).trim() else null
    }

    private fun cleanTitle(rawTitle: String): String {
        return rawTitle
            .replace(htmlTagPattern.toRegex(), "")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .trim()
    }

    private fun stripHtml(rawHtml: String): String {
        return rawHtml
            .replace(htmlTagPattern.toRegex(), " ")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun parseMetaDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        )
        for (f in formats) {
            try {
                val parsed = f.parse(dateStr.trim())
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }
}
