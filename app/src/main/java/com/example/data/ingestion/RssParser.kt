package com.example.data.ingestion

import android.util.Xml
import com.example.data.model.FetchMethod
import com.example.data.model.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class RssParser(private val httpClient: OkHttpClient = OkHttpClient()) {

    private val dateFormats = listOf(
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).apply { timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    )

    private val imgTagPattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE)
    private val htmlTagPattern = Pattern.compile("<[^>]+>")

    suspend fun fetchFeed(source: Source): Result<List<RawArticle>> = withContext(Dispatchers.IO) {
        val feedUrl = source.rssUrl?.takeIf { it.isNotBlank() } ?: source.websiteUrl
        if (feedUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("رابط التغذية فارغ"))
        }

        try {
            val request = Request.Builder()
                .url(feedUrl)
                .header("User-Agent", "PoliticalDesk-IntelProbe/2.0 (Android; Strategic Political Intelligence)")
                .header("Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml, */*")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP Error ${response.code}: ${response.message}"))
            }

            val bodyString = response.body?.string() ?: ""
            if (bodyString.isBlank()) {
                return@withContext Result.failure(Exception("محتوى التغذية فارغ"))
            }

            val articles = parseXml(bodyString, source)
            Result.success(articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    internal fun parseXml(xmlContent: String, source: Source): List<RawArticle> {
        val results = mutableListOf<RawArticle>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlContent))

            var eventType = parser.eventType
            var isInsideItem = false
            var isInsideEntry = false // Atom

            var currentTitle: String? = null
            var currentLink: String? = null
            var currentDescription: String? = null
            var currentPubDate: String? = null
            var currentAuthor: String? = null
            var currentImageUrl: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name?.lowercase(Locale.ROOT)

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tagName) {
                            "item" -> {
                                isInsideItem = true
                                currentTitle = null
                                currentLink = null
                                currentDescription = null
                                currentPubDate = null
                                currentAuthor = null
                                currentImageUrl = null
                            }
                            "entry" -> { // Atom feed entry
                                isInsideEntry = true
                                currentTitle = null
                                currentLink = null
                                currentDescription = null
                                currentPubDate = null
                                currentAuthor = null
                                currentImageUrl = null
                            }
                            "title" -> {
                                if (isInsideItem || isInsideEntry) {
                                    currentTitle = readTagText(parser)
                                }
                            }
                            "link" -> {
                                if (isInsideItem) {
                                    val href = parser.getAttributeValue(null, "href")
                                    val text = readTagText(parser)
                                    currentLink = href?.trim()?.ifBlank { null } ?: text.trim()
                                } else if (isInsideEntry) {
                                    val href = parser.getAttributeValue(null, "href")
                                    val text = readTagText(parser)
                                    currentLink = href?.trim()?.ifBlank { null } ?: text.trim()
                                }
                            }
                            "description", "summary", "content:encoded", "content" -> {
                                if (isInsideItem || isInsideEntry) {
                                    val text = readTagText(parser)
                                    if (currentDescription == null || currentDescription.length < text.length) {
                                        currentDescription = text
                                    }
                                    if (currentImageUrl == null) {
                                        currentImageUrl = extractImageFromHtml(text)
                                    }
                                }
                            }
                            "pubdate", "published", "updated", "dc:date" -> {
                                if (isInsideItem || isInsideEntry) {
                                    currentPubDate = readTagText(parser)
                                }
                            }
                            "author", "dc:creator" -> {
                                if (isInsideItem || isInsideEntry) {
                                    currentAuthor = readTagText(parser)
                                }
                            }
                            "enclosure" -> {
                                if (isInsideItem || isInsideEntry) {
                                    val url = parser.getAttributeValue(null, "url")
                                    val type = parser.getAttributeValue(null, "type")
                                    if (url != null && (type == null || type.startsWith("image/"))) {
                                        currentImageUrl = url
                                    }
                                }
                            }
                            "media:content", "media:thumbnail" -> {
                                if (isInsideItem || isInsideEntry) {
                                    val url = parser.getAttributeValue(null, "url")
                                    if (url != null) {
                                        currentImageUrl = url
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if ((tagName == "item" && isInsideItem) || (tagName == "entry" && isInsideEntry)) {
                            if (!currentTitle.isNullOrBlank() && !currentLink.isNullOrBlank()) {
                                val cleanTitle = stripHtml(currentTitle).trim()
                                val cleanSnippet = stripHtml(currentDescription ?: "").take(600).trim()
                                val timestamp = parseDate(currentPubDate)

                                results.add(
                                    RawArticle(
                                        title = cleanTitle,
                                        originalUrl = currentLink,
                                        snippet = cleanSnippet,
                                        fullText = stripHtml(currentDescription ?: ""),
                                        author = currentAuthor?.trim(),
                                        imageUrl = currentImageUrl,
                                        publishedAt = timestamp,
                                        sourceId = source.id,
                                        sourceName = source.nameAr,
                                        method = FetchMethod.RSS,
                                        language = source.language
                                    )
                                )
                            }
                            isInsideItem = false
                            isInsideEntry = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Throwable) {
            // If XmlPullParser is not available (e.g. unmocked in unit test), fall back to DOM
        }

        if (results.isEmpty()) {
            results.addAll(parseWithDom(xmlContent, source))
        }

        return results
    }

    private fun parseWithDom(xmlContent: String, source: Source): List<RawArticle> {
        val domResults = mutableListOf<RawArticle>()
        try {
            val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            factory.isNamespaceAware = false
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(org.xml.sax.InputSource(StringReader(xmlContent)))
            doc.documentElement.normalize()

            // 1. Try RSS <item> tags
            val items = doc.getElementsByTagName("item")
            for (i in 0 until items.length) {
                val node = items.item(i)
                if (node.nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
                    val elem = node as org.w3c.dom.Element
                    val title = getElementFirstChildText(elem, "title")
                    val link = getElementFirstChildText(elem, "link").ifBlank {
                        elem.getElementsByTagName("link").item(0)?.attributes?.getNamedItem("href")?.nodeValue ?: ""
                    }
                    val desc = getElementFirstChildText(elem, "description").ifBlank {
                        getElementFirstChildText(elem, "content:encoded")
                    }.ifBlank {
                        getElementFirstChildText(elem, "summary")
                    }
                    val pubDate = getElementFirstChildText(elem, "pubDate").ifBlank {
                        getElementFirstChildText(elem, "dc:date")
                    }
                    val author = getElementFirstChildText(elem, "author").ifBlank {
                        getElementFirstChildText(elem, "dc:creator")
                    }

                    if (title.isNotBlank() && link.isNotBlank()) {
                        domResults.add(
                            RawArticle(
                                title = stripHtml(title).trim(),
                                originalUrl = link.trim(),
                                snippet = stripHtml(desc).take(600).trim(),
                                fullText = stripHtml(desc).trim(),
                                author = author.trim().ifBlank { null },
                                imageUrl = extractImageFromHtml(desc),
                                publishedAt = parseDate(pubDate),
                                sourceId = source.id,
                                sourceName = source.nameAr,
                                method = FetchMethod.RSS,
                                language = source.language
                            )
                        )
                    }
                }
            }

            // 2. Try Atom <entry> tags if no items found
            if (domResults.isEmpty()) {
                val entries = doc.getElementsByTagName("entry")
                for (i in 0 until entries.length) {
                    val node = entries.item(i)
                    if (node.nodeType == org.w3c.dom.Node.ELEMENT_NODE) {
                        val elem = node as org.w3c.dom.Element
                        val title = getElementFirstChildText(elem, "title")
                        val linkNode = elem.getElementsByTagName("link").item(0)
                        val link = linkNode?.attributes?.getNamedItem("href")?.nodeValue
                            ?: getElementFirstChildText(elem, "link")
                        val desc = getElementFirstChildText(elem, "content").ifBlank {
                            getElementFirstChildText(elem, "summary")
                        }
                        val pubDate = getElementFirstChildText(elem, "published").ifBlank {
                            getElementFirstChildText(elem, "updated")
                        }
                        val author = getElementFirstChildText(elem, "author")

                        if (title.isNotBlank() && link.isNotBlank()) {
                            domResults.add(
                                RawArticle(
                                    title = stripHtml(title).trim(),
                                    originalUrl = link.trim(),
                                    snippet = stripHtml(desc).take(600).trim(),
                                    fullText = stripHtml(desc).trim(),
                                    author = author.trim().ifBlank { null },
                                    imageUrl = extractImageFromHtml(desc),
                                    publishedAt = parseDate(pubDate),
                                    sourceId = source.id,
                                    sourceName = source.nameAr,
                                    method = FetchMethod.RSS,
                                    language = source.language
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
        }
        return domResults
    }

    private fun getElementFirstChildText(parent: org.w3c.dom.Element, tagName: String): String {
        val list = parent.getElementsByTagName(tagName)
        return if (list.length > 0) list.item(0).textContent ?: "" else ""
    }

    private fun readTagText(parser: XmlPullParser): String {
        val sb = StringBuilder()
        var event = parser.next()
        while (event != XmlPullParser.END_TAG && event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.TEXT || event == XmlPullParser.CDSECT) {
                sb.append(parser.text ?: "")
            }
            event = parser.next()
        }
        return sb.toString().trim()
    }

    private fun stripHtml(html: String): String {
        return htmlTagPattern.matcher(html).replaceAll(" ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun extractImageFromHtml(html: String): String? {
        val matcher = imgTagPattern.matcher(html)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun parseDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return System.currentTimeMillis()
        val cleaned = dateStr.trim()
        for (df in dateFormats) {
            try {
                val parsed = df.parse(cleaned)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
        return System.currentTimeMillis()
    }
}
