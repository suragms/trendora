package com.example.core.network

import com.example.data.remote.ApiErrorKind
import java.io.IOException

/**
 * Maps network/HTTP failures to human-friendly messages and [ApiErrorKind].
 *
 * Raw exceptions (which expose internals like socket messages or stack traces)
 * are NEVER surfaced to the user — only these clean strings are.
 */
object GNewsErrorMapper {

    /** A stable, user-friendly message when no network connection is available. */
    const val NO_INTERNET_MESSAGE = "No internet connection."

    /** Shown when the app has no API key configured. */
    const val MISSING_KEY_MESSAGE = "News API key is missing. Add your key in local.properties."

    /** Soft banner when live GNews is rate-limited but Room cache is shown. */
    const val RATE_LIMIT_CACHED_MESSAGE =
        "Live updates temporarily unavailable. Showing recent news."

    /** Soft banner when live GNews is rate-limited and only sample data remains. */
    const val RATE_LIMIT_FALLBACK_MESSAGE = "Live news is temporarily unavailable."

    /** Soft banner for other remote failures when cache is shown. */
    const val LIVE_UNAVAILABLE_CACHED_MESSAGE =
        "Live updates temporarily unavailable. Showing recent news."

    fun fromThrowable(t: Throwable): Pair<String, ApiErrorKind> = when (t) {
        is IOException -> NO_INTERNET_MESSAGE to ApiErrorKind.NETWORK
        else -> "Unable to load trending news." to ApiErrorKind.UNKNOWN
    }

    fun fromHttpCode(code: Int, apiKeyConfigured: Boolean): Pair<String, ApiErrorKind> {
        if (!apiKeyConfigured) return MISSING_KEY_MESSAGE to ApiErrorKind.NOT_CONFIGURED
        return when (code) {
            401 -> "Invalid API key. Check your GNews API key." to ApiErrorKind.UNAUTHORIZED
            403 -> "Access denied by the news service." to ApiErrorKind.FORBIDDEN
            429 -> RATE_LIMIT_FALLBACK_MESSAGE to ApiErrorKind.RATE_LIMITED
            408, 504 -> "Request timed out. Please try again." to ApiErrorKind.NETWORK
            in 500..599 -> "Server error. Please try again later." to ApiErrorKind.SERVER_ERROR
            else -> "Unable to load trending news." to ApiErrorKind.UNKNOWN
        }
    }

    fun kindFromHttpCode(code: Int, apiKeyConfigured: Boolean): ApiErrorKind =
        fromHttpCode(code, apiKeyConfigured).second
}
