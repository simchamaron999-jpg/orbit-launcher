# Orbit Launcher 0.7.5

**Orbit Launcher** is an Android-only home launcher written in Kotlin and Jetpack Compose. It uses a Material You 3 interface to place launchable apps around a central clock or widget surface, with configurable pages, gestures, wallpapers, widgets, conventional voice launching, and optional BYOK AI assistance.

## Release highlights

| Feature | Behaviour in 0.7.5 |
|---|---|
| **Independent home pages** | Create up to eight swipeable pages. Each page can use **Recent**, **Most used**, **Favourites**, or **All apps** as its source. |
| **Readable app density** | Every page has an explicit visible-app target: **8, 12, 16, 20, 24, or 32**. The orbit uses the selected limit before applying responsive icon sizing. |
| **Control layout** | The top-right Customize control is a gear. AI sits above the central clock, microphone and app search sit below it, and a four-icon navigation dock is positioned below the orbit. |
| **Visual polish** | The home screen now uses a tighter orbit, lighter-weight icon pods, compact labelled capsules, stronger wallpaper scrims, and a more balanced central clock surface for clearer hierarchy on detailed photos. |
| **Black orbit icons** | App marks and labels in the orbit use a consistent black monochrome treatment over light pods. |
| **AI voice requests** | The sparkle button opens voice capture and sends the spoken request only to the BYOK provider configured by the user. App-launch replies are restricted to packages discovered on the device. |
| **Supported BYOK providers** | OpenAI, Anthropic, Google Gemini, OpenRouter, and a custom **HTTPS** OpenAI-compatible endpoint. **OmniRoute is not offered.** |
| **Wallpaper contrast** | The settings and navigation controls sample the active wallpaper and use light controls on dark imagery, or dark controls on light imagery. |
| **System wallpaper sync** | Every selected gallery or user wallpaper is automatically applied to the Android system wallpaper, so it also appears behind Recent Apps. The manual Apply action remains available. |
| **Templates** | Save the current layout as a local template, then install or remove saved templates in **Settings → Templates**. |

## First-time setup

Set Orbit as the default Home app from **Settings → System → Set as default home**. Grant **Usage statistics** only if you want accurate Recent and Most used pages. Grant microphone access only when using voice launch or Ask Orbit.

To use Ask Orbit, open **Settings → AI Engine**, choose a provider, and enter your own API key. The app does not include any developer API key. The selected provider receives the voice request and the installed-app list only as needed to resolve the request.

> The AI feature needs an active network connection and a valid user-provided API key. Network and provider errors are shown in the central surface.

## Privacy and security

Orbit requests only the permissions needed for its launcher features: microphone access for user-initiated voice capture, usage statistics for optional usage-based pages, internet access for opted-in BYOK AI requests, and wallpaper access for the explicit system-wallpaper action. App data and user preferences are private to the app; automated cloud backup and device-transfer extraction are disabled to reduce the risk of API-key exposure. The app permits HTTPS traffic only.

## Build

```bash
export ANDROID_HOME=/path/to/Android/Sdk
export JAVA_HOME=/path/to/jdk-17
./gradlew :app:assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Project information

| Item | Value |
|---|---|
| **Android application ID** | `com.sm.orbitlauncher` |
| **Creator** | Simcha Maron |
| **Contact** | [simchamaronapp@gmail.app](mailto:simchamaronapp@gmail.app) |
| **Source repository** | <https://github.com/simchamaron999-jpg/orbit-launcher> |
| **License** | Apache-2.0 |

## References

[1]: https://developer.android.com/privacy-and-security/security-tips "Android security checklist"
[2]: https://developer.android.com/guide/components/intents-filters "Android intents and intent filters"
[3]: https://m3.material.io/ "Material Design 3"

[1] [2] [3]
