# Orbit Launcher 0.7.3 — Professional Security and Reliability Audit

**Audit date:** 12 August 2026  
**Audited application ID:** `com.sm.orbitlauncher`  
**Audited scope:** All twelve Kotlin source files, Android manifest and resources, Gradle configuration, dependency catalog, F-Droid metadata, release documentation, permissions, AI network flow, launcher lifecycle, voice recognition, widget hosting, wallpaper loading, page-density configuration, and release artifacts.

## Executive conclusion

Orbit Launcher 0.7.3 is **buildable and suitable for user testing as a debug APK**. The security review found no embedded provider API keys, no arbitrary code-loading path, no WebView surface, no exported provider/service/receiver, and no non-HTTPS network endpoint in the revised code. The only exported component is the Home/Launcher activity, which Android requires for a launcher. The code limits AI-directed app launches to activities found through the device package manager. [1] [2]

The audit identified and remediated the release-blocking defect shown in the user screenshot: the application declared no `INTERNET` permission, preventing BYOK AI network calls. A Google Gemini endpoint-construction defect was also corrected. Additional hardening now disables backup/data extraction, refuses cleartext HTTP, validates custom AI endpoints as HTTPS, cancels microphone recognition when the activity pauses, bounds AI web-search text, validates availability of an external web-search handler, and samples custom wallpaper images to reduce memory pressure.

## Audit coverage and methods

| Area | Method | Outcome |
|---|---|---|
| **Source review** | Line-by-line review of `MainActivity`, AI, voice, widget, model, repository, all UI, onboarding, and theme Kotlin files. | Completed. |
| **Manifest and component review** | Manual review of permissions, `queries`, intent filters, backup policy, cleartext setting, and exports. | Completed. |
| **Sensitive API scan** | Searched for network, filesystem, WebView, reflection, dynamic loading, broad IPC, and intent-launch APIs. | Completed; no dynamic loader or WebView found. |
| **Secret scan** | Checked for common OpenAI, Google, GitHub, AWS, and similar key formats. | No embedded secret found. |
| **Build validation** | `assembleDebug` after remediation. | **BUILD SUCCESSFUL**. |
| **Static analysis** | `lintDebug`. | **0 errors, 21 warnings**. |
| **Artifact integrity** | SHA-256 verification of the generated APK. | Verified. |

## Remediated findings

| Severity before remediation | Finding | Correction in 0.7.3 | Verification |
|---|---|---|---|
| **Release blocking** | `INTERNET` was absent, so BYOK AI requests failed with the error visible in the screenshot. | Added `INTERNET` and `ACCESS_NETWORK_STATE` permissions. | Fresh build completed successfully. |
| **Release blocking** | Google Gemini had a `null` endpoint in the provider enum, but the old client resolved the endpoint before handling the Google-specific URL. | Constructed the Gemini URL before generic endpoint resolution. | Kotlin build passed. |
| **High** | API-key preferences could be included in backup or device-transfer data under default platform behaviour. | Disabled backups and added explicit legacy and Android 12+ extraction exclusions. | Manifest and XML resources reviewed; lint passes with no data-extraction warning. |
| **High** | A custom AI endpoint could theoretically be configured with `http://`. | Enforced HTTPS for custom endpoints and set `usesCleartextTraffic="false"`. | Static source and manifest review. |
| **Medium** | Speech recognition might remain active as the launcher entered the background. | Cancel recognition in `onPause()`. | Source lifecycle review. |
| **Medium** | AI-triggered web search could throw if no compatible search application existed. | Added `resolveActivity()` guard and user-visible failure state. | Source review and build validation. |
| **Medium** | Full-size user wallpaper decoding could cause avoidable memory pressure or crashes with very large images. | Added sampled bitmap decode paths for display and luminance analysis. | Source review and build validation. |
| **Medium** | Public documentation still described unlimited page sources and the earlier version. | Updated the README for 0.7.3 and the explicit app-limit controls. | Documentation review. |
| **Low** | Info entries did not provide interactive navigation. | Added safe user-initiated mail, GitHub, and F-Droid links; restored the device-wallpaper action. | Source review and build validation. |

