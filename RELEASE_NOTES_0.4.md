# Orbit Launcher 0.4 — Release Notes

**Orbit Launcher 0.4** is a focused design-correction release. It makes settings intentionally quiet at first glance, removes remaining bright separators, and replaces the built-in gallery with portrait-native natural wallpapers that fill a phone screen without blurred bands or landscape letterboxing.

> **Design change:** A user now sees a compact list of settings topics first, not a wall of controls. Each topic opens only when its arrow is selected.

## Completed changes

| Area | Orbit 0.4 behavior |
|---|---|
| Settings categories | All six categories—Quick controls, Centre gestures, App circle, Look and wallpaper, Widgets, and System—are **collapsed by default**. |
| Expand controls | Tapping a category row or its chevron expands the relevant controls; the chevron points up when open and down when closed. Several sections may be opened at the same time, but none are forced open. |
| Settings hierarchy | Bright horizontal dividers and the horizontal category strip are removed. Supporting copy, Material 3 tonal surfaces, spacing, and chevrons provide the visual hierarchy instead. |
| App-circle separators | The orbit remains free of decorative white/bright separator lines. |
| Built-in wallpaper gallery | The previous landscape-origin wallpaper assets have been replaced by twelve source-native portrait scenes. |
| Wallpaper treatment | Each gallery image is rendered as a direct 1080 × 1920 portrait composition. The earlier blurred-extension/letterbox treatment has been removed. |
| Gallery scenes | Mountain reflection, Lakeside silence, Emerald forest, Blue peak, Canyon falls, Forest cascade, Alpine falls, Autumn falls, Starlit cliffs, Highland lake, Golden lake, and Sunset bay. |

## Settings interaction

The settings sheet still retains all existing controls. The change is structural rather than reductive: users open only the area they need instead of navigating a single large form.

| Category | Controls available when expanded |
|---|---|
| Quick controls | Centre size, icon-only app-name preference, ring content source, and a reminder about the persistent microphone/search buttons. |
| Centre gestures | Tap, double-tap, triple-tap, and long-press mappings. |
| App circle | Apps per orbit, rotation speed, activation threshold, icon scale, and favourites. |
| Look and wallpaper | Portrait wallpaper gallery, personal photo picker, ambient backdrop, appearance, and haptics. |
| Widgets | Central widget and up to four square widget tiles. |
| System | Home role request and optional Usage Access. |

## Wallpaper preparation

The prepared gallery contains only vertical source photographs. Each image is cropped directly to a 9:16, 1080 × 1920 output; no synthetic side, top, or bottom panels are added. A restrained brightness adjustment maintains legibility of launcher controls while preserving the image’s full portrait composition.

## Validation

| Check | Result |
|---|---|
| Android lint | Passed with `:app:lintDebug`. |
| Kotlin/Compose compilation | Passed with `:app:assembleDebug`. |
| Wallpaper assets | Twelve 1080 × 1920 portrait JPEGs generated and linked through direct compiled resource identifiers. |
| Device test recommended | The exact feel of expand/collapse motion, wallpaper contrast, third-party widget providers, and real Android Home-role behavior should be verified on a physical device before a production release. |

## References

[1]: https://m3.material.io/components/lists/overview "Lists — Material Design 3"
[2]: https://developer.android.com/develop/ui/compose/animation/quick-guide "Animation quick guide — Android Developers"

[1] [2]
