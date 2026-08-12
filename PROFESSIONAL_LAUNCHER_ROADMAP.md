# Orbit Launcher — Professional Product Research and Roadmap

## Executive summary

Orbit Launcher already has a differentiated visual idea: a central time or widget surface surrounded by a radial app orbit. The issue is not the concept. The issue is that several of its current rules are **preset-driven** rather than **context-driven**. A professional launcher should feel immediately reliable on every device, let a user find any app quickly, and keep customization available without forcing the user to become a layout designer.

The strongest recommendation is to replace the current static `Small`, `Balanced`, and `Large` center options—and the related fixed icon scales—with an **Adaptive layout engine**. The engine should size the centre, orbit radius, icons, labels, and dock from usable window space, insets, app count, and an optional user density preference. This reflects Material guidance to design for breakpoints and available space rather than individual device models; larger windows should reflow to expose useful supporting content rather than stretching the same phone layout. [1] [2]

> “These let you group UI behavior without worrying about the specific underlying device.” — Android Developers adaptive-layout session, describing window-size classes. [3]

The research also finds that a launcher earns its “professional” feeling through **reliability, discovery, and restraint**. Established launchers consistently emphasize fast app search, clear default-home behavior, personalization, widgets, manageable gestures, backup/restore, and performance support. Their documentation also exposes the non-obvious operational realities of launchers: manufacturer restrictions, battery handling, missing apps, and gesture conflicts. [4] [5] [6]

## What a professional Android launcher should include

A launcher does not need every feature that competing products advertise. It needs a trustworthy core first, then optional power features that are discoverable, privacy-conscious, and easy to reverse.

| Product layer | Must include | Why it matters | Orbit status | Recommended next action |
|---|---|---|---|---|
| **Reliable home behavior** | Correct default-home state, fast return to home, accurate installed-app list, clear empty/loading/error states | A launcher replaces the system’s most frequently used surface, so reliability is the product. | Partly present. Default-home detection exists; app loading and dense-icon behavior have needed iterative fixes. | Add explicit app-list loading and empty states; test return-to-home latency on real devices. |
| **App discovery** | Fast app search, alphabetical fallback, favorites/recent use, no-result state | Visual layouts are not sufficient when an app is unfamiliar or a page is dense. | Basic substring app search exists. | Turn search into a first-class system with result ranking, keyboard focus, and scope settings. |
| **Personalization** | Wallpaper-aware theme, icon treatment, widgets, layout density, readable labels, saved layouts | Personalization is expected, but it must not create a settings maze. | Strong base: wallpapers, templates, pages, clocks, widgets, gestures. | Replace static sizing choices with adaptive geometry plus one density control. |
| **Ergonomics** | Reachable primary actions, limited predictable gestures, haptics, clear touch targets | Launchers are used constantly and often one-handed. | Partial: dock, gestures, haptics. | Keep a small consistent gesture set; add search as a reachable primary action. |
| **Accessibility** | Meaningful semantics, logical traversal, scalable content, contrast, non-color selection cues | A radial UI is novel and must deliberately behave like an Android interface for assistive tools. | Early implementation exists for app-button semantics. | Add collection semantics, clockwise TalkBack traversal, accessibility checks, and a visible label policy. |
| **Resilience** | Backup/restore, migration, manufacturer limitation help, permission explanation | Professional launchers must explain platform restrictions instead of appearing broken. | Backup policy and templates exist; support states are limited. | Add a compact “Launcher health” panel for default-home, usage access, widget support, battery limitation advice, and backup/export. |

> “Just start typing in anything and you'll be able to find any apps, contacts, you could also search the web.” — launcher-review speaker, describing universal search. Orbit should treat search as the dependable fallback to its radial interface, rather than as a secondary hidden utility. [7]

## Current prototype-level gaps in Orbit Launcher

