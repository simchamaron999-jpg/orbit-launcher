# Orbit Launcher — Product and Build Plan

**Author:** Manus AI  
**Platform:** Android only  
**Technology:** Native Kotlin, Jetpack Compose, Material 3, Android App Widget APIs

## Product intent

Orbit Launcher turns the sketch’s large central circle and surrounding app belt into an Android home-screen replacement. The experience is intentionally focused: a visually calm, Material You 3 home surface where the centre is personal and the outer orbit is functional.

> **Primary interaction:** Long-press the central surface, say an app name, and Orbit finds and opens the best matching installed launchable app.

## Scope and interaction model

| Area | Planned behaviour | Acceptance criterion |
|---|---|---|
| Launcher role | The activity declares the Android Home and Default categories so the user can select Orbit as the device home app. | Pressing Home after the user selects Orbit opens the launcher. |
| Centre surface | The centre can show a large clock, clock plus date, or a user-selected Android app widget. | A user can change the centre mode from home settings. |
| Widget hosting | The launcher allocates, binds, configures, renders, and removes a hosted app-widget instance. Widget binding is explicitly confirmed by Android when required. | A compatible widget selected through the Android picker appears in the central circle. |
| Voice launch | A long press asks for microphone permission if needed, starts a one-shot speech session, resolves the spoken phrase against installed apps, and launches the best match. | Saying an installed app’s display name opens that app or presents a clear no-match response. |
| Orbit content | The circular ring can use favourites, most-used apps, recently used apps, or every app in alphabetical order. | Settings switch the active content source without leaving the launcher. |
| Ring navigation | A drag around the outer region rotates through pages. In **All apps** mode it progresses alphabetically through the full app list. | Users can reach apps beyond the first ring page. |
| Usage data | With explicit Usage Access, recent and most-used use device activity; without it, they use launches made through Orbit. | The launcher remains useful without privileged usage access. |
| Visual system | Material You dynamic colors on Android 12+; elegant fallback palette before Android 12. Circular surfaces use tonal elevation, clear contrast, icon labels, haptics, and reduced-motion-safe behavior. | The screen adapts to the system color scheme while retaining readability. |

## Deliberate product decisions

The centre is treated as a **widget slot**, not merely a clock skin. Native widget hosting is supported through `AppWidgetHost`; Android requires the host to allocate and preserve widget IDs, respect sizing and configuration activities, and obtain user approval to bind where the system requires it.[1]

The first release will host **one central widget instance**. This preserves the large, compositional circle shown in the sketch and avoids an overloaded home screen. A future release can introduce multiple pages or smaller peripheral widgets without changing the core data model.

Voice recognition is implemented as an intentional, one-shot command rather than a background hotword listener. Android’s speech service needs `RECORD_AUDIO`, can use on-device recognition where available, must be created and used on the main thread, and must be released after use.[2] This approach protects battery and privacy while fitting the long-press interaction exactly.

Installed apps are discovered through the standard `ACTION_MAIN` plus `CATEGORY_LAUNCHER` query. The manifest also declares visibility for the recognition service; Android documents this requirement for apps targeting Android 11 and newer.[2] [3]

## Delivery architecture

| Layer | Responsibility |
|---|---|
| `MainActivity` | The launcher activity, widget-picker result flow, default-home settings handoff, and Compose entry point. |
| `ui/` | Material 3 theme, responsive radial layout, centre surface, settings sheet, picker sheet, and voice feedback. |
| `data/` | Installed-app discovery, local favourites/history, optional Android usage-stat queries, and shared preferences. |
| `voice/` | Permission-safe speech session, phrase normalization, ranking, and app matching. |
| `widget/` | Persistent `AppWidgetHost` lifecycle, widget selection/binding/configuration, and Android View interop. |

## Build sequence

| Phase | Outcome |
|---|---|
| 1. Scope and constraints | This product plan, platform decisions, and validation targets. |
| 2. Project foundation | Native Android Gradle project, home intent registration, Material You theme, and baseline app discovery. |
| 3. Main experience | Central templates/widget host, radial ring, paging gesture, and Material 3 settings. |
| 4. Functional integrations | Voice launch, usage-derived ring modes, favourites, widget binding, and default-home handoff. |
| 5. Quality and delivery | Static checks/build attempt, implementation notes, and a packaged source archive. |

## Technical boundaries

A launcher cannot silently replace the system’s default home app; Android requires the user to choose it. Similarly, widget hosting may invoke Android-controlled approval and the widget provider’s own configuration screen. These steps are intentional because the system owns those permissions and configurations.[1]

The app will target modern Android versions while keeping a practical minimum API level of 26. Material You dynamic color is enabled when the platform supports it. Voice launch depends on a recognition service being present and on microphone permission, so its UI includes graceful fallback states rather than assuming a particular device vendor service.

## References

[1]: https://developer.android.com/develop/ui/views/appwidgets/host "Build a widget host — Android Developers"
[2]: https://developer.android.com/reference/android/speech/SpeechRecognizer "SpeechRecognizer — Android Developers"
[3]: https://developer.android.com/training/package-visibility/use-cases "Fulfill common use cases while having limited package visibility — Android Developers"

[1] [2] [3]

## Visual direction

The screen is a full-bleed wallpaper-responsive surface. The centre is a large, softly elevated circular card. A thin, segmented orbit surrounds it; each app sits in a rounded, touch-friendly pod with its label available on focus or touch. Dense marker dots in the sketch become breathing room, not clutter: the orbit uses a quiet track, selected state glow, and a single current-mode label. The visual language is Material You 3 rather than skeuomorphic: expressive typography, dynamic color, generous shapes, and deliberate elevation.

The settings surface will be a modal sheet that leaves the orbital composition visible behind it. It gives the user direct choices for central content, ring source, favourites, usage permission, and Android widget selection. This retains the sketch’s single-screen character while keeping advanced controls discoverable.

---

**Status:** Product scope complete; project implementation begins next.
