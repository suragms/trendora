package com.example.core.network

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the GNews [Retrofit] service. The API key is read from
 * [BuildConfig.GNEWS_API_KEY], which is injected from the git-ignored
 * `local.properties` at build time.
 */
object GNewsClient {

    private const val BASE_URL = "https://gnews.io/api/v4/"
    private const val TAG = "GNewsClient"

    /** Known non-secret placeholders that must never be treated as real keys. */
    private val PLACEHOLDER_KEYS = setOf(
        "",
        "CI_PLACEHOLDER",
        "YOUR_GNEWS_KEY",
        "your_gnews_key_here"
    )

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        // BASIC would log full URLs (including apikey). Redact secrets before logging.
        val logging = HttpLoggingInterceptor { message ->
            val redacted = message
                .replace(Regex("(?i)apikey=[^&\\s\"']+"), "apikey=***")
                .replace(Regex("(?i)([?&]key)=[^&\\s\"']+"), "$1=***")
            Log.d(TAG, redacted)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    val apiKey: String
        get() {
            val key = BuildConfig.GNEWS_API_KEY.trim()
            return if (key in PLACEHOLDER_KEYS) "" else key
        }

    val isConfigured: Boolean
        get() = apiKey.isNotBlank()

    val apiService: GNewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GNewsApiService::class.java)
    }
}