| Current code or behavior | Why it still feels prototype-like | Professional replacement |
|---|---|---|
| `CenterSize` is `Small`, `Balanced`, or `Large` with fixed multipliers (`0.82`, `1.0`, `1.16`). | It changes the centre without reference to the usable device area, app count, dock, or screen shape. The same setting behaves differently on every device and can crowd the orbit. | Remove fixed visible size presets. Compute adaptive geometry from available window bounds; expose only an optional density adjustment. |
| `IconScale` has fixed 40/50/60dp pods. | It is another device-independent preset and can conflict with the selected centre size. | Have the layout engine choose a safe pod size after calculating the ring circumference; user adjustment becomes a relative density value. |
| The orbit radius uses `shortest * 0.41f` and ignores top/bottom controls, system insets, custom widget tiles, and available side space. | The circle can look balanced in one screenshot but become crowded or wasteful on another form factor. | Use a safe layout region after reserving controls and insets. Calculate ring radius and centre diameter from that region. |
| Dense pages rely on a single ring. | An orbit is visually distinctive, but 20–32 apps cannot all retain legible labels around a phone-sized circle. | Keep the single ring for direct launching; use focus-revealed labels at density; route broad discovery to search or an adaptive square/list App Library. |
| The app search sheet only does a simple case-insensitive app-name substring match. | It lacks focus on open, recency/favorite ranking, aliases, an empty state, and user-controlled scope. | Add a dedicated Search screen/sheet with ranking and a Settings > Search section. Keep privacy-sensitive scopes opt-in. |
| The home layout is full-screen radial on every size. | Enlarging a radial launcher indefinitely creates empty space, not productivity. | Use breakpoint-specific composition: one hub on compact; hub plus supporting search/widget/list pane on medium and expanded windows. [1] |
| Settings offers many peer controls in a single hierarchy. | A very customizable launcher can feel like a configuration prototype if choices are exposed before users need them. | Separate **Everyday** settings from **Advanced** settings; add setting search; show contextual previews and short explanations. |

## Recommended adaptive sizing model

### Remove fixed size labels

The user-facing settings should no longer show **Small**, **Balanced**, or **Large** for the centre or icon pods. Instead, present a concise layout choice:

| User control | Purpose | Behavior |
|---|---|---|
| **Adaptive layout** — default | Fits the current device and app count | Calculates all dimensions automatically. This is the standard, recommended choice. |
| **Home density** — slider | Lets a user make the overall composition slightly tighter or more spacious | A relative adjustment from `-10%` to `+10%`; it does not map directly to a static dp size. The engine preserves safe gaps and touch targets. |
| **Custom layout** — advanced | Gives experienced users deeper control | Reveals controls for centre emphasis, orbit distance, label behavior, and dock spacing. Each remains constrained by safety limits. |
| **Layout style** — product decision | Gives the user a non-radial alternative if desired | Keep **Orbit** as default. A future **Adaptive Grid** can use square app cells in App Library or as an alternative home style. Do not automatically replace the distinctive Orbit design with a square grid. |

### Geometry contract

The engine should use real available space rather than a list of hand-picked device profiles.

```text
Inputs
  usableWidthDp, usableHeightDp
  topInsetDp, bottomInsetDp
  appCount, widgetTileCount
  densityAdjustment [-0.10, +0.10]
  windowBreakpoint

Derived values
  safeHomeHeight = usableHeightDp - topControls - bottomDock - systemInsets
  compositionSide = min(usableWidthDp, safeHomeHeight)
  centreDiameter = clamp(compositionSide × centreRatio × densityAdjustment, minCentre, maxCentre)
  orbitRadius = calculated from remaining ring area and safe gutter
  podDiameter = min(preferredAdaptivePod, circumference / appCount - safeGap)
  labelPolicy = persistent / reveal-on-hold / hidden based on appCount and pod space
```

The actual values should be implemented in dp and derived from Compose constraints/density, not from raw pixels. A device’s breakpoint then changes the **composition**, not just its scale.

| Breakpoint | Home composition | Search / navigation | Sizing behavior |
|---|---|---|---|
| **Compact** (<600dp width) | One central hub and one orbit, with bottom dock. | Bottom app search sheet or full-screen App Library. | Centre and ring fit the safe vertical region; adaptive density defaults to readable icons and focus-revealed labels after the dense threshold. |
| **Medium** (600–839dp) | Hub remains primary; show a collapsible supporting widget or App Library pane when there is clear room. | Side rail or anchored search surface when ergonomically appropriate. | Cap the radial hub so it remains touch-reachable; use extra space for content rather than ballooning the clock and icons. |
| **Expanded** (840–1199dp) | Dedicated orbit pane plus supporting pane for App Library, search results, widgets, or page manager. | Navigation rail or stable side actions. | The orbit has a maximum square canvas; companion content fills the additional space. |
| **Large / extra-large** (1200dp+) | Orbit as a centered workspace with optional second/third panes. | Persistent navigation and App Library. | Do not simply scale the phone home screen. Reflow to preserve hierarchy and scanability. |

Material’s published breakpoints are compact under 600dp, medium 600–839dp, expanded 840–1199dp, large 1200–1599dp, and extra-large at 1600dp or above. The guidance specifically calls for deciding what to reveal, divide, resize, reposition, or swap as space changes. [1]

## Search should become an explicit product feature

The user mentioned possibly adding a search setting. This is recommended, but the settings should control a clear experience—not add an unconnected option.

