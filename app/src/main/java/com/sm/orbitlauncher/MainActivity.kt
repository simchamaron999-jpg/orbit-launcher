package com.sm.orbitlauncher

import android.Manifest
import android.app.SearchManager
import android.app.WallpaperManager
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.sm.orbitlauncher.ai.AiClient
import com.sm.orbitlauncher.data.*
import com.sm.orbitlauncher.ui.LauncherSettingsScreen
import com.sm.orbitlauncher.ui.OrbitHomeScreen
import com.sm.orbitlauncher.ui.theme.OrbitTheme
import com.sm.orbitlauncher.voice.VoiceAppLauncher
import com.sm.orbitlauncher.widget.OrbitWidgetHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private lateinit var repository: LauncherRepository
    private lateinit var widgetHost: OrbitWidgetHost
    private lateinit var voiceLauncher: VoiceAppLauncher
    private val mainHandler = Handler(Looper.getMainLooper())

    private var allApps by mutableStateOf<List<LaunchableApp>>(emptyList())
    private var launcherPages by mutableStateOf<List<LauncherPage>>(emptyList())
    private var launcherTemplates by mutableStateOf<List<LauncherTemplate>>(emptyList())
    private var appsByPage by mutableStateOf<List<List<LaunchableApp>>>(emptyList())
    
    private var centerMode by mutableStateOf(CenterMode.CLOCK)
    private var centerSize by mutableStateOf(CenterSize.BALANCED)
    private var centerActions by mutableStateOf<Map<CenterGesture, CenterAction>>(emptyMap())
    private var appTrigger by mutableStateOf(AppTrigger.TAP)
    private var rotationSpeed by mutableStateOf(RotationSpeed.BALANCED)
    private var iconScale by mutableStateOf(IconScale.COMFORTABLE)
    private var labelsVisible by mutableStateOf(true)
    private var hapticsEnabled by mutableStateOf(true)
    private var appearanceMode by mutableStateOf(AppearanceMode.SYSTEM)
    private var ambientBackdrop by mutableStateOf(AmbientBackdrop.ORBIT)
    private var builtinWallpaper by mutableStateOf<BuiltinWallpaper?>(null)
    private var wallpaperUri by mutableStateOf<String?>(null)
    private var widgetId by mutableIntStateOf(-1)
    private var tileWidgetIds by mutableStateOf<List<Int>>(emptyList())
    private var voiceState by mutableStateOf<VoiceUiState>(VoiceUiState.Idle)
    private var showSettings by mutableStateOf(false)
    private var usageAccess by mutableStateOf(false)
    private var searchRequestToken by mutableIntStateOf(0)
    
    private var aiProvider by mutableStateOf(AiProvider.OPENAI)
    private var aiApiKey by mutableStateOf("")
    private var aiEndpoint by mutableStateOf<String?>(null)
    private var clockStyle by mutableStateOf(ClockStyle.EXPRESSIVE)

    private var pendingWidgetId = -1
    private var pendingWidgetPlacement = WidgetPlacement.CENTRE
    private var voiceRequestMode = VoiceRequestMode.APP_LAUNCH

    private val microphonePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startVoiceRequest() else showVoiceFailure()
    }

    private val widgetPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleWidgetPickResult(result.resultCode, result.data)
    }

    private val widgetConfiguration = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        handleWidgetConfigurationResult(result.resultCode, result.data)
    }

    private val homeRoleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    private val wallpaperPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        runCatching { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        wallpaperUri = uri.toString()
        builtinWallpaper = null
        repository.setWallpaperUri(wallpaperUri)
        repository.setBuiltinWallpaper(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        repository = LauncherRepository(applicationContext)
        widgetHost = OrbitWidgetHost(applicationContext, repository)
        voiceLauncher = VoiceAppLauncher(applicationContext)
        
        loadPreferences()
        refreshApps()

        setContent {
            OrbitTheme(appearanceMode = appearanceMode) {
                OrbitHomeScreen(
                    pages = launcherPages,
                    appsByPage = appsByPage,
                    allApps = allApps,
                    centerMode = centerMode,
                    centerSize = centerSize,
                    centerActions = centerActions,
                    appTrigger = appTrigger,
                    rotationSpeed = rotationSpeed,
                    iconScale = iconScale,
                    labelsVisible = labelsVisible,
                    hapticsEnabled = hapticsEnabled,
                    ambientBackdrop = ambientBackdrop,
                    builtinWallpaper = builtinWallpaper,
                    wallpaperUri = wallpaperUri,
                    widgetId = widgetId,
                    tileWidgetIds = tileWidgetIds,
                    widgetHost = widgetHost,
                    voiceState = voiceState,
                    searchRequestToken = searchRequestToken,
                    onLaunchApp = ::launchApp,
                    onStartVoice = ::beginVoiceLaunch,
                    onOpenSearch = ::openSearch,
                    onAiAssistant = ::beginAiVoiceRequest,
                    onCenterAction = ::performCenterAction,
                    onSettings = { showSettings = true }
                )

                if (showSettings) {
                    LauncherSettingsScreen(
                        apps = allApps,
                        pages = launcherPages,
                        centerMode = centerMode,
                        centerSize = centerSize,
                        centerActions = centerActions,
                        appTrigger = appTrigger,
                        rotationSpeed = rotationSpeed,
                        iconScale = iconScale,
                        labelsVisible = labelsVisible,
                        hapticsEnabled = hapticsEnabled,
                        appearanceMode = appearanceMode,
                        ambientBackdrop = ambientBackdrop,
                        builtinWallpaper = builtinWallpaper,
                        hasWallpaperPhoto = wallpaperUri != null,
                        hasWidget = widgetId >= 0,
                        tileWidgets = tileWidgetIds.map { WidgetTile(it, 0f, 0f) },
                        hasUsageAccess = usageAccess,
                        isFavorite = { repository.isFavorite(it.packageName) },
                        aiProvider = aiProvider,
                        aiApiKey = aiApiKey,
                        aiEndpoint = aiEndpoint,
                        clockStyle = clockStyle,
                        templates = launcherTemplates,
                        onDismiss = { showSettings = false },
                        onUpdatePage = ::updatePage,
                        onAddPage = ::addPage,
                        onRemovePage = ::removePage,
                        onCenterMode = { mode -> centerMode = mode; repository.setSelectedCenterMode(mode) },
                        onCenterSize = { size -> centerSize = size; repository.setSelectedCenterSize(size) },
                        onCenterAction = { gesture, action ->
                            centerActions = centerActions + (gesture to action)
                            repository.setCenterAction(gesture, action)
                        },
                        onAppTrigger = { trigger -> appTrigger = trigger; repository.setAppTrigger(trigger) },
                        onRotationSpeed = { speed -> rotationSpeed = speed; repository.setRotationSpeed(speed) },
                        onIconScale = { scale -> iconScale = scale; repository.setIconScale(scale) },
                        onLabelsVisible = { visible -> labelsVisible = visible; repository.setLabelsVisible(visible) },
                        onHapticsEnabled = { enabled -> hapticsEnabled = enabled; repository.setHapticsEnabled(enabled) },
                        onAppearanceMode = { mode -> appearanceMode = mode; repository.setAppearanceMode(mode) },
                        onAmbientBackdrop = { backdrop -> ambientBackdrop = backdrop; repository.setAmbientBackdrop(backdrop) },
                        onBuiltinWallpaper = ::selectBuiltinWallpaper,
                        onPickWallpaperPhoto = { wallpaperPicker.launch(arrayOf("image/*")) },
                        onClearWallpaper = {
                            wallpaperUri = null
                            builtinWallpaper = null
                            repository.setWallpaperUri(null)
                            repository.setBuiltinWallpaper(null)
                        },
                        onApplySystemWallpaper = ::applySelectedWallpaperToSystem,
                        onPickWidget = { startWidgetPicker(WidgetPlacement.CENTRE) },
                        onRemoveWidget = {
                            widgetHost.removeWidget(widgetId)
                            widgetId = -1
                            centerMode = CenterMode.CLOCK
                            repository.setSelectedCenterMode(CenterMode.CLOCK)
                        },
                        onPickTileWidget = { startWidgetPicker(WidgetPlacement.TILE) },
                        onRemoveTileWidget = { id ->
                            widgetHost.discardWidget(id)
                            repository.removeTileWidgetId(id)
                            tileWidgetIds = repository.tileWidgetIds()
                        },
                        onUpdateTileWidgets = { },
                        onToggleFavorite = { repository.toggleFavorite(it.packageName); refreshPageApps() },
                        onAiProvider = { p -> aiProvider = p; repository.setAiProvider(p) },
                        onAiApiKey = { k -> aiApiKey = k; repository.setAiApiKey(k) },
                        onAiEndpoint = { e -> aiEndpoint = e; repository.setAiEndpoint(e) },
                        onClockStyle = { s -> clockStyle = s; repository.setClockStyle(s) },
                        onAddTemplate = ::addTemplate,
                        onInstallTemplate = ::installTemplate,
                        onRemoveTemplate = ::removeTemplate,
                        onRequestUsageAccess = ::openUsageAccessSettings,
                        onRequestDefaultHome = ::requestHomeRole
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::widgetHost.isInitialized) widgetHost.onActivityResumed()
        if (::repository.isInitialized) {
            usageAccess = repository.hasUsageAccess()
            refreshApps()
        }
    }

    override fun onPause() {
        if (::voiceLauncher.isInitialized) voiceLauncher.cancel()
        if (::widgetHost.isInitialized) widgetHost.onActivityPaused()
        super.onPause()
    }

    override fun onDestroy() {
        voiceLauncher.release()
        super.onDestroy()
    }

    private fun loadPreferences() {
        launcherPages = repository.pages()
        launcherTemplates = repository.templates()
        centerMode = repository.selectedCenterMode()
        centerSize = repository.selectedCenterSize()
        centerActions = CenterGesture.entries.associateWith(repository::centerAction)
        appTrigger = repository.appTrigger()
        rotationSpeed = repository.rotationSpeed()
        iconScale = repository.iconScale()
        labelsVisible = repository.labelsVisible()
        hapticsEnabled = repository.hapticsEnabled()
        appearanceMode = repository.appearanceMode()
        ambientBackdrop = repository.ambientBackdrop()
        builtinWallpaper = repository.builtinWallpaper()
        wallpaperUri = repository.wallpaperUri()
        widgetId = repository.widgetId()
        tileWidgetIds = repository.tileWidgetIds()
        aiProvider = repository.aiProvider()
        aiApiKey = repository.aiApiKey()
        aiEndpoint = repository.aiEndpoint()
        clockStyle = repository.clockStyle()
    }

    private fun updatePage(updated: LauncherPage) {
        launcherPages = launcherPages.map { if (it.id == updated.id) updated else it }
        repository.setPages(launcherPages)
        refreshPageApps()
    }

    private fun addPage() {
        if (launcherPages.size >= 8) return
        val newPage = LauncherPage(id = "page-${System.currentTimeMillis()}", name = "All apps", source = RingMode.ALL_APPS)
        launcherPages = launcherPages + newPage
        repository.setPages(launcherPages)
        refreshPageApps()
    }

    private fun removePage(page: LauncherPage) {
        if (launcherPages.size <= 1) return
        launcherPages = launcherPages.filterNot { it.id == page.id }
        repository.setPages(launcherPages)
        refreshPageApps()
    }

    private fun addTemplate(template: LauncherTemplate) {
        launcherTemplates = launcherTemplates + template
        repository.setTemplates(launcherTemplates)
    }

    private fun installTemplate(template: LauncherTemplate) {
        launcherPages = template.pages
        centerMode = template.centerMode
        centerSize = template.centerSize
        iconScale = template.iconScale
        labelsVisible = template.labelsVisible
        ambientBackdrop = template.ambientBackdrop
        clockStyle = template.clockStyle
        
        repository.setPages(launcherPages)
        repository.setSelectedCenterMode(centerMode)
        repository.setSelectedCenterSize(centerSize)
        repository.setIconScale(iconScale)
        repository.setLabelsVisible(labelsVisible)
        repository.setAmbientBackdrop(ambientBackdrop)
        repository.setClockStyle(clockStyle)
        
        refreshPageApps()
    }

    private fun removeTemplate(id: String) {
        launcherTemplates = launcherTemplates.filterNot { it.id == id }
        repository.setTemplates(launcherTemplates)
    }

    private fun selectBuiltinWallpaper(wallpaper: BuiltinWallpaper) {
        builtinWallpaper = wallpaper
        wallpaperUri = null
        repository.setBuiltinWallpaper(wallpaper)
        repository.setWallpaperUri(null)
    }

    private fun applySelectedWallpaperToSystem() {
        val selectedBuiltin = builtinWallpaper
        val selectedUri = wallpaperUri
        if (selectedBuiltin == null && selectedUri == null) return
        lifecycleScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val input = when {
                    selectedBuiltin != null -> resources.openRawResource(selectedBuiltin.resourceId)
                    selectedUri != null -> contentResolver.openInputStream(android.net.Uri.parse(selectedUri))
                    else -> null
                } ?: error("Selected wallpaper could not be opened")
                input.use { WallpaperManager.getInstance(applicationContext).setStream(it) }
            }
            lifecycleScope.launch(Dispatchers.Main) {
                val message = if (result.isSuccess) "Device wallpaper applied" else "Could not apply the device wallpaper"
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performCenterAction(action: CenterAction) {
        when (action) {
            CenterAction.NONE -> Unit
            CenterAction.VOICE -> beginVoiceLaunch()
            CenterAction.SEARCH -> openSearch()
            CenterAction.SETTINGS -> showSettings = true
            CenterAction.ALL_APPS -> openSearch()
            CenterAction.NEXT_WALLPAPER -> {
                val collection = BuiltinWallpaper.entries
                val currentIndex = builtinWallpaper?.let { collection.indexOf(it) } ?: -1
                selectBuiltinWallpaper(collection[(currentIndex + 1) % collection.size])
            }
        }
    }

    private fun openSearch() { searchRequestToken += 1 }

    private fun handleWidgetPickResult(resultCode: Int, data: Intent?) {
        val selectedId = widgetHost.selectedWidgetId(data).takeIf { it >= 0 } ?: pendingWidgetId
        if (resultCode != RESULT_OK || selectedId < 0) {
            if (selectedId >= 0) widgetHost.discardWidget(selectedId)
            pendingWidgetId = -1
            return
        }
        if (widgetHost.needsConfiguration(selectedId)) {
            pendingWidgetId = selectedId
            widgetHost.configurationIntent(selectedId)?.let(widgetConfiguration::launch) ?: saveWidget(selectedId)
        } else saveWidget(selectedId)
    }

    private fun handleWidgetConfigurationResult(resultCode: Int, data: Intent?) {
        val configuredId = widgetHost.selectedWidgetId(data).takeIf { it >= 0 } ?: pendingWidgetId
        if (resultCode == RESULT_OK && configuredId >= 0) saveWidget(configuredId)
        else if (configuredId >= 0) widgetHost.discardWidget(configuredId)
        pendingWidgetId = -1
    }

    private fun startWidgetPicker(placement: WidgetPlacement) {
        pendingWidgetPlacement = placement
        widgetPicker.launch(widgetHost.createPickerIntent())
    }

    private fun saveWidget(id: Int) {
        when (pendingWidgetPlacement) {
            WidgetPlacement.CENTRE -> {
                widgetId = id
                widgetHost.saveConfiguredWidget(id)
                centerMode = CenterMode.APP_WIDGET
                repository.setSelectedCenterMode(CenterMode.APP_WIDGET)
            }
            WidgetPlacement.TILE -> {
                repository.addTileWidgetId(id)
                tileWidgetIds = repository.tileWidgetIds()
            }
        }
    }

    private fun refreshApps() {
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val apps = repository.installedApps()
            lifecycleScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                allApps = apps
                refreshPageApps()
            }
        }
    }

    private fun refreshPageApps() {
        appsByPage = launcherPages.map { page ->
            val sourceApps = when (page.source) {
                RingMode.FAVORITES -> allApps.filter { repository.isFavorite(it.packageName) }
                RingMode.MOST_USED -> repository.mostUsedApps(allApps)
                RingMode.RECENT -> repository.recentApps(allApps)
                RingMode.ALL_APPS -> allApps.sortedBy { it.label.lowercase() }
            }
            sourceApps.take(page.appLimit)
        }
    }

    private fun launchApp(app: LaunchableApp) {
        packageManager.getLaunchIntentForPackage(app.packageName)?.let(::startActivity)
    }

    private fun beginVoiceLaunch() {
        voiceRequestMode = VoiceRequestMode.APP_LAUNCH
        requestVoicePermissionOrStart()
    }

    private fun beginAiVoiceRequest() {
        voiceRequestMode = VoiceRequestMode.AI_COMMAND
        requestVoicePermissionOrStart()
    }

    private fun requestVoicePermissionOrStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startVoiceRequest()
        } else {
            voiceState = VoiceUiState.RequestingPermission
            microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startVoiceRequest() {
        when (voiceRequestMode) {
            VoiceRequestMode.APP_LAUNCH -> startVoiceLaunch()
            VoiceRequestMode.AI_COMMAND -> startAiVoiceCommand()
        }
    }

    private fun startVoiceLaunch() {
        voiceState = VoiceUiState.Listening
        voiceLauncher.start(
            apps = allApps,
            onListening = { voiceState = VoiceUiState.Listening },
            onPartialPhrase = { phrase -> voiceState = VoiceUiState.Heard(phrase) },
            onResolved = { app, _ -> launchApp(app); voiceState = VoiceUiState.Idle },
            onFailure = { message ->
                voiceState = VoiceUiState.Failed(message)
                mainHandler.postDelayed({ voiceState = VoiceUiState.Idle }, 2000)
            }
        )
    }

    private fun startAiVoiceCommand() {
        if (aiApiKey.isBlank()) {
            voiceState = VoiceUiState.Failed("Add an API key in Settings > AI Engine")
            mainHandler.postDelayed({ voiceState = VoiceUiState.Idle }, 2500)
            return
        }
        voiceState = VoiceUiState.Listening
        voiceLauncher.startTranscript(
            onListening = { voiceState = VoiceUiState.Listening },
            onPartialPhrase = { phrase -> voiceState = VoiceUiState.Heard(phrase) },
            onResult = ::processAiVoiceRequest,
            onFailure = { message ->
                voiceState = VoiceUiState.Failed(message)
                mainHandler.postDelayed({ voiceState = VoiceUiState.Idle }, 2000)
            }
        )
    }

    private fun processAiVoiceRequest(phrase: String) {
        voiceState = VoiceUiState.Heard("Thinking…")
        val availableApps = allApps.joinToString("\n") { "- ${it.label} (${it.packageName})" }
        val instructions = """
            You are Orbit, a concise Android launcher assistant.
            Current time: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}
            
            User request: "$phrase"
            
            TASKS:
            1. If the user wants to open/launch an app, respond with: OPEN_APP: <package.name>
            2. If the user wants to search the web, respond with: SEARCH: <query>
            3. For general questions, give a 1-sentence helpful reply.
            
            INSTALLED APPS:
            $availableApps
        """.trimIndent()
        
        lifecycleScope.launch {
            AiClient(aiProvider, aiApiKey, aiEndpoint).complete(phrase, instructions).onSuccess { answer ->
                when {
                    answer.contains("OPEN_APP:", ignoreCase = true) -> {
                        val pkg = answer.substringAfter("OPEN_APP:").trim().split(" ", "\n")[0]
                        val app = allApps.find { it.packageName == pkg } ?: findAppByLabel(pkg)
                        if (app != null) {
                            launchApp(app)
                            voiceState = VoiceUiState.Heard("Launching ${app.label}")
                            mainHandler.postDelayed({ voiceState = VoiceUiState.Idle }, 1000)
                        } else {
                            voiceState = VoiceUiState.Heard("App not found: $pkg")
                            mainHandler.postDelayed({ voiceState = VoiceUiState.Idle }, 2000)
                        }
                    }
                    answer.contains("SEARCH:", ignoreCase = true) -> {
                        val query = answer.substringAfter("SEARCH:").trim().take(300)
                        val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                            putExtra(SearchManager.QUERY, query)
                        }
                        if (searchIntent.resolveActivity(packageManager) != null) {
                            startActivity(searchIntent)
                            voiceState = VoiceUiState.Idle
                        } else {
                            voiceState = VoiceUiState.Failed("No web search app is available on this device")
                            mainHandler.postDelayed({ voiceState = VoiceUiState.Idle }, 3000)
                        }
                    }
                    else -> {
                        voiceState = VoiceUiState.Heard(answer.trim().take(200))
                        mainHandler.postDelayed({ voiceState = VoiceUiState.Idle }, 5000)
                    }
                }
            }.onFailure { error ->
                voiceState = VoiceUiState.Failed("AI Error: ${error.localizedMessage}")
                mainHandler.postDelayed({ voiceState = VoiceUiState.Idle }, 3000)
            }
        }
    }

    private fun findAppByLabel(label: String): LaunchableApp? {
        val normalized = label.trim().lowercase()
        return allApps.firstOrNull { it.label.lowercase() == normalized }
            ?: allApps.firstOrNull { it.label.lowercase().contains(normalized) || normalized.contains(it.label.lowercase()) }
    }

    private fun showVoiceFailure() {
        voiceState = VoiceUiState.Failed("Microphone permission required")
        mainHandler.postDelayed({ voiceState = VoiceUiState.Idle }, 2000)
    }

    private fun openUsageAccessSettings() { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }

    private fun requestHomeRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                homeRoleRequest.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
            }
        } else {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        }
    }

    private enum class VoiceRequestMode { APP_LAUNCH, AI_COMMAND }
    private enum class WidgetPlacement { CENTRE, TILE }
}
