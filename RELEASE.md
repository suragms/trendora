# Trendora — Play Store Release Guide

This document covers everything required to publish Trendora to the Google Play Store.
It intentionally does **not** create a signing keystore — you must generate your own
upload key (see below) and keep it safe.

---

## 1. Application identity

Current values live in [`app/build.gradle.kts`](app/build.gradle.kts):

| Field        | Value                          | When to change                                  |
| ------------ | ------------------------------ | ----------------------------------------------- |
| `applicationId` | `com.aistudio.trendora.kxwtq`  | **Never change after the first Play upload.**   |
| `namespace`  | `com.example`                  | Can stay as-is (not user-visible).              |
| `versionCode`| `1`                            | Bump by +1 for **every** Play upload.           |
| `versionName`| `1.0`                          | SemVer shown to users (e.g. `1.0`, `1.1`).      |

> ⚠️ `versionCode` is what Play uses to detect new uploads. It must always increase.
> `versionName` is cosmetic and may stay the same if you only fix a bug.

To cut a new release, edit `versionCode` / `versionName` in `defaultConfig`.

---

## 2. Signing the release build (required)

Play requires your APK/AAB to be signed. Trendora reads the keystore and passwords from
environment variables so **no secrets are hardcoded** in the repo.

The release `signingConfig` in `app/build.gradle.kts` expects:

| Environment variable | Purpose                                                        |
| -------------------- | -------------------------------------------------------------- |
| `KEYSTORE_PATH`      | Absolute path to your `.jks` upload keystore file.             |
| `STORE_PASSWORD`     | Keystore password.                                             |
| `KEY_PASSWORD`       | Key password.                                                  |
| `keyAlias`           | Fixed to `"upload"` in `build.gradle.kts`.                     |

If `KEYSTORE_PATH` is not set, Gradle falls back to `<projectRoot>/my-upload-key.jks`.
If that file is also missing, the **release** build will fail with a keystore error — this
is intentional: it prevents you from shipping an unsigned/randomly-signed build.

### Generate your own upload key (do this once)

```bash
keytool -genkeypair -v \
  -keystore my-upload-key.jks \
  -alias upload \
  -keyalg RSA -keysize 2048 -validity 10000
```

Then export the passwords when building:

```bash
# Windows (PowerShell)
$env:KEYSTORE_PATH="$PWD\my-upload-key.jks"
$env:STORE_PASSWORD="<your store password>"
$env:KEY_PASSWORD="<your key password>"

# macOS / Linux
export KEYSTORE_PATH="$PWD/my-upload-key.jks"
export STORE_PASSWORD="<your store password>"
export KEY_PASSWORD="<your key password>"
```

> 🔐 Store this keystore somewhere safe (a password manager / secure vault). If you lose it
> you must request an [upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756)
> from Google, which takes time.

---

## 3. Build the release

```bash
# Clean release AAB (recommended for Play)
gradle :app:bundleRelease

# Or an APK
gradle :app:assembleRelease
```

Outputs:
- AAB: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`

The debug build is signed with `debug.keystore` automatically and needs no setup.

---

## 4. R8 / code shrinking

Currently `isMinifyEnabled = false` for the release build type. Before shipping you may
enable shrinking to reduce APK size:

```kotlin
release {
  isMinifyEnabled = true
  isShrinkResources = true
  proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
}
```

**Caveats:** Trendora uses Moshi (codegen, already via KSP), Coil, Room, and OkHttp.
Verify the app end-to-end with minification enabled before publishing — keep rules for
Room-generated schemas and any model classes referenced by reflection. If unsure, keep
`isMinifyEnabled = false` for the first release.

---

## 5. Play Console checklist

1. Create a **Google Play Console** developer account ($25 one-time fee).
2. **App signing** → enable *Play App Signing* (Google manages the signing key; your
   `.jks` above is the **upload key**).
3. **Create a release** → upload `app-release.aab`.
4. Fill in store listing (title, description, screenshots, feature graphic).
5. **Content rating** questionnaire (required before the first release).
6. **Data safety** form — Trendora does not collect personal data beyond local
   preferences/DataStore.
7. Roll out to **Closed testing** (internal testers) before production if desired.

---

## 6. Secrets & the Gemini API key

The API key is **not** hardcoded. The [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin)
reads it from a local `.env` file (git-ignored):

```
# .env  (never commit this)
GEMINI_API_KEY=your_key_here
```

`BuildConfig.GEMINI_API_KEY` is then available in code. See [`.env.example`](.env.example)
for the expected variable names. Never commit `.env` or any real key.

---

## 7. Pre-release QA checklist

- [ ] `./gradlew :app:testDebugUnitTest` passes (unit + Robolectric tests).
- [ ] App runs in dark mode (default) and light mode.
- [ ] Offline banner appears with airplane mode on; cached/mock data still renders.
- [ ] Pull-to-refresh works on Home.
- [ ] Save/remove trend & article shows Snackbar confirmation.
- [ ] Gemini AI Trends gracefully falls back when the API key is absent/offline.
- [ ] `versionCode` bumped and the release AAB builds with your real upload key.
