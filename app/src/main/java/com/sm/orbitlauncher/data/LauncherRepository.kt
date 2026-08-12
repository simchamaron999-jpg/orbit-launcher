package com.sm.orbitlauncher.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Process
import org.json.JSONArray
import org.json.JSONObject

class LauncherRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun pages(): List<LauncherPage> {
        val raw = prefs.getString(KEY_PAGES, null) ?: return defaultPages()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val source = runCatching { RingMode.valueOf(item.getString("source")) }.getOrNull() ?: continue
                    val id = item.optString("id").takeIf { it.isNotBlank() } ?: "page-$i"
                    val name = item.optString("name").takeIf { it.isNotBlank() } ?: source.title
                    val limit = item.optInt("appLimit", 12)
                    add(LauncherPage(id = id, name = name, source = source, appLimit = limit))
                }
            }.ifEmpty { defaultPages() }
        }.getOrElse { defaultPages() }
    }

    fun setPages(pages: List<LauncherPage>) {
        val encoded = JSONArray().apply {
            pages.distinctBy { it.id }.take(MAX_PAGE_COUNT).forEach { page ->
                put(JSONObject().apply {
                    put("id", page.id)
                    put("name", page.name)
                    put("source", page.source.name)
                    put("appLimit", page.appLimit)
                })
            }
        }
        prefs.edit().putString(KEY_PAGES, encoded.toString()).apply()
    }

    fun templates(): List<LauncherTemplate> {
        val raw = prefs.getString(KEY_TEMPLATES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val pagesArray = obj.getJSONArray("pages")
                    val pages = buildList {
                        for (j in 0 until pagesArray.length()) {
                            val p = pagesArray.getJSONObject(j)
                            add(LauncherPage(
                                id = p.getString("id"),
                                name = p.getString("name"),
                                source = RingMode.valueOf(p.getString("source")),
                                appLimit = p.optInt("appLimit", 12)
                            ))
                        }
                    }
                    add(LauncherTemplate(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        pages = pages,
                        centerMode = CenterMode.valueOf(obj.getString("centerMode")),
                        centerSize = CenterSize.valueOf(obj.getString("centerSize")),
                        iconScale = IconScale.valueOf(obj.getString("iconScale")),
                        labelsVisible = obj.getBoolean("labelsVisible"),
                        ambientBackdrop = AmbientBackdrop.valueOf(obj.getString("ambientBackdrop")),
                        clockStyle = ClockStyle.valueOf(obj.getString("clockStyle"))
                    ))
                }
            }
        }.getOrElse { emptyList() }
    }

    fun setTemplates(templates: List<LauncherTemplate>) {
        val encoded = JSONArray().apply {
            templates.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("name", t.name)
                    put("pages", JSONArray().apply {
                        t.pages.forEach { p ->
                            put(JSONObject().apply {
                                put("id", p.id)
                                put("name", p.name)
                                put("source", p.source.name)
                                put("appLimit", p.appLimit)
                            })
                        }
                    })
                    put("centerMode", t.centerMode.name)
                    put("centerSize", t.centerSize.name)
                    put("iconScale", t.iconScale.name)
                    put("labelsVisible", t.labelsVisible)
                    put("ambientBackdrop", t.ambientBackdrop.name)
                    put("clockStyle", t.clockStyle.name)
                })
            }
        }
        prefs.edit().putString(KEY_TEMPLATES, encoded.toString()).apply()
    }

    private fun defaultPages(): List<LauncherPage> = listOf(
        LauncherPage(id = "recent", name = "Recent", source = RingMode.RECENT),
        LauncherPage(id = "most-used", name = "Most used", source = RingMode.MOST_USED),
        LauncherPage(id = "favourites", name = "Favourites", source = RingMode.FAVORITES)
    )

    fun installedApps(): List<LaunchableApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0).mapNotNull { info ->
            val label = info.loadLabel(pm).toString()
            val packageName = info.activityInfo.packageName
            val activityName = info.activityInfo.name
            val icon = info.loadIcon(pm)
            LaunchableApp(label, packageName, activityName, icon)
        }
    }

    fun isFavorite(packageName: String): Boolean = prefs.getStringSet(KEY_FAVORITES, emptySet())?.contains(packageName) == true
    fun toggleFavorite(packageName: String) {
        val set = prefs.getStringSet(KEY_FAVORITES, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (set.contains(packageName)) set.remove(packageName) else set.add(packageName)
        prefs.edit().putStringSet(KEY_FAVORITES, set).apply()
    }

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun mostUsedApps(all: List<LaunchableApp>): List<LaunchableApp> {
        if (!hasUsageAccess()) return all.take(12)
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, now - 1000 * 60 * 60 * 24 * 7, now)
        val counts = stats.associate { it.packageName to it.totalTimeInForeground }
        return all.sortedByDescending { counts[it.packageName] ?: 0L }
    }

    fun recentApps(all: List<LaunchableApp>): List<LaunchableApp> {
        if (!hasUsageAccess()) return all.take(12)
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 1000 * 60 * 60 * 24, now)
        val recents = stats.sortedByDescending { it.lastTimeUsed }.map { it.packageName }
        return all.sortedBy { val idx = recents.indexOf(it.packageName); if (idx == -1) Int.MAX_VALUE else idx }
    }

    // Settings Getters/Setters
    fun selectedCenterMode(): CenterMode = runCatching { CenterMode.valueOf(prefs.getString(KEY_CENTER_MODE, CenterMode.CLOCK.name)!!) }.getOrDefault(CenterMode.CLOCK)
    fun setSelectedCenterMode(mode: CenterMode) = prefs.edit().putString(KEY_CENTER_MODE, mode.name).apply()
    
    fun selectedCenterSize(): CenterSize = runCatching { CenterSize.valueOf(prefs.getString(KEY_CENTER_SIZE, CenterSize.BALANCED.name)!!) }.getOrDefault(CenterSize.BALANCED)
    fun setSelectedCenterSize(size: CenterSize) = prefs.edit().putString(KEY_CENTER_SIZE, size.name).apply()

    fun centerAction(gesture: CenterGesture): CenterAction = runCatching { CenterAction.valueOf(prefs.getString("gesture_${gesture.name}", CenterAction.NONE.name)!!) }.getOrDefault(CenterAction.NONE)
    fun setCenterAction(gesture: CenterGesture, action: CenterAction) = prefs.edit().putString("gesture_${gesture.name}", action.name).apply()

    fun appTrigger(): AppTrigger = runCatching { AppTrigger.valueOf(prefs.getString(KEY_APP_TRIGGER, AppTrigger.TAP.name)!!) }.getOrDefault(AppTrigger.TAP)
    fun setAppTrigger(trigger: AppTrigger) = prefs.edit().putString(KEY_APP_TRIGGER, trigger.name).apply()

    fun rotationSpeed(): RotationSpeed = runCatching { RotationSpeed.valueOf(prefs.getString(KEY_ROTATION_SPEED, RotationSpeed.BALANCED.name)!!) }.getOrDefault(RotationSpeed.BALANCED)
    fun setRotationSpeed(speed: RotationSpeed) = prefs.edit().putString(KEY_ROTATION_SPEED, speed.name).apply()

    fun iconScale(): IconScale = runCatching { IconScale.valueOf(prefs.getString(KEY_ICON_SCALE, IconScale.COMFORTABLE.name)!!) }.getOrDefault(IconScale.COMFORTABLE)
    fun setIconScale(scale: IconScale) = prefs.edit().putString(KEY_ICON_SCALE, scale.name).apply()

    fun labelsVisible(): Boolean = prefs.getBoolean(KEY_LABELS_VISIBLE, true)
    fun setLabelsVisible(visible: Boolean) = prefs.edit().putBoolean(KEY_LABELS_VISIBLE, visible).apply()

    fun hapticsEnabled(): Boolean = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
    fun setHapticsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()

    fun appearanceMode(): AppearanceMode = runCatching { AppearanceMode.valueOf(prefs.getString(KEY_APPEARANCE_MODE, AppearanceMode.SYSTEM.name)!!) }.getOrDefault(AppearanceMode.SYSTEM)
    fun setAppearanceMode(mode: AppearanceMode) = prefs.edit().putString(KEY_APPEARANCE_MODE, mode.name).apply()

    fun ambientBackdrop(): AmbientBackdrop = runCatching { AmbientBackdrop.valueOf(prefs.getString(KEY_AMBIENT_BACKDROP, AmbientBackdrop.ORBIT.name)!!) }.getOrDefault(AmbientBackdrop.ORBIT)
    fun setAmbientBackdrop(backdrop: AmbientBackdrop) = prefs.edit().putString(KEY_AMBIENT_BACKDROP, backdrop.name).apply()

    fun builtinWallpaper(): BuiltinWallpaper? = runCatching { BuiltinWallpaper.valueOf(prefs.getString(KEY_BUILTIN_WALLPAPER, null)!!) }.getOrNull()
    fun setBuiltinWallpaper(wallpaper: BuiltinWallpaper?) = prefs.edit().putString(KEY_BUILTIN_WALLPAPER, wallpaper?.name).apply()

    fun wallpaperUri(): String? = prefs.getString(KEY_WALLPAPER_URI, null)
    fun setWallpaperUri(uri: String?) = prefs.edit().putString(KEY_WALLPAPER_URI, uri).apply()

    fun widgetId(): Int = prefs.getInt(KEY_WIDGET_ID, -1)
    fun setWidgetId(id: Int) = prefs.edit().putInt(KEY_WIDGET_ID, id).apply()
    fun saveWidgetId(id: Int) = setWidgetId(id)
    fun clearWidgetId() = prefs.edit().remove(KEY_WIDGET_ID).apply()

    fun tileWidgetIds(): List<Int> = runCatching { JSONArray(prefs.getString(KEY_TILE_WIDGETS, "[]")).let { arr -> List(arr.length()) { arr.getInt(it) } } }.getOrDefault(emptyList())
    fun addTileWidgetId(id: Int) {
        val ids = tileWidgetIds() + id
        prefs.edit().putString(KEY_TILE_WIDGETS, JSONArray(ids).toString()).apply()
    }
    fun removeTileWidgetId(id: Int) {
        val ids = tileWidgetIds().filter { it != id }
        prefs.edit().putString(KEY_TILE_WIDGETS, JSONArray(ids).toString()).apply()
    }

    fun aiProvider(): AiProvider = runCatching { AiProvider.valueOf(prefs.getString(KEY_AI_PROVIDER, AiProvider.OPENAI.name)!!) }.getOrDefault(AiProvider.OPENAI)
    fun setAiProvider(provider: AiProvider) = prefs.edit().putString(KEY_AI_PROVIDER, provider.name).apply()

    fun aiApiKey(): String = prefs.getString(KEY_AI_API_KEY, "") ?: ""
    fun setAiApiKey(key: String) = prefs.edit().putString(KEY_AI_API_KEY, key).apply()

    fun aiEndpoint(): String? = prefs.getString(KEY_AI_ENDPOINT, null)
    fun setAiEndpoint(endpoint: String?) = prefs.edit().putString(KEY_AI_ENDPOINT, endpoint).apply()

    fun clockStyle(): ClockStyle = runCatching { ClockStyle.valueOf(prefs.getString(KEY_CLOCK_STYLE, ClockStyle.EXPRESSIVE.name)!!) }.getOrDefault(ClockStyle.EXPRESSIVE)
    fun setClockStyle(style: ClockStyle) = prefs.edit().putString(KEY_CLOCK_STYLE, style.name).apply()

    companion object {
        private const val PREFS_NAME = "orbit_launcher"
        private const val KEY_FAVORITES = "favorite_ids"
        private const val KEY_PAGES = "launcher_pages"
        private const val KEY_TEMPLATES = "launcher_templates"
        private const val MAX_PAGE_COUNT = 8
        private const val KEY_CENTER_MODE = "center_mode"
        private const val KEY_CENTER_SIZE = "center_size"
        private const val KEY_APP_TRIGGER = "app_trigger"
        private const val KEY_ROTATION_SPEED = "rotation_speed"
        private const val KEY_ICON_SCALE = "icon_scale"
        private const val KEY_LABELS_VISIBLE = "labels_visible"
        private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        private const val KEY_APPEARANCE_MODE = "appearance_mode"
        private const val KEY_AMBIENT_BACKDROP = "ambient_backdrop"
        private const val KEY_BUILTIN_WALLPAPER = "builtin_wallpaper"
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
        private const val KEY_WIDGET_ID = "widget_id"
        private const val KEY_TILE_WIDGETS = "tile_widgets"
        private const val KEY_AI_PROVIDER = "ai_provider"
        private const val KEY_AI_API_KEY = "ai_api_key"
        private const val KEY_AI_ENDPOINT = "ai_endpoint"
        private const val KEY_CLOCK_STYLE = "clock_style"
    }
}
