package com.sm.orbitlauncher.ui

import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.sm.orbitlauncher.data.AiProvider
import com.sm.orbitlauncher.data.AmbientBackdrop
import com.sm.orbitlauncher.data.AppearanceMode
import com.sm.orbitlauncher.data.AppTrigger
import com.sm.orbitlauncher.data.BuiltinWallpaper
import com.sm.orbitlauncher.data.CenterAction
import com.sm.orbitlauncher.data.CenterGesture
import com.sm.orbitlauncher.data.CenterMode
import com.sm.orbitlauncher.data.CenterSize
import com.sm.orbitlauncher.data.ClockStyle
import com.sm.orbitlauncher.data.IconScale
import com.sm.orbitlauncher.data.HomeLayoutMode
import com.sm.orbitlauncher.data.LaunchableApp
import com.sm.orbitlauncher.data.LauncherPage
import com.sm.orbitlauncher.data.LauncherTemplate
import com.sm.orbitlauncher.data.RingMode
import com.sm.orbitlauncher.data.RotationSpeed
import com.sm.orbitlauncher.data.WallpaperCategory

private enum class SettingsCategory(val title: String) {
    QUICK("Quick"),
    AI("AI Engine"),
    GESTURES("Gestures"),
    APPS("Apps"),
    TEMPLATES("Templates"),
    WALLPAPERS("Wallpapers"),
    WIDGETS("Widgets"),
    SYSTEM("System"),
    INFO("Info")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherSettingsScreen(
    apps: List<LaunchableApp>,
    pages: List<LauncherPage>,
    centerMode: CenterMode,
    centerSize: CenterSize, // Retained only for legacy layout templates.
    homeLayoutMode: HomeLayoutMode,
    homeDensity: Float,
    centerActions: Map<CenterGesture, CenterAction>,
    appTrigger: AppTrigger,
    rotationSpeed: RotationSpeed,
    iconScale: IconScale,
    labelsVisible: Boolean,
    hapticsEnabled: Boolean,
    appearanceMode: AppearanceMode,
    ambientBackdrop: AmbientBackdrop,
    builtinWallpaper: BuiltinWallpaper?,
    hasWallpaperPhoto: Boolean,
    hasWidget: Boolean,
    tileWidgets: List<com.sm.orbitlauncher.data.WidgetTile>,
    hasUsageAccess: Boolean,
    isFavorite: (LaunchableApp) -> Boolean,
    aiProvider: AiProvider,
    aiApiKey: String,
    aiEndpoint: String?,
    clockStyle: ClockStyle,
    onDismiss: () -> Unit,
    onUpdatePage: (LauncherPage) -> Unit,
    onAddPage: () -> Unit,
    onRemovePage: (LauncherPage) -> Unit,
    onCenterMode: (CenterMode) -> Unit,
    onCenterSize: (CenterSize) -> Unit,
    onHomeLayoutMode: (HomeLayoutMode) -> Unit,
    onHomeDensity: (Float) -> Unit,
    onCenterAction: (CenterGesture, CenterAction) -> Unit,
    onAppTrigger: (AppTrigger) -> Unit,
    onRotationSpeed: (RotationSpeed) -> Unit,
    onIconScale: (IconScale) -> Unit,
    onLabelsVisible: (Boolean) -> Unit,
    onHapticsEnabled: (Boolean) -> Unit,
    onAppearanceMode: (AppearanceMode) -> Unit,
    onAmbientBackdrop: (AmbientBackdrop) -> Unit,
    onBuiltinWallpaper: (BuiltinWallpaper) -> Unit,
    onPickWallpaperPhoto: () -> Unit,
    onClearWallpaper: () -> Unit,
    onApplySystemWallpaper: () -> Unit,
    onPickWidget: () -> Unit,
    onRemoveWidget: () -> Unit,
    onPickTileWidget: () -> Unit,
    onRemoveTileWidget: (Int) -> Unit,
    onUpdateTileWidgets: (List<com.sm.orbitlauncher.data.WidgetTile>) -> Unit,
    onToggleFavorite: (LaunchableApp) -> Unit,
    onAiProvider: (AiProvider) -> Unit,
    onAiApiKey: (String) -> Unit,
    onAiEndpoint: (String?) -> Unit,
    onClockStyle: (ClockStyle) -> Unit,
    templates: List<LauncherTemplate>,
    onAddTemplate: (LauncherTemplate) -> Unit,
    onInstallTemplate: (LauncherTemplate) -> Unit,
    onRemoveTemplate: (String) -> Unit,
    onRequestUsageAccess: () -> Unit,
    onRequestDefaultHome: () -> Unit
) {
    var currentCategory by remember { mutableStateOf(SettingsCategory.QUICK) }
    var aiApiKeyDraft by remember(aiApiKey) { mutableStateOf(aiApiKey) }
    var aiKeySaveFeedback by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val isDefaultHome = remember(context) {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolvedHome = context.packageManager.resolveActivity(homeIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        resolvedHome?.activityInfo?.packageName == context.packageName
    }
    fun openExternal(uri: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        }
    }
    
    BackHandler(onBack = onDismiss)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orbit Settings") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back to Home")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            CategoryBar(selected = currentCategory, onSelect = { currentCategory = it })
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                when (currentCategory) {
                    SettingsCategory.QUICK -> {
                        item { SectionTitle("Home Presets") }
                        item {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item { Spacer(Modifier.size(24.dp, 1.dp)) }
                                item { PresetButton("Minimalist") { onHomeLayoutMode(HomeLayoutMode.ADAPTIVE); onLabelsVisible(false) } }
                                item { PresetButton("Balanced") { onHomeLayoutMode(HomeLayoutMode.ADAPTIVE); onLabelsVisible(true) } }
                                item { PresetButton("Dense") { onHomeLayoutMode(HomeLayoutMode.CUSTOM); onHomeDensity(-0.06f); onLabelsVisible(false) } }
                                item { PresetButton("Focus") { onHomeLayoutMode(HomeLayoutMode.CUSTOM); onHomeDensity(0.04f); onLabelsVisible(false); onAmbientBackdrop(AmbientBackdrop.DUSK) } }
                                item { PresetButton("Night") { onAppearanceMode(AppearanceMode.DARK); onAmbientBackdrop(AmbientBackdrop.ORBIT); onLabelsVisible(false) } }
                                item { PresetButton("Bright") { onAppearanceMode(AppearanceMode.LIGHT); onAmbientBackdrop(AmbientBackdrop.CLAY); onLabelsVisible(true) } }
                                item { Spacer(Modifier.size(24.dp, 1.dp)) }
                            }
                        }
                        item { SectionTitle("Visuals") }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Schedule, null) },
                                headlineContent = { Text("Clock style") },
                                trailingContent = { Text(clockStyle.title, color = MaterialTheme.colorScheme.primary) }
                            )
                        }
                        item { ChoiceChips(ClockStyle.entries.toList(), clockStyle, { it.title }, onClockStyle) }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Tune, null) },
                                headlineContent = { Text("Home layout") },
                                supportingContent = { Text("Orbit automatically fits your screen, controls, and app count") },
                                trailingContent = { Text(homeLayoutMode.title, color = MaterialTheme.colorScheme.primary) }
                            )
                        }
                        item { ChoiceChips(HomeLayoutMode.entries.toList(), homeLayoutMode, { it.title }, onHomeLayoutMode) }
                        if (homeLayoutMode == HomeLayoutMode.CUSTOM) {
                            item {
                                Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                    Text("Custom density", style = MaterialTheme.typography.labelLarge)
                                    Text(
                                        if (homeDensity >= 0f) "Spacious +${(homeDensity * 100).toInt()}%" else "Compact ${(homeDensity * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Slider(
                                        value = homeDensity,
                                        onValueChange = onHomeDensity,
                                        valueRange = -0.10f..0.10f
                                    )
                                }
                            }
                        }
                        item { SettingSwitch("App names", "Show persistent labels when there is room; reveal names on hold in dense orbits", labelsVisible, onLabelsVisible) }
                        item { SectionTitle("Quick customization") }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Settings, null) },
                                headlineContent = { Text("Appearance") },
                                supportingContent = { Text("Match system, keep light, or keep dark") }
                            )
                        }
                        item { ChoiceChips(AppearanceMode.entries.toList(), appearanceMode, { it.title }, onAppearanceMode) }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Image, null) },
                                headlineContent = { Text("Ambient backdrop") },
                                supportingContent = { Text("Used when no wallpaper is selected") }
                            )
                        }
                        item { ChoiceChips(AmbientBackdrop.entries.toList(), ambientBackdrop, { it.title }, onAmbientBackdrop) }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Gesture, null) },
                                headlineContent = { Text("App launch gesture") },
                                supportingContent = { Text("Choose the gesture that opens an orbit icon") }
                            )
                        }
                        item { ChoiceChips(AppTrigger.entries.toList(), appTrigger, { it.title }, onAppTrigger) }
                    }

                    SettingsCategory.AI -> {
                        item { SectionTitle("OpenRouter AI Engine") }
                        item {
                            Text(
                                "Orbit Launcher uses OpenRouter with the free & unlimited model (google/gemma-4-31b-it:free). Enter your OpenRouter API key below.",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        item {
                            val context = LocalContext.current
                            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                OutlinedTextField(
                                    value = aiApiKeyDraft,
                                    onValueChange = {
                                        aiApiKeyDraft = it
                                        aiKeySaveFeedback = null
                                    },
                                    label = { Text("OpenRouter API Key") },
                                    supportingText = { Text("Stored only after you tap Save API Key.") },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = PasswordVisualTransformation()
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            onAiApiKey(aiApiKeyDraft.trim())
                                            aiKeySaveFeedback = if (aiApiKeyDraft.isBlank()) {
                                                "Saved without an API key. Orbit AI remains unavailable until a key is entered."
                                            } else {
                                                "OpenRouter API key saved on this device."
                                            }
                                        }
                                    ) {
                                        Text("Save API Key")
                                    }
                                }
                                aiKeySaveFeedback?.let { feedback ->
                                    Text(
                                        text = feedback,
                                        modifier = Modifier.padding(top = 8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    SettingsCategory.GESTURES -> {
                        item { SectionTitle("Orbit Gestures") }
                        items(CenterGesture.entries.toList()) { gesture ->
                            val selected = centerActions[gesture] ?: CenterAction.NONE
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Gesture, null) },
                                headlineContent = { Text(gesture.title) },
                                supportingContent = { Text(selected.title) }
                            )
                            GestureActionRow(selected = selected, onSelect = { onCenterAction(gesture, it) })
                        }
                    }

                    SettingsCategory.TEMPLATES -> {
                        item { SectionTitle("Layout Templates") }
                        item {
                            Text(
                                "Save the current Orbit layout as a template, then install or remove it whenever you want. Templates save page sources, app limits, and visual preferences on this device.",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        item {
                            OutlinedButton(
                                onClick = {
                                    onAddTemplate(LauncherTemplate(
                                        id = "template-${System.currentTimeMillis()}",
                                        name = "My Layout ${templates.size + 1}",
                                        pages = pages,
                                        centerMode = centerMode,
                                        centerSize = centerSize,
                                        iconScale = iconScale,
                                        labelsVisible = labelsVisible,
                                        ambientBackdrop = ambientBackdrop,
                                        clockStyle = clockStyle
                                    ))
                                },
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            ) { Text("Save current layout") }
                        }
                        if (templates.isEmpty()) {
                            item { Text("No saved templates yet.", Modifier.padding(24.dp), style = MaterialTheme.typography.bodyMedium) }
                        }
                        items(templates, key = { it.id }) { template ->
                            ListItem(
                                headlineContent = { Text(template.name) },
                                supportingContent = { Text("${template.pages.size} pages · ${template.ambientBackdrop.title}") },
                                trailingContent = {
                                    Row {
                                        TextButton(onClick = { onInstallTemplate(template) }) { Text("Install") }
                                        TextButton(onClick = { onRemoveTemplate(template.id) }) { Text("Delete") }
                                    }
                                }
                            )
                        }
                    }

                    SettingsCategory.APPS -> {
                        item { SectionTitle("Home pages") }
                        item {
                            Text(
                                "Each page has its own app source. Choose exactly how many apps are visible on each page so the orbit remains clear and easy to tap.",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(pages, key = { it.id }) { page ->
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Apps, null) },
                                headlineContent = { Text(page.name) },
                                supportingContent = { Text(page.source.subtitle) },
                                trailingContent = {
                                    if (pages.size > 1) {
                                        TextButton(onClick = { onRemovePage(page) }) { Text("Remove") }
                                    }
                                }
                            )
                            ChoiceChips(RingMode.entries.toList(), page.source, { it.title }) { source ->
                                onUpdatePage(page.copy(name = source.title, source = source))
                            }
                            ListItem(
                                headlineContent = { Text("Apps visible on this page") },
                                supportingContent = { Text("Currently ${page.appLimit} apps") }
                            )
                            ChoiceChips(listOf(8, 12, 16, 20, 24, 32), page.appLimit, { it.toString() }) { limit ->
                                onUpdatePage(page.copy(appLimit = limit))
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = onAddPage,
                                enabled = pages.size < 8,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            ) { Text("Add page") }
                        }
                        item { SectionTitle("Orbit motion") }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Speed, null) },
                                headlineContent = { Text("Rotation speed") },
                                trailingContent = { Text(rotationSpeed.title, color = MaterialTheme.colorScheme.primary) }
                            )
                        }
                        item { ChoiceChips(RotationSpeed.entries.toList(), rotationSpeed, { it.title }, onRotationSpeed) }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Search, null) },
                                headlineContent = { Text("App Library and Search") },
                                supportingContent = { Text("The grid App Library automatically adapts to your screen and provides full app-name search") }
                            )
                        }
                        item { SettingSwitch("Haptic feedback", "Vibrate on orbit interactions", hapticsEnabled, onHapticsEnabled) }
                    }

                    SettingsCategory.WALLPAPERS -> {
                        item { SectionTitle("Gallery") }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Image, null) },
                                headlineContent = { Text("Apply as device wallpaper") },
                                supportingContent = { Text("Use the selected Orbit wallpaper on the Android Home and Recent Apps background") },
                                trailingContent = {
                                    OutlinedButton(
                                        onClick = onApplySystemWallpaper,
                                        enabled = builtinWallpaper != null || hasWallpaperPhoto
                                    ) { Text("Apply") }
                                }
                            )
                        }
                        WallpaperCategory.entries.forEach { cat ->
                            item { Text(cat.title, Modifier.padding(horizontal = 24.dp, vertical = 8.dp), style = MaterialTheme.typography.titleSmall) }
                            item {
                                BuiltinWallpaperRow(
                                    wallpapers = BuiltinWallpaper.entries.filter { it.category == cat },
                                    selected = builtinWallpaper,
                                    onSelect = onBuiltinWallpaper
                                )
                            }
                        }
                    }

                    SettingsCategory.WIDGETS -> {
                        item { SectionTitle("Widget system") }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Widgets, null) },
                                headlineContent = { Text(if (hasWidget) "Replace centre widget" else "Add centre widget") },
                                trailingContent = { OutlinedButton(onClick = onPickWidget) { Text("Choose") } }
                            )
                        }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Widgets, null) },
                                headlineContent = { Text("Square tiles") },
                                supportingContent = { Text("${tileWidgets.size} widgets added") },
                                trailingContent = { OutlinedButton(onClick = onPickTileWidget) { Text("Add") } }
                            )
                        }
                        items(tileWidgets) { widget ->
                            ListItem(
                                headlineContent = { Text("Widget ${widget.id}") },
                                supportingContent = { Text("Size: ${widget.width}x${widget.height}") },
                                trailingContent = { TextButton(onClick = { onRemoveTileWidget(widget.id) }) { Text("Remove") } }
                            )
                        }
                    }

                    SettingsCategory.SYSTEM -> {
                        item { SectionTitle("Orbit System") }
                        if (!isDefaultHome) {
                            item {
                                ListItem(
                                    leadingContent = { Icon(Icons.Outlined.Home, null) },
                                    headlineContent = { Text("Set as default home") },
                                    trailingContent = { Button(onClick = onRequestDefaultHome) { Text("Set") } }
                                )
                            }
                        } else {
                            item {
                                ListItem(
                                    leadingContent = { Icon(Icons.Outlined.Home, null) },
                                    headlineContent = { Text("Default Launcher") },
                                    supportingContent = { Text("Orbit Launcher is currently active as your default home.") }
                                )
                            }
                        }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Insights, null) },
                                headlineContent = { Text("Usage statistics") },
                                trailingContent = { if (!hasUsageAccess) OutlinedButton(onClick = onRequestUsageAccess) { Text("Allow") } else Text("Allowed") }
                            )
                        }
                    }

                    SettingsCategory.INFO -> {
                        item { SectionTitle("About Orbit Launcher") }
                        item {
                            ListItem(
                                headlineContent = { Text("Creator") },
                                supportingContent = { Text("Simcha Maron") }
                            )
                        }
                        item {
                            ListItem(
                                modifier = Modifier.clickable { openExternal("mailto:simchamaronapp@gmail.app") },
                                headlineContent = { Text("Email") },
                                supportingContent = { Text("simchamaronapp@gmail.app") }
                            )
                        }
                        item {
                            ListItem(
                                headlineContent = { Text("Version") },
                                supportingContent = { Text("0.7.5") }
                            )
                        }
                        item {
                            ListItem(
                                modifier = Modifier.clickable { openExternal("https://github.com/simchamaron999-jpg/orbit-launcher") },
                                headlineContent = { Text("GitHub repository") },
                                supportingContent = { Text("github.com/simchamaron999-jpg/orbit-launcher") }
                            )
                        }
                        item {
                            ListItem(
                                modifier = Modifier.clickable { openExternal("https://gitlab.com/fdroid/fdroiddata") },
                                headlineContent = { Text("F-Droid repository") },
                                supportingContent = { Text("Open F-Droid source repository") }
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(48.dp)) }
            }
        }
    }
}

