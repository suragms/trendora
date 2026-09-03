# Trendora

> AI-powered trend intelligence platform for discovering, analyzing, and understanding what's trending.

<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

## Features

- 🏠 **Home** — Top headlines, breaking news, and category news from the real GNews API.
- 🔎 **Explore** — Search, category filtering, country filtering, and time filtering with client-side recency filtering.
- 🤖 **AI Trends** — Gemini-powered predictive analytics: sentiment, growth prediction, viral probability, and AI confidence.
- 📈 **Trend Details** — Structured AI deep-dive, sentiment breakdown, related topics, and multi-platform discussions.
- 🔖 **Saved** — Bookmark trends and articles locally.
- 👤 **Profile** — User preferences (dark mode, country) stored in DataStore.
- 📴 **Offline-first** — Remote data is cached in Room; when offline or the API fails, the app gracefully shows cached data (or bundled sample data) — never a broken empty screen.
- 🏷️ **Trend Score** — A **Trendora-calculated** 0–100 score (Viral / Rising / Popular / Low Activity) derived from real signals such as recency, source popularity, and category activity. GNews does **not** provide a virality score; this is clearly a Trendora estimate.

## Screenshots

*(Add your screenshots here, e.g. `docs/screenshots/home.png`.)*

## Installation

**Prerequisites:**

- [Android Studio](https://developer.android.com/studio) (with the Android SDK)
- A JDK (bundled with Android Studio)

## API Configuration

Trendora uses two APIs. Configure **both keys in the single, git-ignored `local.properties`** file at the project root (the same file where your Android SDK path lives).

1. Get a free **GNews API key**: https://gnews.io/
2. Get a free **Gemini API key**: https://aistudio.google.com/apikey
3. Create or edit `local.properties` in the project root:

```
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
GNEWS_API_KEY=YOUR_GNEWS_KEY
GEMINI_API_KEY=YOUR_GEMINI_KEY
```

Both keys are injected via `BuildConfig` at build time and are **never** hardcoded in source or committed to Git. `local.properties` and `.env` are git-ignored.

> 🔐 **Never commit a real key.** If you ever do, rotate it immediately.

## Build Instructions

**Debug build** (signed with Android's standard debug keystore — works on a fresh clone with zero extra setup):

```bash
# Windows
gradlew.bat :app:assembleDebug

# macOS / Linux
./gradlew :app:assembleDebug
```

**Run unit tests:**

```bash
gradlew.bat :app:testDebugUnitTest
```

## Generate APK

| Build | Command | Output |
| ----- | ------- | ------ |
| Debug APK | `gradlew :app:assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` |
| Release APK | `gradlew :app:assembleRelease` | `app/build/outputs/apk/release/app-release.apk` |

> **Release signing:** The release build requires a signing keystore and its passwords, provided via environment variables (never hardcoded). See [`RELEASE.md`](RELEASE.md) for full instructions and the `keytool` command to generate your own upload key.

## Run in Android Studio

1. Open Android Studio → **Open** → select this project directory.
2. Let Gradle sync finish (Android Studio installs the Gradle wrapper and dependencies).
3. Add your API keys to `local.properties` (see above).
4. Select a device/emulator and press **Run** (▶).

## Project Architecture

```
RemoteTrendDataSource (GNews)  ─┐
CachedTrendDataSource (Room)   ─┼─► FallbackTrendDataSource ─► Repositories ─► ViewModels ─► Compose UI
MockTrendDataSource (bundled) ─┘
```

The fallback order is: **Remote → Cache → Mock**. The UI is never left on a blank screen.

## Running GitHub Actions CI (optional)

A sample CI workflow is provided at `.github/workflows/ci.yml`. It builds the debug APK and runs unit tests on every push/PR. Secrets (API keys) are **not** published — the build uses placeholder keys so tests that require configuration fall back to their offline/mock paths.

## License / Notes

- Built with Kotlin, Jetpack Compose, MVVM, Room, DataStore, Retrofit, and OkHttp.
- This is a demo/portfolio project using public APIs; data shown may be sample/mock when the API is unavailable or a key is missing.
