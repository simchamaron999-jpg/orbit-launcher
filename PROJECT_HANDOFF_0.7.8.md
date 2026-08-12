# Orbit Launcher — Project Handoff (0.7.8)

> **Purpose:** This document is a complete, current handoff for continuing Orbit Launcher work in a new chat without relying on prior chat history.

## Project identity

| Item | Current value |
|---|---|
| App | Orbit Launcher |
| Platform | Native Android, Kotlin, Jetpack Compose / Material 3 |
| Package ID | `com.sm.orbitlauncher` |
| Project directory | `/home/ubuntu/orbit-launcher-v073` |
| Android configuration | minSdk 26, targetSdk 35, compileSdk 35 |
| Current version | **0.7.8** (`versionCode` 15) |
| Creator | Simcha Maron — `simchamaronapp@gmail.app` |
| Source repository | https://github.com/simchamaron999-jpg/orbit-launcher |
| Public branch | `main` only; the remote `master` branch was deleted at the user's request |
| GitHub releases | **None**. Do not create one unless the user explicitly approves it. |

## Verified 0.7.8 build

The updated debug APK compiled successfully with the following command:

```bash
cd /home/ubuntu/orbit-launcher-v073
export ANDROID_HOME=/home/ubuntu/android-sdk
./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx1024m -Dfile.encoding=UTF-8' assembleDebug
```

The debug artifact is packaged at:

```text
/home/ubuntu/orbit-release-078/Orbit-Launcher-0.7.8-debug.apk
/home/ubuntu/orbit-release-078/Orbit-Launcher-0.7.8-debug.apk.sha256
```

The expected package metadata is:

```text
com.sm.orbitlauncher
versionName: 0.7.8
versionCode: 15
```

## What 0.7.8 changes

| Area | Current implementation |
|---|---|
| App icons | Removed the forced black `SRC_IN` icon color filter from the orbit. App icons now render using their original application artwork instead of appearing as black placeholder-like symbols. |
| System bars | The launcher uses edge-to-edge immersive mode. `MainActivity` re-hides the status and navigation bars after focus returns with a 120 ms delay, while allowing temporary reveal by swipe. |
| Default launcher status | The Settings > System screen checks Android's current Home intent resolution. It shows **Set as default home** only when Orbit is not active; otherwise it shows **Default Launcher**. |
| AI provider | AI is now **OpenRouter-only**, using `google/gemma-4-31b-it:free` and OpenRouter chat completions. The client adds OpenRouter `HTTP-Referer` and `X-Title` headers. |
| AI settings | The Settings AI area only exposes an OpenRouter API-key field and Save button. Any legacy provider preference is automatically treated as OpenRouter. |
| Visual behavior | Dense-orbit label text scales down at app counts above 20, but the best next design refinement is still needed for very dense layouts; see Pending work. |

## Important source files

| File | Responsibility |
|---|---|
| `app/src/main/java/com/sm/orbitlauncher/MainActivity.kt` | Launcher lifecycle, immersive mode, app discovery, AI voice entry point, wallpaper application, and settings wiring. |
| `app/src/main/java/com/sm/orbitlauncher/ui/OrbitHomeScreen.kt` | Radial app orbit, central clock/widget surface, search sheet, top settings, and bottom dock. |
| `app/src/main/java/com/sm/orbitlauncher/ui/LauncherSettingsScreen.kt` | Full-screen Material 3 settings UI, category bar, AI settings, wallpaper UI, and system status. |
| `app/src/main/java/com/sm/orbitlauncher/ai/AiClient.kt` | OpenRouter HTTP client; currently uses `google/gemma-4-31b-it:free`. |
| `app/src/main/java/com/sm/orbitlauncher/data/LauncherModels.kt` | Data models, including the OpenRouter-only `AiProvider` enum. |
| `app/src/main/java/com/sm/orbitlauncher/data/LauncherRepository.kt` | SharedPreferences persistence, installed app lookup, pages, settings, and OpenRouter key storage. |
| `app/src/main/AndroidManifest.xml` | Launcher registration, portrait lock, permissions, backup/data-extraction policy. |
| `app/build.gradle.kts` | Version configuration and Android build settings. |

## Pending work — do not claim these are finished

The user requested the following, but they are **not yet implemented** in this build:

| Request | Required next action |
|---|---|
| Automatic/random wallpapers | Add a user setting for wallpaper rotation interval, persist it, and use Android-appropriate scheduled work to choose a built-in wallpaper and apply it through `WallpaperManager.FLAG_SYSTEM`. This needs an implementation decision that preserves behavior under Android battery restrictions. |
| Very dense orbit labels | The source currently reduces label size above 20 apps. A better professional solution should avoid overlaps by showing labels only for a selected/held app in dense mode, or by capping the visible apps per page and keeping full names in All Apps search. Do not make labels so tiny that they become unreadable. |
| AI device testing | The source now has a consistent OpenRouter request path, but it has not been tested with the user's real API key. Ask the user to enter their own key and provide a screenshot of any error message before changing model/provider logic again. |
| Settings visual normalization | The user asked for a more normal, professional Settings page. This should be a focused visual refinement after the current build is tested. |
| F-Droid packaging | RFP exists but store publication has not happened. The request is open and awaiting review; see below. |

## F-Droid status

| Item | Status |
|---|---|
| Request for Packaging | Submitted successfully: [F-Droid RFP #4251](https://gitlab.com/fdroid/rfp/-/work_items/4251) |
| Last checked state | Open / To do; no maintainer comments or labels at the last check. |
| Store publication | **Not yet published.** F-Droid must review and build the tagged public source before a store page appears. |
| Public source tag | `v0.7.5` was pushed during submission preparation. |

## User constraints to preserve

The user wants an Android-only launcher with Material You 3 styling, portrait orientation locked, BYOK AI with user-entered API keys, central clock/widget replacement, voice app launch, configurable gestures, multi-page radial app orbit, built-in wallpaper gallery, and no GitHub releases without explicit approval.

The current user prefers short iterative updates: explain the plan, wait for confirmation before any new large feature batch, then deliver a tested APK. Keep the public GitHub repository on the single `main` branch.

## Suggested first message in a new chat

> I have the Orbit Launcher 0.7.8 handoff. The verified APK is ready, the repository now has only `main`, and the next unfinished item is automatic wallpaper rotation plus a final dense-orbit label design. Please tell me which one you want to prioritize after testing 0.7.8.

## References

[1] [OpenRouter Gemma 4 31B free model](https://openrouter.ai/google/gemma-4-31b-it:free)

[2] [F-Droid Request for Packaging #4251](https://gitlab.com/fdroid/rfp/-/work_items/4251)

[3] [Orbit Launcher public source repository](https://github.com/simchamaron999-jpg/orbit-launcher)

