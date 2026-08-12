# Orbit Launcher 0.7

**Orbit Launcher** is a native Android-only home launcher, built with Kotlin, Jetpack Compose, and Material You 3. It places app sources around a central clock or widget surface, combining a distinctive radial interface with practical launcher controls, wallpaper customization, widgets, gesture actions, and optional bring-your-own-key AI assistance.

## Release highlights

| Feature | Description |
|---|---|
| **Independent home pages** | Create up to eight swipeable pages and give each one its own source: **Recent**, **Most used**, **Favourites**, or **All apps**. |
| **Unlimited app sources** | Pages no longer use a fixed app limit. Every matching app is shown, while Orbit automatically reduces icon and label size as the page becomes denser. |
| **Wallpaper-adaptive controls** | Orbit samples the active wallpaper luminance. The page indicator and settings control become light on dark wallpapers and dark on light wallpapers. |
| **Refined centre controls** | The voice, Ask Orbit, and search controls now use a visually consistent circular icon treatment. |
| **AI voice requests** | Tapping **Ask Orbit** opens the microphone, transcribes the request, and sends it to the selected BYOK provider. For an app-launch request, the AI can select and open an installed app; otherwise, its concise reply is shown in the centre surface. |
| **Supported BYOK providers** | OpenAI, Anthropic, Google Gemini, OpenRouter, and a custom OpenAI-compatible endpoint. **OmniRoute is not offered.** |
| **Material You customization** | Choose the central clock or widget, clock style, circle size, app-label visibility, app trigger, gesture actions, wallpapers, visual theme, and more. |
| **Android launcher features** | Voice app launching, AppWidget hosting, full-screen category settings, usage-access support, portrait lock, haptics, and first-run onboarding. |

## Page configuration

Open **Settings → Apps** to add, remove, and configure home pages. A new installation starts with separate **Recent**, **Most used**, and **Favourites** pages. Swipe left or right on the home screen to change pages. Recent and Most Used use Android usage statistics once the corresponding system permission has been allowed in **Settings → System**.

## AI voice setup

Open **Settings → AI Engine**, select a provider, and enter your own API key. Keys are saved only in the app's private preferences. Tap the sparkle icon at the bottom of the centre circle, speak naturally, and Orbit will use the configured provider to interpret the request. A network connection is required for the AI provider request; the separate conventional voice-launch control does not require an AI key.

## Build and installation

```bash
export ANDROID_HOME=/path/to/Android/Sdk
export JAVA_HOME=/path/to/jdk-17
./gradlew :app:assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Project links

| Item | Value |
|---|---|
| **Creator** | Simcha Maron |
| **Contact** | simchamaronapp@gmail.app |
| **Repository** | <https://github.com/simchamaron999-jpg/orbit-launcher> |
| **License** | Apache-2.0 |

## References

[1]: https://developer.android.com/develop/ui/views/appwidgets/host "Build a widget host — Android Developers"
[2]: https://developer.android.com/develop/ui/compose "Jetpack Compose — Android Developers"
[3]: https://m3.material.io/ "Material Design 3"

[1] [2] [3]
