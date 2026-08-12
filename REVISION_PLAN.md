# Orbit Launcher — Revision Plan

**Revision objective:** Move Orbit from a fixed radial launcher to a deliberately customisable personal home surface, while keeping its defining central long-press gesture intact.

## Confirmed interaction changes

| Request | Revised behavior |
|---|---|
| Remove instructional centre text | The clock and widget centre no longer display a “hold to speak” or “press long” instruction. |
| Long-press voice/search options | A fixed long press on the centre opens a compact two-button action chooser: a microphone button starts one-shot voice launch; a search button opens app search. The chooser is icon-led and contains no instructional copy. |
| More icons above the circle | The ring becomes an **upper halo** with 18 app positions per page, arranged over two soft arcs above and around the centre circle. |
| Change circle size | Users choose Small, Balanced, or Large centre geometry. The halo adapts its inner and outer arc radii around that choice. |
| Custom app triggers | Users select either **Tap** or **Double tap** as the app-target activation trigger. The centre’s long press is reserved and cannot be reassigned. |
| Square widgets | In addition to the central widget, users can add several independent square widget tiles beneath the centre. Each is a separate Android widget instance. |
| Wallpaper changes | Users can select a photo as the launcher-only wallpaper, clear it, or use one of the integrated ambient color backgrounds. |

## Five additional customisation features

| Feature | User value | Implementation |
|---|---|---|
| Icon scale | Lets users balance density and touch target size. | Compact, Comfortable, and Expressive icon pods resize the upper halo. |
| Label visibility | Supports a more minimal home surface without sacrificing recognisability for users who want labels. | One toggle hides or displays app labels. |
| Haptic feedback | Makes custom app triggers feel deliberate and confirms successful activation. | A configurable haptic tick accompanies an app launch. |
| Appearance mode | Lets a user keep Orbit aligned with the device, always light, or always dark. | System, Light, and Dark Material 3 appearance choices. |
| Ambient backdrop | Adds personality without the privacy or complexity of a live wallpaper. | Five tonal gradients, optionally under a selected launcher photo. |

## Home composition

The home screen retains a large centre circle as its visual anchor. App targets are no longer distributed through a full 360-degree ring. Instead, two arcs form a hovering “halo” above the circle, giving the centre more room and ensuring the app system reads as an intentional crown rather than a border.

The central surface continues to accept the unchangeable long press. It is intentionally silent until invoked. The chosen voice/search action then appears as two clear circular controls. Search opens a lightweight app finder with an immediate text field and matching installed apps; microphone begins one-shot speech recognition only after the user taps the microphone icon.

## Widget model

| Surface | Purpose | Capacity |
|---|---|---|
| Centre circle | One hero widget or a clock template. | One Android widget instance. |
| Square tile strip | Small one-by-one widget tiles selected individually. | Up to four widget instances in the initial revision. |

Both surfaces use the existing native `AppWidgetHost`. Android owns widget binding and provider configuration; Orbit persists each allocated widget ID and releases it when the tile is removed.

## Validation targets

The revised Android build must compile successfully; retain Home-role support; launch apps with both trigger options; expose search and microphone chooser actions after the centre long press; persist geometry, appearance, wallpaper, and layout choices; and preserve existing central widget behavior. Device-level tests remain necessary for actual speech services, widget-provider configuration, and the system wallpaper/photo picker.
