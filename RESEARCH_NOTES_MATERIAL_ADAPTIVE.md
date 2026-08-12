# Research Notes — Material You 3 and Adaptive Sizing

## Official sources reviewed

| Source | Key evidence for Orbit Launcher |
|---|---|
| [Material Design 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3) | Material 3 centers theming on a coordinated color scheme, typography, and shapes through `MaterialTheme`. Dynamic color is available on Android 12+ and should have a light/dark fallback. Semantic color roles should express prominence and state. M3 includes display/title/body/label typography roles and favors tonal elevation over unnecessary shadows. |
| [Material 3 Breakpoints](https://m3.material.io/foundations/layout/breakpoints) | Design for available window space rather than individual devices. Breakpoints are compact (<600dp), medium (600–839dp), expanded (840–1199dp), large (1200–1599dp), and extra-large (1600dp+). When a breakpoint changes, decide what to reveal, divide, resize, reposition, or swap rather than uniformly scaling the interface. |
| [Build adaptive apps](https://developer.android.com/develop/ui/compose/build-adaptive-apps) | Android apps need to respond to different devices and runtime window changes. Adaptive Compose uses window size classes and can adjust primary navigation and layouts accordingly. |
| [Accessibility in Jetpack Compose](https://developer.android.com/develop/ui/compose/accessibility) | Compose accessibility requires meaningful semantics, scalable content, sensible traversal, content descriptions/state communication where useful, and testing with accessibility checks. |

## Implications for Orbit Launcher

1. Replace the user-facing **Small / Balanced / Large** concept with an **Adaptive size** system. The launcher should calculate the orbit radius, central surface diameter, icon size, label policy, and dock position from usable screen width/height, insets, and app count. A small user adjustment can remain, but it should be a continuous compact-to-spacious preference rather than three device-independent size labels.
2. On compact phone windows, reserve space for the centre, dock, and top controls before computing the orbit. The app count should determine whether labels are persistent, reveal-on-hold, or shown in a companion search/list surface.
3. On medium and expanded windows, change composition—not merely enlarge the current orbit. Keep the orbit at an ergonomic maximum diameter and reveal supporting content such as search, favorites, widget tiles, page tools, or a navigation rail in adjacent space.
4. Use semantic Material roles throughout the launcher. Dynamic colors should support the selected system wallpaper; custom wallpaper contrast logic should modify semantic tokens, not introduce arbitrary black/white components.
5. Professional launcher essentials identified for further research: a reliable app discovery path, fast search, predictable default-home state, wallpaper and icon coherence, accessible targets/semantics, backup/restore, gesture clarity, and resilient empty/error/loading states.

## Established launcher patterns

| Source | Evidence | Implication for Orbit Launcher |
|---|---|---|
| [Lawnchair](https://lawnchair.app/) | Positions itself as a customizable Pixel-style launcher with Material You and At a Glance support. | Professional launchers balance a familiar reliable home experience with selective customizability; Orbit should keep a dependable search/discovery fallback behind its distinctive radial UI. |
| [Smart Launcher FAQ](https://docs.smartlauncher.net/faq) | Documents setup/migration, personalization, gestures, battery behavior, app-drawer duplication/missing-app support, device-specific compatibility, notifications, and vendor restrictions. | A production launcher needs not just features but resilient lifecycle and support states: onboarding, default-home detection, battery/permission explanations, missing-app handling, backup/restore, and clear device limitation messaging. |
| [Niagara Button documentation](https://help.niagaralauncher.app/article/116-niagara-button) | Uses a configurable favorite shortcut with one secondary swipe action and explicitly limits gesture complexity to keep behavior memorable. | Orbit should not add unlimited gestures everywhere. Offer a small number of high-confidence actions in consistent locations, with a discoverable configuration screen. |
| [Niagara Launcher Play listing](https://play.google.com/store/apps/details?id=bitpit.launcher&hl=en_US) | Emphasizes themes, custom icons, wallpapers, clocks, Material You, widgets, gestures, advanced search, hide-apps, notification features, and backup/restore; it also markets a one-handed, uncluttered experience. | Core candidate features are reliable app discovery, a clear app drawer/search, wallpaper/icon coherence, optional widgets, discreet personalization, backup/restore, and privacy-safe optional power features. They should be staged by user value and complexity rather than copied wholesale. |

## Research conclusion so far

A professional launcher earns trust through three layers: **reliability** (always reaches apps, restores a layout, behaves predictably as the default Home app), **speed** (fast search and one-handed navigation), and **personalization** (wallpaper, icon, typography, widgets, and gestures that remain coherent rather than creating a settings maze). Orbit’s radial concept is already differentiated; its roadmap should prioritize those reliability and discovery layers before adding more novelty.

### Sources

- https://lawnchair.app/
- https://docs.smartlauncher.net/faq
- https://help.niagaralauncher.app/article/116-niagara-button
- https://play.google.com/store/apps/details?id=bitpit.launcher&hl=en_US

## Video findings

| Video | Evidence | Orbit implication |
|---|---|---|
| [Best Launchers review](https://www.youtube.com/watch?v=XnV3i3rX7zY) | The review highlights universal search for apps, contacts, and web results; habit-aware discovery; fast media controls; configurable gestures; one-handed A–Z access with haptics; and the risk of overly complex customization or weak automatic organization. | Make **App Search** a first-class launcher feature. Add contact/web results only when privacy and user control are clear. Keep gestures limited and consistently placed. Prioritize fast, reliable default organization over an extreme DIY layout editor. |
| [Building UI with Material 3 adaptive library](https://www.youtube.com/watch?v=xPUZENis4gc) | The speaker describes window size classes as a way to group behavior without caring about individual device models, and warns against merely stretching content. The analysis recommends reflowing into panes and adapting navigation across compact, medium, and expanded windows. | Implement an adaptive layout engine based on usable window size; do not name user presets after device-independent sizes. Keep one orbit on compact phones, add supporting panes or a rail on larger windows, and keep a radial centre away from hinges. |
| [Build more accessible UIs with Jetpack Compose](https://www.youtube.com/watch?v=80qkStdDWXQ) | The session emphasizes meaningful custom semantics, logical traversal order, content/action labels, scalable controls, and automated/manual accessibility checks. | Treat the orbit as an accessible app collection. Add predictable clockwise traversal, clear action labels, collection semantics, and device testing with TalkBack and Accessibility Scanner. |

### Video quotes and cautions

> “Just start typing in anything and you'll be able to find any apps, contacts, you could also search the web.” — launcher review speaker, discussing universal search. This supports search as a professional, high-speed fallback rather than relying exclusively on a visual orbit. [5]

> “These let you group UI behavior without worrying about the specific underlying device.” — Android Developers adaptive-layout session, describing window-size classes. Orbit should calculate its layout from available space and breakpoints rather than offer only Small, Balanced, or Large UI modes. [6]

The video-analysis tool is interpretive rather than a verbatim transcript; these quotes should be treated as near-exact product-design evidence, not legal or technical authority. Official Android documentation remains the source of record for implementation decisions.

### Sources

[5] https://www.youtube.com/watch?v=XnV3i3rX7zY
[6] https://www.youtube.com/watch?v=xPUZENis4gc
[7] https://www.youtube.com/watch?v=80qkStdDWXQ
