package com.example.core.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

open class GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /** Known non-secret placeholders that must never be treated as real keys. */
    private val placeholderKeys = setOf(
        "",
        "MY_GEMINI_API_KEY",
        "CI_PLACEHOLDER",
        "YOUR_GEMINI_KEY",
        "your_gemini_key_here"
    )

    private val rateLimitedUntilMs = AtomicLong(0L)

    private val apiKey: String
        get() = try {
            val key = BuildConfig.GEMINI_API_KEY.trim()
            if (key in placeholderKeys) "" else key
        } catch (_: Exception) {
            ""
        }

    open val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    /** True while Gemini calls are suppressed after HTTP 429. */
    open val isRateLimited: Boolean
        get() = System.currentTimeMillis() < rateLimitedUntilMs.get()

    open suspend fun generateContent(prompt: String): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            Log.w(TAG, "Gemini API key is not configured")
            return@withContext null
        }
        if (isRateLimited) {
            Log.w(TAG, "Gemini API key is rate-limited; skipping request")
            return@withContext null
        }

        try {
            // Key is only used for the request URL — never logged.
            val url =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // Log status only — never the request URL (contains the key).
                    Log.w(TAG, "Request failed with code: ${response.code}")
                    if (response.code == 429) {
                        val retryAfter = response.header("Retry-After")?.toLongOrNull()
                            ?: DEFAULT_RATE_LIMIT_COOLDOWN_SEC
                        rateLimitedUntilMs.set(
                            System.currentTimeMillis() + retryAfter.coerceIn(1L, 86_400L) * 1000L
                        )
                        Log.w(TAG, "Gemini rate limited; cooldown applied")
                    }
                    return@withContext null
                }
                val responseString = response.body?.string() ?: return@withContext null
                val rootJson = JSONObject(responseString)
                val candidates = rootJson.optJSONArray("candidates") ?: return@withContext null
                if (candidates.length() > 0) {
                    val first = candidates.getJSONObject(0)
                    val content = first.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API", e)
        }
        null
    }

    companion object {
        private const val TAG = "GeminiApiClient"
        private const val DEFAULT_RATE_LIMIT_COOLDOWN_SEC = 15 * 60L
    }
}
