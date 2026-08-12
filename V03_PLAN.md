# Orbit Launcher 0.3 — Interaction and Customisation Plan

## New home-screen direction

Orbit 0.3 restores the app system to a **single, full 360-degree orbit**. There is one radial row of app positions, rather than an upper-only halo or two concentric rows. Users can select the number of positions per page, allowing a spacious or dense circle without breaking the one-app-per-position rule.

The central clock no longer hides its primary actions. A small, persistent two-icon dock sits inside the lower edge of the large centre circle: **microphone** starts voice launch and **search** opens the installed-app finder. It contains no instructional text.

## Gesture system

The new centre gesture mapper removes the formerly fixed long press. A user can assign one action to each of four centre gestures: tap, double-tap, triple-tap, and long press. Triple tap is detected as three taps within a short intentional interval. Gesture mappings affect only the central surface; app icons retain their separate activation trigger to avoid accidental app openings.

| Gesture | Default action | Configurable actions |
|---|---|---|
| Tap | None | None, voice launch, app search, settings, all-apps orbit, next wallpaper |
| Double tap | App search | None, voice launch, app search, settings, all-apps orbit, next wallpaper |
| Triple tap | Settings | None, voice launch, app search, settings, all-apps orbit, next wallpaper |
| Long press | Voice launch | None, voice launch, app search, settings, all-apps orbit, next wallpaper |
| App target | Tap | Tap or double-tap |

The visible microphone and search buttons remain direct shortcuts even if the user changes every gesture mapping.

## Full-circle orbit controls

| Control | Options | Effect |
|---|---|---|
| Orbit capacity | 8, 12, 16 | Number of single-row app positions around the full circle per page. |
| Rotation speed | Gentle, Balanced, Quick | Controls the page-change rotation animation duration. |
| App trigger | Tap, Double tap | Opens app targets with the selected deliberate trigger. |
| Icon scale | Compact, Comfortable, Expressive | Changes touch-target size inside the selected ring capacity. |
| App names | Shown, Hidden | Provides a fully icon-only orbit when hidden. |
| Centre size | Small, Balanced, Large | Scales the central clock/widget without changing the full orbit model. |

## Wallpaper gallery

A built-in gallery of 12 curated landscape and ambient wallpapers will sit alongside the existing photo document picker. The gallery deliberately uses a mixture of mountains, shoreline, desert, forest, night sky, alpine lake, volcanic terrain, and calm color-led natural scenes so users can quickly give Orbit a distinct identity without leaving the launcher.

The wallpaper setting will offer three sources: **Built-in gallery**, **Choose photo**, and **Ambient backdrop**. A selected gallery image or user photo remains launcher-only and is overlaid at a restrained opacity behind Material 3 content.

## Additional features added for this revision

| Feature | Purpose |
|---|---|
| Gesture map | Makes the large centre surface behave like a personally configured command button. |
| Orbit capacity | Lets a user choose a calmer 8-target ring or a more information-dense 16-target ring. |
| Rotation speed | Lets motion feel calm or fast without altering app order. |
| Wallpaper shuffle action | Allows an assigned centre gesture to cycle through built-in wallpapers. |
| All-apps action | Allows an assigned centre gesture to switch the orbit directly to the full alphabetized app library. |
| Quick-edit visual feedback | The settings sheet describes defaults and keeps the icon-only app-name option prominent for a minimal design. |

## Validation targets

The build must compile successfully; display a persistent centre microphone/search dock; render app targets through the entire circle in one radial row; preserve the app-name toggle; animate rotation at the selected speed; persist gesture assignments and wallpaper selection; support all 12 built-in wallpapers; and retain central/widgets/photo wallpaper behavior from 0.2.