## Security posture

The application asks for microphone access only for user-initiated voice capture, optional usage-statistics access for Recent/Most Used page ordering, normal network access for user-configured AI calls, and wallpaper access for the explicit wallpaper action. It exposes only the launcher activity, and that activity must be exported for Android to invoke it as Home. There are no services, receivers, or content providers exposed by the manifest. Android recommends both minimising permissions and avoiding unnecessary exported components; the current manifest meets that design goal. [1] [2]

AI keys remain user-supplied and are stored in private application preferences. Backup and device-transfer extraction are disabled. The residual risk is that standard `SharedPreferences` are not hardware-backed encryption; this is acceptable for the current local BYOK prototype but should be upgraded to Android Keystore-backed encryption before a security-sensitive production launch. The launcher now requires encrypted HTTPS transport for all AI providers and rejects a custom cleartext endpoint. Android’s networking guidance recommends HTTPS for sensitive traffic, which is applied here. [1]

The AI prompt can request an app launch, but the app only launches an activity if the provider reply matches a package discovered locally. It does not execute shell commands, invoke arbitrary components, or deserialize provider-controlled code. Web search is delegated only through a user-visible Android intent after confirming a capable handler exists. These constraints reduce the risk of unsafe AI-controlled actions.

## Launcher reliability review

| Component | Reviewed behaviour | Outcome |
|---|---|---|
| **Home role** | `ROLE_HOME` is requested on Android Q+; older devices use Home settings. The activity declares the required HOME and LAUNCHER filters. | Correct for a third-party launcher. |
| **App discovery** | Installed app scans run on an IO dispatcher and publish to Compose state on Main. | Fixes the prior Home-entry delay risk. |
| **Pages and density** | Each `LauncherPage` persists `appLimit`, and app lists are trimmed with `take(page.appLimit)`. Settings expose 8–32 choices. | Correct and visible under **Settings → Apps**. |
| **Voice** | Availability is checked, errors are translated for the user, recognizer instances are released, and recognition is cancelled when the activity pauses. | Reliable one-shot interaction model. |
| **AI voice** | Requires a nonblank user API key, transcribes first, then invokes the configured provider over HTTPS. | Correct after the network-permission and Gemini fixes. |
| **Widget host** | Widget IDs are persisted, configuration is handled, and host listening tracks the activity lifecycle. | No critical issue found. |
| **Wallpaper selection** | Persisted document URIs receive read permission; custom images are decoded with sampling. | Reduced memory risk. |
| **External links** | Mail, GitHub, and F-Droid use user-initiated standard Android view intents. | Safe with existing system resolution. |

## Lint and release observations

The final Android lint run reports **0 errors and 21 warnings**. The remaining warnings are non-blocking: fixed portrait orientation is intentional because the product requirement is portrait-only; some AndroidX/Compose versions have newer releases available; and one duplicate image was detected in the wallpaper resources. The duplicate wallpaper should be replaced later to improve visual variety and reduce package size, but it is not a runtime or security vulnerability.

Two publication items remain outside the APK’s runtime-security scope. First, the project currently builds a **debug-signed APK** only. A production GitHub release should include a developer-signed release APK once the owner supplies an existing signing keystore or explicitly authorizes creation and secure handover of a new one. Second, the included `fdroid/metadata.yml` is descriptive metadata, not a complete F-Droid build recipe. F-Droid submission will need a valid `fdroiddata` metadata entry and an independently reproducible build. These are publication requirements, not flaws in the launcher’s runtime code.

## Release decision

> **Debug/test release:** Approved. The audited debug APK is built, checksummed, and ready for installation.
>
> **Production signed APK:** Pending a developer signing key or explicit authorization to generate one.
>
> **F-Droid publication:** Pending F-Droid packaging metadata and independent reproducible-build review.

## References

[1]: https://developer.android.com/privacy-and-security/security-tips "Android security checklist"
[2]: https://developer.android.com/privacy-and-security/risks/access-control-to-exported-components "Permission-based access control to exported components"
[3]: https://developer.android.com/guide/components/intents-filters "Intents and intent filters"

[1] [2] [3]
