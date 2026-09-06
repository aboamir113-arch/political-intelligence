package com.example.data.ingestion

import com.example.data.model.FetchMethod
import com.example.data.model.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

enum class ApiAuthType {
    NONE,
    BEARER_TOKEN,
    API_KEY_HEADER,
    QUERY_PARAM
}

data class ApiConfig(
    val authType: ApiAuthType = ApiAuthType.NONE,
    val headerName: String? = null,
    val tokenOrKey: String? = null,
    val queryParamKey: String? = null,
    val itemsPath: String = "data", // json key path to articles array, e.g. "articles", "results", "data", or root "items"
    val titleField: String = "title",
    val urlField: String = "url",
    val snippetField: String = "description",
    val dateField: String = "published_at",
    val authorField: String = "author",
    val imageField: String = "image_url"
) {
    companion object {
        fun fromJson(jsonStr: String?): ApiConfig {
            if (jsonStr.isNullOrBlank()) return ApiConfig()
            return try {
                val obj = JSONObject(jsonStr)
                ApiConfig(
                    authType = runCatching { ApiAuthType.valueOf(obj.optString("authType", "NONE")) }.getOrDefault(ApiAuthType.NONE),
                    headerName = obj.optString("headerName", null),
                    tokenOrKey = obj.optString("tokenOrKey", null),
                    queryParamKey = obj.optString("queryParamKey", null),
                    itemsPath = obj.optString("itemsPath", "data"),
                    titleField = obj.optString("titleField", "title"),
                    urlField = obj.optString("urlField", "url"),
                    snippetField = obj.optString("snippetField", "description"),
                    dateField = obj.optString("dateField", "published_at"),
                    authorField = obj.optString("authorField", "author"),
                    imageField = obj.optString("imageField", "image_url")
                )
            } catch (_: Exception) {
                ApiConfig()
            }
        }
    }
}

interface ApiConnector {
    suspend fun fetchArticles(source: Source): Result<List<RawArticle>>
}

class GenericRestApiConnector(private val httpClient: OkHttpClient) : ApiConnector {

    override suspend fun fetchArticles(source: Source): Result<List<RawArticle>> = withContext(Dispatchers.IO) {
        val apiUrl = source.apiUrl?.takeIf { it.isNotBlank() } ?: source.websiteUrl
        if (apiUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("رابط الـ API غير محدد"))
        }

        val config = ApiConfig.fromJson(source.apiConfigJson)
        try {
            var requestUrl = apiUrl
            if (config.authType == ApiAuthType.QUERY_PARAM && !config.queryParamKey.isNullOrBlank() && !config.tokenOrKey.isNullOrBlank()) {
                val delimiter = if (requestUrl.contains("?")) "&" else "?"
                requestUrl += "$delimiter${config.queryParamKey}=${config.tokenOrKey}"
            }

            val requestBuilder = Request.Builder()
                .url(requestUrl)
                .header("User-Agent", "PoliticalDesk-IntelProbe/2.0 (Android; Strategic API Ingestion)")
                .header("Accept", "application/json")

            when (config.authType) {
                ApiAuthType.BEARER_TOKEN -> {
                    if (!config.tokenOrKey.isNullOrBlank()) {
                        requestBuilder.header("Authorization", "Bearer ${config.tokenOrKey}")
                    }
                }
                ApiAuthType.API_KEY_HEADER -> {
                    val hName = config.headerName ?: "X-API-KEY"
                    if (!config.tokenOrKey.isNullOrBlank()) {
                        requestBuilder.header(hName, config.tokenOrKey)
                    }
                }
                else -> {}
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.code == 401 || response.code == 403) {
                return@withContext Result.failure(Exception("AUTHENTICATION_ERROR: رمز المصادقة غير صالح أو مرفوض (${response.code})"))
            }
            if (response.code == 429) {
                return@withContext Result.failure(Exception("RATE_LIMITED: تم تجاوز حد الطلبات المسموح به لهذا المصدر (429)"))
            }
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP Error ${response.code}: ${response.message}"))
            }

            val body = response.body?.string() ?: ""
            if (body.isBlank()) {
                return@withContext Result.failure(Exception("استجابة الـ API فارغة"))
            }

            val articles = parseJsonArticles(body, config, source)
            Result.success(articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseJsonArticles(jsonBody: String, config: ApiConfig, source: Source): List<RawArticle> {
        val list = mutableListOf<RawArticle>()
        try {
            var array: JSONArray? = null
            if (jsonBody.trim().startsWith("[")) {
                array = JSONArray(jsonBody)
            } else {
                val root = JSONObject(jsonBody)
                // Search for path
                val keys = config.itemsPath.split(".")
                var current: Any? = root
                for (k in keys) {
                    if (current is JSONObject && current.has(k)) {
                        current = current.get(k)
                    } else {
                        current = null
                        break
                    }
                }
                if (current is JSONArray) {
                    array = current
                } else {
                    // Try common defaults: "articles", "results", "items", "data"
                    for (candidate in listOf("articles", "results", "items", "data")) {
                        if (root.has(candidate) && root.get(candidate) is JSONArray) {
                            array = root.getJSONArray(candidate)
                            break
                        }
                    }
                }
            }

            if (array == null) return emptyList()

            val now = System.currentTimeMillis()
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val title = item.optString(config.titleField, "").trim()
                val url = item.optString(config.urlField, "").trim()
                if (title.isBlank() || url.isBlank()) continue

                val snippet = item.optString(config.snippetField, "").take(600).trim()
                val author = item.optString(config.authorField, null)?.takeIf { it.isNotBlank() }
                val image = item.optString(config.imageField, null)?.takeIf { it.isNotBlank() }
                val dateRaw = item.optString(config.dateField, "")
                val publishedAt = parseDate(dateRaw, now)

                list.add(
                    RawArticle(
                        title = title,
                        originalUrl = url,
                        snippet = snippet,
                        author = author,
                        imageUrl = image,
                        publishedAt = publishedAt,
                        sourceId = source.id,
                        sourceName = source.nameAr,
                        method = FetchMethod.API,
                        language = source.language,
                        rawMetadataJson = item.toString()
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parseDate(dateStr: String, fallback: Long): Long {
        if (dateStr.isBlank()) return fallback
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        )
        for (f in formats) {
            try {
                val d = f.parse(dateStr.trim())
                if (d != null) return d.time
            } catch (_: Exception) {}
        }
        return fallback
    }
}
