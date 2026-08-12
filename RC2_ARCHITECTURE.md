# Orbit Launcher 0.6 (Release Candidate 2) — Architecture

## Core Modules & Updates

1. **AI Engine (BYOK)**
   - Providers: OpenAI, Anthropic, Google Gemini, OpenRouter, and a custom endpoint.
   - User-entered keys remain in the app's private preferences.
   - Fallback handling for offline mode and intent parsing.

2. **Full-Screen Settings**
   - Replaces bottom sheet with a dedicated, full-screen Material You interface.
   - Top category navigation bar.
   - Clean, divider-free layout with collapsible or tabbed sections.

3. **Clock & UI Gallery**
   - Material You 3 clock styles (Expressive Digital, Modern Analog, Info-Centric, Minimalist).
   - Adjustable transparency, blur, haptics, and reduction-of-motion toggle.

4. **Widget & Layout Engine**
   - Unlimited square/rectangular resizable widget tiles.
   - Drag-and-drop app positioning with grid constraints.
   - Undo snackbar for app/widget removals.

5. **Onboarding & Utilities**
   - First-run tutorial (triggers only on fresh installs).
   - JSON Backup and Restore for settings and layouts.