@Composable
private fun CategoryBar(selected: SettingsCategory, onSelect: (SettingsCategory) -> Unit) {
    val categories = SettingsCategory.entries.toList()
    ScrollableTabRow(
        selectedTabIndex = categories.indexOf(selected).coerceAtLeast(0),
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        categories.forEach { category ->
            Tab(
                selected = selected == category,
                onClick = { onSelect(category) },
                text = { Text(category.title, maxLines = 1) }
            )
        }
    }
}

@Composable
private fun PresetButton(label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) { Text(label) }
}

@Composable
private fun SectionTitle(value: String) {
    Text(
        text = value,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun <T> ChoiceChips(choices: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(Modifier.size(24.dp, 1.dp)) }
        items(choices) { choice -> FilterChip(selected == choice, { onSelect(choice) }, label = { Text(label(choice), maxLines = 1) }) }
        item { Spacer(Modifier.size(24.dp, 1.dp)) }
    }
}

@Composable
private fun SettingSwitch(title: String, detail: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(detail) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) }
    )
}

@Composable
private fun GestureActionRow(selected: CenterAction, onSelect: (CenterAction) -> Unit) {
    LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(Modifier.size(20.dp, 1.dp)) }
        items(CenterAction.entries.toList()) { action ->
            FilterChip(selected == action, { onSelect(action) }, label = { Text(action.title) })
        }
        item { Spacer(Modifier.size(20.dp, 1.dp)) }
    }
}

@Composable
private fun BuiltinWallpaperRow(
    wallpapers: List<BuiltinWallpaper>,
    selected: BuiltinWallpaper?,
    onSelect: (BuiltinWallpaper) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.size(20.dp, 1.dp)) }
        items(wallpapers) { wallpaper ->
            Surface(
                onClick = { onSelect(wallpaper) },
                modifier = Modifier.size(width = 94.dp, height = 142.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (selected == wallpaper) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = if (selected == wallpaper) 5.dp else 1.dp
            ) {
                Column {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth().height(112.dp).clip(MaterialTheme.shapes.medium),
                        factory = { viewContext ->
                            ImageView(viewContext).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                                setImageResource(wallpaper.resourceId)
                            }
                        },
                        update = { it.setImageResource(wallpaper.resourceId) }
                    )
                    Text(
                        text = wallpaper.title,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        item { Spacer(Modifier.size(20.dp, 1.dp)) }
    }
}
