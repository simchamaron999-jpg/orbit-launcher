package com.manus.orbitlauncher.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import org.json.JSONArray
import org.json.JSONObject

class LauncherRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun installedApps(): List<LaunchableApp> {
        val usageByPackage = usageByPackage()
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
        return resolveInfos.mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            val pkg = activity.packageName
            val cls = activity.name
            val label = info.loadLabel(context.packageManager)?.toString() ?: pkg
            val icon = info.loadIcon(context.packageManager)
            val usage = usageByPackage[pkg]
            LaunchableApp(
                label = label,
                packageName = pkg,
                activityName = cls,
                icon = icon,
                lastUsedAt = usage?.first ?: 0L,
                useScore = usage?.second ?: 0L
            )
        }.distinctBy { it.stableId }.sortedBy { it.label.lowercase() }
    }

    fun mostUsedApps(allApps: List<LaunchableApp>): List<LaunchableApp> =
        allApps.sortedWith(compareByDescending<LaunchableApp> { it.useScore }.thenBy { it.label.lowercase() })

    fun recentApps(allApps: List<LaunchableApp>): List<LaunchableApp> =
        allApps.sortedWith(compareByDescending<LaunchableApp> { it.lastUsedAt }.thenBy { it.label.lowercase() })

    fun recordUsage(packageName: String) {}

    private fun usageByPackage(): Map<String, Pair<Long, Long>> {
        if (!hasUsageAccess()) return emptyMap()
        val manager = context.getSystemService(UsageStatsManager::class.java) ?: return emptyMap()
        val end = System.currentTimeMillis()
        val start = end - 30L * 24L * 60L * 60L * 1000L
        return manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            .groupBy { it.packageName }
            .mapValues { (_, stats) ->
                (stats.maxOfOrNull { it.lastTimeUsed } ?: 0L) to stats.sumOf { it.totalTimeInForeground }
            }
    }

    fun selectedRingMode(): RingMode = runCatching {
        RingMode.valueOf(prefs.getString(KEY_RING_MODE, RingMode.FAVORITES.name)!!)
    }.getOrDefault(RingMode.FAVORITES)

    fun setSelectedRingMode(mode: RingMode) = prefs.edit().putString(KEY_RING_MODE, mode.name).apply()

    fun pages(): List<LauncherPage> {
        val raw = prefs.getString(KEY_PAGES, null) ?: return defaultPages()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val source = runCatching { RingMode.valueOf(item.getString("source")) }.getOrNull() ?: continue
                    val id = item.optString("id").takeIf { it.isNotBlank() } ?: "page-$index"
                    val name = item.optString("name").takeIf { it.isNotBlank() } ?: source.title
                    add(LauncherPage(id = id, name = name, source = source))
                }
            }.ifEmpty { defaultPages() }
        }.getOrElse { defaultPages() }
    }

    fun setPages(pages: List<LauncherPage>) {
        val normalized = pages.distinctBy { it.id }.take(MAX_PAGE_COUNT).ifEmpty { defaultPages() }
        val encoded = JSONArray().apply {
            normalized.forEach { page ->
                put(JSONObject().apply {
                    put("id", page.id)
                    put("name", page.name)
                    put("source", page.source.name)
                })
            }
        }
        prefs.edit().putString(KEY_PAGES, encoded.toString()).apply()
    }

    private fun defaultPages(): List<LauncherPage> = listOf(
        LauncherPage(id = "recent", name = "Recent", source = RingMode.RECENT),
        LauncherPage(id = "most-used", name = "Most used", source = RingMode.MOST_USED),
        LauncherPage(id = "favourites", name = "Favourites", source = RingMode.FAVORITES)
    )

    fun selectedCenterMode(): CenterMode = CenterMode.valueOf(prefs.getString(KEY_CENTER_MODE, CenterMode.CLOCK.name)!!)
    fun setSelectedCenterMode(mode: CenterMode) = prefs.edit().putString(KEY_CENTER_MODE, mode.name).apply()

    fun selectedCenterSize(): CenterSize = CenterSize.valueOf(prefs.getString(KEY_CENTER_SIZE, CenterSize.BALANCED.name)!!)
    fun setSelectedCenterSize(size: CenterSize) = prefs.edit().putString(KEY_CENTER_SIZE, size.name).apply()

    fun centerAction(gesture: CenterGesture): CenterAction {
        val defaultAction = when (gesture) {
            CenterGesture.TAP -> CenterAction.NONE
            CenterGesture.DOUBLE_TAP -> CenterAction.VOICE
            CenterGesture.TRIPLE_TAP -> CenterAction.SEARCH
            CenterGesture.LONG_PRESS -> CenterAction.SETTINGS
        }
        val name = prefs.getString(KEY_CENTER_ACTION_PREFIX + gesture.name, defaultAction.name)!!
        return runCatching { CenterAction.valueOf(name) }.getOrDefault(defaultAction)
    }

    fun setCenterAction(gesture: CenterGesture, action: CenterAction) {
        prefs.edit().putString(KEY_CENTER_ACTION_PREFIX + gesture.name, action.name).apply()
    }

    fun appTrigger(): AppTrigger = AppTrigger.valueOf(prefs.getString(KEY_APP_TRIGGER, AppTrigger.TAP.name)!!)
    fun setAppTrigger(trigger: AppTrigger) = prefs.edit().putString(KEY_APP_TRIGGER, trigger.name).apply()

    fun orbitCapacity(): OrbitCapacity = OrbitCapacity.valueOf(prefs.getString(KEY_ORBIT_CAPACITY, OrbitCapacity.BALANCED.name)!!)
    fun setOrbitCapacity(capacity: OrbitCapacity) = prefs.edit().putString(KEY_ORBIT_CAPACITY, capacity.name).apply()

    fun rotationSpeed(): RotationSpeed = RotationSpeed.valueOf(prefs.getString(KEY_ROTATION_SPEED, RotationSpeed.BALANCED.name)!!)
    fun setRotationSpeed(speed: RotationSpeed) = prefs.edit().putString(KEY_ROTATION_SPEED, speed.name).apply()

    fun iconScale(): IconScale = IconScale.valueOf(prefs.getString(KEY_ICON_SCALE, IconScale.COMFORTABLE.name)!!)
    fun setIconScale(scale: IconScale) = prefs.edit().putString(KEY_ICON_SCALE, scale.name).apply()

    fun labelsVisible(): Boolean = prefs.getBoolean(KEY_LABELS_VISIBLE, true)
    fun setLabelsVisible(visible: Boolean) = prefs.edit().putBoolean(KEY_LABELS_VISIBLE, visible).apply()

    fun hapticsEnabled(): Boolean = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
    fun setHapticsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()

    fun appearanceMode(): AppearanceMode = AppearanceMode.valueOf(prefs.getString(KEY_APPEARANCE_MODE, AppearanceMode.SYSTEM.name)!!)
    fun setAppearanceMode(mode: AppearanceMode) = prefs.edit().putString(KEY_APPEARANCE_MODE, mode.name).apply()

    fun ambientBackdrop(): AmbientBackdrop = AmbientBackdrop.valueOf(prefs.getString(KEY_AMBIENT_BACKDROP, AmbientBackdrop.ORBIT.name)!!)
    fun setAmbientBackdrop(backdrop: AmbientBackdrop) = prefs.edit().putString(KEY_AMBIENT_BACKDROP, backdrop.name).apply()

    fun builtinWallpaper(): BuiltinWallpaper? {
        val name = prefs.getString(KEY_BUILTIN_WALLPAPER, null) ?: return null
        return runCatching { BuiltinWallpaper.valueOf(name) }.getOrNull()
    }

    fun setBuiltinWallpaper(wallpaper: BuiltinWallpaper?) {
        prefs.edit().apply {
            if (wallpaper == null) remove(KEY_BUILTIN_WALLPAPER) else putString(KEY_BUILTIN_WALLPAPER, wallpaper.name)
        }.apply()
    }

    fun wallpaperUri(): String? = prefs.getString(KEY_WALLPAPER_URI, null)

    fun setWallpaperUri(uri: String?) {
        prefs.edit().apply {
            if (uri == null) remove(KEY_WALLPAPER_URI) else putString(KEY_WALLPAPER_URI, uri)
        }.apply()
    }

    fun aiProvider(): AiProvider = runCatching {
        AiProvider.valueOf(prefs.getString(KEY_AI_PROVIDER, AiProvider.OPENAI.name)!!)
    }.getOrDefault(AiProvider.OPENAI)
    fun setAiProvider(provider: AiProvider) = prefs.edit().putString(KEY_AI_PROVIDER, provider.name).apply()

    fun aiApiKey(): String = prefs.getString(KEY_AI_API_KEY, "")!!
    fun setAiApiKey(key: String) = prefs.edit().putString(KEY_AI_API_KEY, key).apply()

    fun aiEndpoint(): String? = prefs.getString(KEY_AI_ENDPOINT, null)
    fun setAiEndpoint(endpoint: String?) = prefs.edit().putString(KEY_AI_ENDPOINT, endpoint).apply()

    fun clockStyle(): ClockStyle = ClockStyle.valueOf(prefs.getString(KEY_CLOCK_STYLE, ClockStyle.EXPRESSIVE.name)!!)
    fun setClockStyle(style: ClockStyle) = prefs.edit().putString(KEY_CLOCK_STYLE, style.name).apply()

    fun tileWidgetIds(): List<Int> = prefs.getString(KEY_TILE_WIDGET_IDS, "")
        .orEmpty()
        .split(',')
        .mapNotNull { it.toIntOrNull() }
        .distinct()
        .take(4)

    fun addTileWidgetId(widgetId: Int) {
        val ids = (tileWidgetIds() + widgetId).distinct().take(4)
        prefs.edit().putString(KEY_TILE_WIDGET_IDS, ids.joinToString(",")).apply()
    }

    fun removeTileWidgetId(widgetId: Int) {
        prefs.edit().putString(
            KEY_TILE_WIDGET_IDS,
            tileWidgetIds().filterNot { it == widgetId }.joinToString(",")
        ).apply()
    }

    fun widgetId(): Int = prefs.getInt(KEY_WIDGET_ID, -1)
    fun setWidgetId(id: Int) = prefs.edit().putInt(KEY_WIDGET_ID, id).apply()
    fun saveWidgetId(id: Int) = setWidgetId(id)
    fun clearWidgetId() = prefs.edit().remove(KEY_WIDGET_ID).apply()

    fun favorites(): Set<String> = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    fun isFavorite(packageName: String): Boolean = favorites().any { it.startsWith(packageName) }
    fun toggleFavorite(packageName: String) {
        val current = favorites().toMutableSet()
        val matching = current.find { it.startsWith(packageName) }
        if (matching != null) current.remove(matching) else current.add(packageName)
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
    }

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        return appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        ) == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageAccess(context: Context) {
        context.startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    fun requestDefaultHome(context: Context) {
        context.startActivity(Intent(android.provider.Settings.ACTION_HOME_SETTINGS))
    }

    companion object {
        private const val PREFS_NAME = "orbit_launcher"
        private const val KEY_FAVORITES = "favorite_ids"
        private const val KEY_RING_MODE = "ring_mode"
        private const val KEY_PAGES = "launcher_pages"
        private const val MAX_PAGE_COUNT = 8
        private const val KEY_CENTER_MODE = "center_mode"
        private const val KEY_CENTER_SIZE = "center_size"
        private const val KEY_APP_TRIGGER = "app_trigger"
        private const val KEY_ICON_SCALE = "icon_scale"
        private const val KEY_LABELS_VISIBLE = "labels_visible"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val KEY_APPEARANCE_MODE = "appearance_mode"
        private const val KEY_AMBIENT_BACKDROP = "ambient_backdrop"
        private const val KEY_CENTER_ACTION_PREFIX = "center_action_"
        private const val KEY_ORBIT_CAPACITY = "orbit_capacity"
        private const val KEY_ROTATION_SPEED = "rotation_speed"
        private const val KEY_BUILTIN_WALLPAPER = "builtin_wallpaper"
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
        private const val KEY_AI_PROVIDER = "ai_provider"
        private const val KEY_AI_API_KEY = "ai_api_key"
        private const val KEY_AI_ENDPOINT = "ai_endpoint"
        private const val KEY_CLOCK_STYLE = "clock_style"
        private const val KEY_TILE_WIDGET_IDS = "tile_widget_ids"
        private const val KEY_WIDGET_ID = "widget_id"
    }
}
