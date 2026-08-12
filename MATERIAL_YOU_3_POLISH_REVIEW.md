# Material You 3 Polish Review — Orbit Launcher

## Design direction

Orbit Launcher should feel like a **calm, personal home surface** rather than a decorated dashboard. Its central radial-orbit concept is distinctive and should remain the visual signature. The next polish pass should use Material You 3 principles to make that signature easier to scan: semantic surfaces, predictable emphasis, restrained elevation, accessible labels, and a clear response to dense content. The dominant task is launching an app quickly; everything else should support that task without competing with it.

This review is based on the supplied phone screenshots and the current Jetpack Compose source. I reviewed the home orbit, central controls, navigation dock, and full-screen settings composition. I did not run the application in an emulator during this review, so device-specific visual behavior, animation timing, and accessibility services still require validation after implementation.

## Baseline

The app has a strong core: a clear central clock, a memorable circular app orbit, adaptive wallpaper-aware controls, a four-action dock, and a full-screen settings system. The most visible sources of friction are **dense-orbit label collisions**, **overly uniform white pods that compete with the app artwork**, **literal black/white surface colors that bypass the Material color system**, and a **settings category bar that looks like a long ungrouped chip row rather than standard settings navigation**.

## Highest-impact changes

| Priority | Screen or code location | Observation | Recommended change | Why it helps | Implementation scope | Status |
|---|---|---|---|---|---|---|
| **Must fix** | `FullCircleOrbit` and `OrbitAppSlot` | At dense counts, labels are reduced to 8sp and remain visible beside adjacent labels. The screenshot shows clipped and competing names around the lower arc. | Introduce a **dense-orbit mode** above a defined threshold (for example, 16 apps): retain full-size, tappable icons; hide persistent labels; reveal a single anchored label only for the pressed/focused icon. Keep full names in All Apps search. | Prevents unreadable text and preserves app identification without shrinking type below a usable size. | Local orbit components and app-limit guidance. | Recommended. |
| **Must fix** | `OrbitAppSlot` | The orbit uses custom pointer input without semantic role, explicit accessible label, or a reliable minimum touch target at high density. | Add `semantics { role = Role.Button; contentDescription = app.label }`, preserve at least a 48dp visual/touch target where feasible, and use a separate invisible hit area if visual icons must remain smaller. | Makes every app launch target discoverable to TalkBack and easier to use with motor-access needs. Compose accessibility relies on meaningful semantics and scalable content. [3] | Local orbit component. | Recommended. |
| **High-value polish** | `OrbitHomeScreen` controls and pods | Home controls and orbit pods use literal `Color.White` / `Color.Black` surfaces in several places, while the centre uses Material roles. This creates a mismatch with dynamic color and can look harsh over detailed wallpaper. | Define a small set of semantic launcher surface tokens—e.g., `launcherControlContainer`, `launcherControlContent`, and `launcherPodContainer`—derived from `MaterialTheme.colorScheme` plus wallpaper luminance. Use these consistently for page chip, settings, dock, centre controls, labels, and pods. | Creates one coherent Material You surface language and maintains readable contrast with wallpaper changes. Material 3 is built around color, typography, and shape systems consumed through `MaterialTheme`. [1] | Theme layer plus home components. | Recommended. |
| **High-value polish** | `CentralSurface` | The central clock uses a 72sp custom display size regardless of center size, and the top AI button plus bottom mic/search pill create three independent white controls. | Give the centre surface a defined hierarchy: clock/date first; one small primary AI affordance; a compact low-emphasis utility row. Scale clock typography from the selected center diameter and use theme display styles as the base. | Reduces visual competition and keeps the user’s focus on time plus their intended next action. M3 provides a structured display/title/body/label type scale for exactly this hierarchy. [1] | Central surface composable. | Recommended. |
| **High-value polish** | `OrbitShortcutDock` | Four icon-only actions sit in a generic dark pill. Their active/unavailable state is not apparent and the icon-only presentation is not self-explanatory for every user. | Keep the compact dock, but apply Material selected-state treatment: show a subtle `secondaryContainer`/`primaryContainer` state for the current page or unavailable previous/next action; add tooltips on long press; preserve content descriptions. | Makes page navigation understandable without adding permanent instructional text. M3 recommends choosing component emphasis and feedback based on action priority. [1] | Dock component. | Recommended. |
| **High-value polish** | `LauncherSettingsScreen` | A long horizontal `FilterChip` row represents every settings category. It is visually busy, forces horizontal scanning, and makes the screen feel less like Android settings. | Replace the all-category chip strip with either a **single-row primary tab set for 3–4 broad groups** or a standard settings list that opens each group as a dedicated full-screen page. Recommended grouping: Home, Appearance, Apps & Gestures, System & About. | Simplifies information architecture, improves scanning, and creates a more familiar Android settings experience. Navigation should reflect information architecture rather than be decorative. [1] [2] | Settings navigation and category mapping; product structure change. | **Product decision.** |
| **High-value polish** | Settings list rows | Section headings use `primary` text, choice chips recur frequently, and several rows provide title plus chip group with weak grouping. | Use `onSurface` for section title hierarchy, `onSurfaceVariant` for helper text, and place closely related settings in a `surfaceContainerLow` group with consistent 16dp/24dp internal rhythm. Use a selection dialog or segmented control when options do not need constant visibility. | Improves scanability and reduces repeated accent color, leaving primary color for selection and high-emphasis actions. [1] | Reusable settings section and choice component. | Recommended. |
| **High-value polish** | AI settings | The Save button announces success with a toast, but the text field’s existing callback may persist changes while typing. The save state is therefore visually ambiguous. | Keep a local draft key in the settings screen, save only when the user taps **Save API Key**, and replace the toast with inline success/error text next to the action. | Aligns behavior with the visible control and preserves user confidence in a sensitive-field workflow. | Local AI-settings state. | Recommended. |
| **Optional enhancement** | Wallpaper gallery | Selected wallpaper is indicated only by its container color. | Add a visible check badge and a concise selection label such as “Applied to home and system wallpaper.” | Gives a non-color cue and immediate confirmation after a visual choice. [3] | Wallpaper row. | Recommended. |
| **Optional enhancement** | Expanded device windows | The UI is intentionally portrait locked, but settings use a full-width single-column layout. | Make the settings content width-capped and use a two-pane or supporting preview pattern when the window is wide enough, without changing phone behavior. | Uses larger screens deliberately rather than merely stretching the phone layout. [2] | Settings layout. | Recommended after phone polish. |

