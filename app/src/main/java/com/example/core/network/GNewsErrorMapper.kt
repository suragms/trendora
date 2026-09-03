package com.example.core.network

import java.io.IOException

/**
 * Maps network/HTTP failures to human-friendly messages.
 *
 * Raw exceptions (which expose internals like socket messages or stack traces)
 * are NEVER surfaced to the user — only these clean strings are.
 */
object GNewsErrorMapper {

    /** A stable, user-friendly message when no network connection is available. */
    const val NO_INTERNET_MESSAGE = "No internet connection."

    /** Shown when the app has no API key configured. */
    const val MISSING_KEY_MESSAGE = "News API key is missing. Add your key in local.properties."

    fun fromThrowable(t: Throwable): String = when (t) {
        is IOException -> NO_INTERNET_MESSAGE
        else -> "Unable to load trending news."
    }

    fun fromHttpCode(code: Int, apiKeyConfigured: Boolean): String {
        // Not configured -> don't pretend there's a network problem.
        if (!apiKeyConfigured) return MISSING_KEY_MESSAGE
        return when (code) {
            401, 403 -> "Invalid API key. Check your GNews API key."
            429 -> "API limit reached. Please try again later."
            408, 504 -> "Request timed out. Please try again."
            in 500..599 -> "Server error. Please try again later."
            else -> "Unable to load trending news."
        }
    }
}