| Search capability | Default | Privacy / product rule | Why it belongs |
|---|---|---|---|
| **App search** | On | Uses installed app names and aliases only. | Essential fallback for every home layout. |
| **Recent and favorite ranking** | On | Ranking is local to the device. | Reduces time to common apps without opaque AI behavior. |
| **Contacts** | Off | Requires an explicit Contacts permission request and an explanation. | Useful, but should remain opt-in. |
| **Web search** | Off | Requires an explicit provider and query-sharing disclosure. | Optional convenience; never a hidden default. |
| **Calculator / settings commands** | Optional | No external data required. | Gives a command-palette quality without expanding privacy scope. |
| **Search launch gesture** | Configurable | Keep one clearly documented gesture, plus the visible search control. | Supports speed without making discovery depend on hidden gestures. |

## Feature roadmap

This is the recommended order. It intentionally prioritizes the parts users notice every time they unlock the phone over novel features.

| Release theme | Priority | Scope | Why now |
|---|---|---|---|
| **1. Adaptive foundation** | Must fix | Replace static centre/icon presets with Adaptive Layout + Home density slider; calculate a safe home region; preserve 48dp touch areas; support compact/medium/expanded composition. | Solves the user’s core sizing request and removes the largest prototype-level limitation. |
| **2. Search-first App Library** | Must fix | Improve app search, keyboard focus, result ranking, empty state, alphabetical adaptive square/list library, and Search settings. | Guarantees fast app discovery when the orbit is dense or unfamiliar. |
| **3. Launcher reliability panel** | High value | Show default-home status, app-load status, needed optional permissions, backup/export, and manufacturer/battery guidance. | Makes the launcher feel trustworthy rather than experimental. |
| **4. Everyday settings redesign** | High value | Create clear Everyday vs Advanced groups, a settings search field, preview cards, and reversible changes. | Reduces customization overload and makes the app feel finished. |
| **5. Optional power features** | Later | Widget stacks, notification summary, contact/web search, smart routines, automatic wallpapers, adaptive grid home style. | Valuable after reliability, discovery, and adaptive layout are proven. |

## Design recommendation on squares

Squares should **not** replace Orbit by default. The radial home is the product identity. A square adaptive grid is most useful as the **App Library** or as a later selectable “Layout style,” because a grid is better than a circle for scanning many apps and for supporting full labels. This gives users both strengths: Orbit for a calm, memorable home surface; Grid/List/Search for fast dense discovery.

## Verification plan before implementation is called complete

| Test | Required evidence |
|---|---|
| Compact phone | Screenshot with 8, 12, 16, and 24 apps; no overlap, clipped controls, or unusable targets. |
| Different screen sizes | Screenshots or emulator runs on at least one compact phone and one wider/tablet-like window. |
| Dense discovery | Keyboard opens search; one-character query works; empty state and app launch work. |
| Large text / TalkBack | Logical clockwise traversal around the orbit, meaningful labels, and reachable core actions. |
| Wallpaper contrast | Dark, light, and busy wallpaper checks for controls, labels, and selection state. |
| Default-home lifecycle | Home return, app launch/return, system-bar behavior, and default-launcher status work without unnecessary prompts. |

## Implementation decision needed from you

The recommended next build is **Adaptive Foundation + Search-first App Library**. This does not add a random set of cosmetic options. It removes the static size presets, gives every device a proper layout, and supplies a professional app-discovery route.

Before code work begins, please confirm two decisions:

1. Should **Orbit remain the default home style**, with a square adaptive grid used for App Library/search and later offered as an optional home layout? **Recommended: yes.**
2. Should the first professional search release remain **Apps only**, with Contacts and Web Search offered later as explicit opt-in choices? **Recommended: yes.**

## References

[1] [Material 3 Breakpoints](https://m3.material.io/foundations/layout/breakpoints)

[2] [Build adaptive apps — Android Developers](https://developer.android.com/develop/ui/compose/build-adaptive-apps)

[3] [Building UI with the Material 3 adaptive library — Android Developers, YouTube](https://www.youtube.com/watch?v=xPUZENis4gc)

[4] [Lawnchair](https://lawnchair.app/)

[5] [Smart Launcher FAQ](https://docs.smartlauncher.net/faq)

[6] [Niagara Launcher — Home Screen listing](https://play.google.com/store/apps/details?id=bitpit.launcher&hl=en_US)

[7] [Best Android Launchers review — YouTube](https://www.youtube.com/watch?v=XnV3i3rX7zY)

[8] [Material Design 3 in Compose — Android Developers](https://developer.android.com/develop/ui/compose/designsystems/material3)

[9] [Accessibility in Jetpack Compose — Android Developers](https://developer.android.com/develop/ui/compose/accessibility)