## Recommended next update scope

The next implementation should be intentionally small and foundation-first. It should **not** redesign every feature at once.

| Step | Work item | Expected outcome |
|---|---|---|
| 1 | Add `DenseOrbitMode` and one-at-a-time label reveal | No overlapping or 8sp app labels; app identity remains available by focus and search. |
| 2 | Centralize launcher surface colors and shapes | Pods, page label, dock, and centre controls look related across light/dark/dynamic color and detailed wallpapers. |
| 3 | Add app-orbit semantics, tooltips, and state feedback | Better TalkBack labels, clearer dock behavior, and accessible action discovery. |
| 4 | Normalize settings grouping and AI save state | Settings become easier to scan and the API-key save action accurately represents persistence. |
| 5 | Validate on a compact phone, a dense 20+ app page, large font scaling, light/dark/wallpaper themes, and TalkBack semantics | Prevents regressions in the core launch flow. |

> **Product decision:** The user must choose whether dense pages should prioritize **more icons** or **persistent app names**. A single circular ring cannot present 20–32 full labels at a legible size without collision. The recommended professional default is icons-first with focus-revealed labels and All Apps search for full-name discovery.

## What the user can learn from this pass

| Principle | Orbit Launcher example |
|---|---|
| **Use semantic surfaces instead of literal colors.** | Replace repeated black and white values in the home orbit with a few centralized launcher tokens derived from the Material color scheme and wallpaper contrast. |
| **Solve density with state, not tiny typography.** | A focused label on the selected orbit item is more usable than 20–32 permanent 8sp labels. |
| **Reserve accent color for meaning.** | In Settings, primary color should communicate selected state or high-emphasis actions, not decorate every section heading. |
| **Make custom patterns accessible by naming their role.** | The radial app icons are not standard buttons visually, so their semantics and content descriptions must explicitly expose them as app-launch actions. |
| **Group settings by task, not by the number of available controls.** | Four broad settings destinations are easier to understand than a scrolling row of nine peer categories. |

## Verification and follow-up

The review verified source-level patterns in `OrbitHomeScreen.kt` and `LauncherSettingsScreen.kt` and used the user-supplied screenshots as visual evidence. It did **not** verify an emulator, device, screen-reader session, large-font setting, or dark/light wallpaper combination after a new implementation. Those checks are mandatory before calling a polish update complete.

The first high-value implementation decision is the dense-orbit label policy. Once that is approved, the foundation token and semantics work can be implemented in the same update with lower risk than a broad redesign.

## Primary references

[1] [Material Design 3 in Compose — Android Developers](https://developer.android.com/develop/ui/compose/designsystems/material3)

[2] [Build adaptive apps — Android Developers](https://developer.android.com/develop/ui/compose/build-adaptive-apps)

[3] [Accessibility in Jetpack Compose — Android Developers](https://developer.android.com/develop/ui/compose/accessibility)
