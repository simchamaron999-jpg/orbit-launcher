# Orbit Launcher 0.3.1 — Audit and Fix Report

**Scope:** Source review, interaction-path review, manifest and resource review, Android lint, and debug build validation.  
**Build reviewed:** `versionCode 4`, `versionName 0.3.1`.

## Audit outcome

The review focused on the full-circle launcher UI, configuration usability, widget and wallpaper lifecycle, resource integrity, and Android build health. The code was inspected and corrected where a source-level issue or a clear usability risk was found. The final lint-and-build task completed successfully.

> **Result:** `./gradlew --no-daemon :app:lintDebug :app:assembleDebug` completed successfully after the corrections below.

## Completed fixes

| Area | Finding | Correction |
|---|---|---|
| App orbit | The decorative circular strokes could read as a bright/white separator between app targets. | Removed the two rendered orbit separator rings. Apps now sit directly on the wallpaper/backdrop in a clean full circle. |
| Settings usability | One long settings sheet forced users to scan unrelated controls and scroll through all options. | Rebuilt settings as six focused categories: **Quick**, **Gestures**, **Apps**, **Look**, **Widgets**, and **System**. The Quick category exposes common controls first. |
| App labels | The no-label preference was present but not surfaced quickly. | Added **App names** to Quick controls with explicit icon-only wording. |
| App search | Search results were placed in a fixed column and could exceed the visible sheet on large app lists. | Replaced the result column with a scrollable lazy list, so all matching apps remain reachable. |
| Wallpaper resources | The gallery relied on runtime resource-name reflection, preventing compile-time verification and causing false unused-resource findings. | Switched the wallpaper model to direct `R.drawable` references. This gives Android compile-time resource checks and removes reflective lookup. |
| Wallpaper variety | Two gallery assets had identical image content under different names. | Replaced the duplicate forest scene with a distinct mountain-lake photo and regenerated the portrait asset. |
| Widget tiles | Four 76 dp tiles plus gaps could overflow the centre-column width on a narrow phone. | Reduced tile size to 68 dp and spacing to 8 dp so a four-tile row fits a typical narrow portrait device. |
| Manifest permissions | The manifest declared a protected widget-binding permission that regular third-party apps cannot receive. Lint also needs an explicit acknowledgement for the special Usage Access declaration. | Removed the ineffective `BIND_APPWIDGET` declaration. Retained `PACKAGE_USAGE_STATS` for the user-controlled Usage Access workflow and documented it for lint with `tools:ignore`. Android still controls widget binding and Usage Access through system UI.[1] [2] |
| Manifest compatibility | An API-33-only back-callback XML attribute was declared while the app’s minimum API level is 26. | Removed the unnecessary manifest attribute. |
| Compose state | The current page index used boxed generic state. | Replaced it with `mutableIntStateOf` for the page index. |

## Validation performed

| Check | Status | Evidence |
|---|---|---|
| Source-level audit | Passed | Reviewed centre action handling, app circle paging, search flow, settings navigation, widget tile widths, wallpaper selection, resource references, and manifest declarations. |
| Lint | Passed | `:app:lintDebug` completes successfully after the manifest and direct-resource fixes. |
| Debug compilation | Passed | `:app:assembleDebug` completes successfully. |
| Resource packaging | Passed | All twelve built-in portrait wallpaper assets are compiled into the app through direct resource references. |
| App artifact | Pending final build in this packaging phase | The final 0.3.1 APK is rebuilt after the version increment. |

## Physical-device checks still required

A sandbox build cannot provide a real Android home-screen role, installed third-party app set, microphone service, or widget provider. The following tests must be completed on a physical device or Android emulator before a production release.

| Device test | Expected behavior |
|---|---|
| Default Home role | Android displays the Home-role choice and returns to Orbit when the physical Home control is used. |
| Visible centre buttons | Microphone and search buttons remain responsive inside the clock or a hosted centre widget. |
| Gesture map | Tap, double-tap, triple-tap, and long press run the configured centre action without unintended overlap. |
| Full-circle capacity | 8, 12, and 16 app positions remain legible and touchable on the target display sizes. |
| App activation | Tap and Double tap modes launch only at the selected trigger threshold. |
| Wallpaper gallery | All 12 scene cards render, select, persist, and display legibly behind launcher controls. |
| Personal photo | Android’s document picker grants retained read access and the chosen image persists after restart. |
| Widget binding | Centre and square-tile widget selection, provider configuration, replacement, and removal work with multiple widget providers. |
| Voice launch | The microphone permission, on-device/available speech recognizer, spoken app matching, and error feedback work on the target device. |
| Usage Access | Device-wide Recent and Most used results populate after explicit user approval in Android settings. |

## References

[1]: https://developer.android.com/develop/ui/views/appwidgets/host "Build a widget host — Android Developers"
[2]: https://developer.android.com/reference/android/app/usage/UsageStatsManager "UsageStatsManager — Android Developers"

[1] [2]
