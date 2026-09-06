# Trendora

> AI-powered trend intelligence platform for discovering, analyzing, and understanding what's trending in real time.

<div align="center">
<img width="1200" height="475" alt="Trendora banner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

## Overview

Trendora is a modern Android app built with **Kotlin + Jetpack Compose** that pulls **real headlines from the GNews API**, surfaces a **Trendora-calculated 0–100 trend score** for each topic, and layers on **Gemini-powered AI analysis** (sentiment, growth prediction, viral probability, and confidence). It is designed to be **offline-first**: live data is cached in a local Room database so the app never shows a broken blank screen, even when you're offline or the API fails.

**Trendora does not require an account or login.** It is fully anonymous — open the app and start exploring immediately. Everything is stored locally on your device.

## Features

- 🏠 **Home** — Top headlines, breaking news, and category news from the real GNews API.
- 🔎 **Explore** — Search (debounced), category filtering, country filtering, time filtering, and sorting with client-side recency filtering.
- 🤖 **AI Trends** — Gemini-powered predictive analytics: sentiment, growth prediction, viral probability, and AI confidence.
- 📈 **Trend Details** — Structured AI deep-dive, sentiment breakdown, related topics, and multi-platform discussions.
- 🔖 **Saved** — Bookmark trends and articles locally; persists across app restarts via Room.
- ⚙️ **Settings** — Dark mode, preferred region, and trend alert preferences, persisted locally in DataStore.
- 📴 **Offline-first** — Remote → Cache → Mock fallback chain. Never a broken empty screen.
- 🏷️ **Trend Score** — A **Trendora-calculated** 0–100 score (Viral / Rising / Popular / Low Activity) derived from real signals such as recency, source popularity, and category activity. GNews does **not** provide a virality score; this is clearly a Trendora estimate.

## Tech Stack

| Layer | Technology |
| ----- | ---------- |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository + Clean-ish layering |
| Async | Kotlin Coroutines + Flow |
| Local storage | Room (trends, news cache, bookmarks, AI cache, history) |
| Preferences | DataStore (dark mode, country, settings) |
| Networking | Retrofit + OkHttp + Moshi |
| News API | GNews |
| AI | Google Gemini (raw OkHttp) |
| DI | Manual (no framework) |
| Navigation | Jetpack Navigation Compose |
| Images | Coil |
| Testing | JUnit, Robolectric, Roborazzi |
| CI | GitHub Actions |

## Architecture

```
RemoteTrendDataSource (GNews)  ─┐
CachedTrendDataSource (Room)   ─┼─► FallbackTrendDataSource ─► Repositories ─► ViewModels ─► Compose UI
MockTrendDataSource (bundled) ─┘
```

The fallback order is: **Remote → Cache → Mock**. The UI is never left on a blank screen. When remote data is available it is shown immediately and written to cache; when the device is offline or the API errors, the app falls back to the cached copy, then to bundled sample data as a last resort.

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

## Run in Android Studio

1. Open Android Studio → **Open** → select this project directory.
2. Let Gradle sync finish (Android Studio installs the Gradle wrapper and dependencies).
3. Add your API keys to `local.properties` (see above).
4. Select a device/emulator and press **Run** (▶).

## Build APK

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

**Output locations:**

| Build | Command | Output |
| ----- | ------- | ------ |
| Debug APK | `gradlew :app:assembleDebug` | `app/build/outputs/apk/debug/app-debug.apk` |
| Release APK | `gradlew :app:assembleRelease` | `app/build/outputs/apk/release/app-release.apk` |

> **Release signing:** The release build requires a signing keystore and its passwords, provided via environment variables (never hardcoded). See [`RELEASE.md`](RELEASE.md) for full instructions and the `keytool` command to generate your own upload key.

## Offline Mode

Trendora is designed to keep working without a connection:

| Situation | What you see |
| --------- | ------------ |
| Online + API success | Live data from GNews |
| Online + API failure | Cached data (from Room) |
| Online + failure + no cache | Bundled sample data |
| Offline + cache | Cached data |
| Offline + no cache | Bundled sample data |

A subtle status banner tells you which source you're looking at: *"You are offline — showing cached trends."* or *"… showing sample trends."*

## Anonymous Usage — No Account, No Login

Trendora is anonymous by design. There is **no sign-up, sign-in, or account** — no email, no password, no profile. The app works immediately after installation.

- **Saved content** (trends & articles) is stored locally in a Room database and persists across restarts.
- **Recent searches & recently viewed** are kept on-device.
- **AI analysis cache** and **news cache** are stored locally for fast, offline-capable lookups.
- **App preferences** (dark mode, preferred region, trend alerts) are stored in DataStore.

Nothing is uploaded to a user account, because no account exists. Clear search history & cache anytime from **Settings** (open the ⚙ gear on the Home screen).

## AI Features

- **Trend deep-dive** — Tap "Analyze with AI" on any trend to get a structured analysis (summary, why it's trending, sentiment, growth prediction, viral probability, confidence).
- **AI Trends hub** — A dashboard with a daily summary, overall market sentiment, rising/viral counts, and average confidence.
- **AI assistant** — Ask natural-language questions about any trend.

AI analyses are **cached** (30-minute TTL) so repeated views are fast and work offline. If the Gemini key is missing or the API fails, the app falls back to a safe, deterministic summary rather than crashing or showing a blank result.

## Security

- API keys are read from the git-ignored `local.properties` and injected via `BuildConfig` — never hardcoded in source.
- `local.properties`, `.env`, `debug.keystore`, `*.jks`, and `*.keystore` are all git-ignored.
- GitHub Actions CI uses **placeholder** keys only and never publishes real secrets.
- Release signing credentials come from environment variables at build time.

## Contributing

Contributions are welcome! Please:

1. Fork the repository and create a feature branch.
2. Make your changes and ensure `gradlew :app:testDebugUnitTest` passes.
3. Open a pull request with a clear description of the change.

## License

- Built with Kotlin, Jetpack Compose, MVVM, Room, DataStore, Retrofit, and OkHttp.
- This is a demo/portfolio project using public APIs; data shown may be sample/mock when the API is unavailable or a key is missing.
