package com.manus.orbitlauncher.ui

import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.manus.orbitlauncher.data.AiProvider
import com.manus.orbitlauncher.data.AmbientBackdrop
import com.manus.orbitlauncher.data.AppearanceMode
import com.manus.orbitlauncher.data.AppTrigger
import com.manus.orbitlauncher.data.BuiltinWallpaper
import com.manus.orbitlauncher.data.CenterAction
import com.manus.orbitlauncher.data.CenterGesture
import com.manus.orbitlauncher.data.CenterMode
import com.manus.orbitlauncher.data.CenterSize
import com.manus.orbitlauncher.data.ClockStyle
import com.manus.orbitlauncher.data.IconScale
import com.manus.orbitlauncher.data.LaunchableApp
import com.manus.orbitlauncher.data.LauncherPage
import com.manus.orbitlauncher.data.RingMode
import com.manus.orbitlauncher.data.RotationSpeed
import com.manus.orbitlauncher.data.WallpaperCategory

private enum class SettingsCategory(val title: String) {
    QUICK("Quick"),
    AI("AI Engine"),
    GESTURES("Gestures"),
    APPS("Apps"),
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
    centerSize: CenterSize,
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
    tileWidgets: List<com.manus.orbitlauncher.data.WidgetTile>,
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
    onPickWidget: () -> Unit,
    onRemoveWidget: () -> Unit,
    onPickTileWidget: () -> Unit,
    onRemoveTileWidget: (Int) -> Unit,
    onUpdateTileWidgets: (List<com.manus.orbitlauncher.data.WidgetTile>) -> Unit,
    onToggleFavorite: (LaunchableApp) -> Unit,
    onAiProvider: (AiProvider) -> Unit,
    onAiApiKey: (String) -> Unit,
    onAiEndpoint: (String?) -> Unit,
    onClockStyle: (ClockStyle) -> Unit,
    onRequestUsageAccess: () -> Unit,
    onRequestDefaultHome: () -> Unit
) {
    var currentCategory by remember { mutableStateOf(SettingsCategory.QUICK) }
    
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
                            Row(Modifier.padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PresetButton("Minimalist", onClick = {
                                    onIconScale(IconScale.EXPRESSIVE)
                                    onCenterSize(CenterSize.LARGE)
                                })
                                PresetButton("Balanced", onClick = {
                                    onIconScale(IconScale.COMFORTABLE)
                                    onCenterSize(CenterSize.BALANCED)
                                })
                                PresetButton("Dense", onClick = {
                                    onIconScale(IconScale.COMPACT)
                                    onCenterSize(CenterSize.SMALL)
                                })
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
                                headlineContent = { Text("Circle size") },
                                trailingContent = { Text(centerSize.title, color = MaterialTheme.colorScheme.primary) }
                            )
                        }
                        item { ChoiceChips(CenterSize.entries.toList(), centerSize, { it.title }, onCenterSize) }
                        item { SettingSwitch("App names", "Hide for a minimal icon-only orbit", labelsVisible, onLabelsVisible) }
                    }

                    SettingsCategory.AI -> {
                        item { SectionTitle("BYOK AI Engine") }
                        item {
                            Text(
                                "Enter your own API key to unlock natural-language search, smart routines, and summaries. Keys are stored securely on-device.",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.AutoAwesome, null) },
                                headlineContent = { Text("Provider") },
                                trailingContent = { Text(aiProvider.title, color = MaterialTheme.colorScheme.primary) }
                            )
                        }
                        item { ChoiceChips(AiProvider.entries.toList(), aiProvider, { it.title }, onAiProvider) }
                        item {
                            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                                OutlinedTextField(
                                    value = aiApiKey,
                                    onValueChange = onAiApiKey,
                                    label = { Text("API Key") },
                                    modifier = Modifier.fillMaxWidth(),
                                    visualTransformation = PasswordVisualTransformation()
                                )
                                if (aiProvider == AiProvider.CUSTOM) {
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = aiEndpoint ?: "",
                                        onValueChange = onAiEndpoint,
                                        label = { Text("Custom Endpoint URL") },
                                        modifier = Modifier.fillMaxWidth()
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

                    SettingsCategory.APPS -> {
                        item { SectionTitle("Home pages") }
                        item {
                            Text(
                                "Each page has its own app source. Swipe left or right on Home to move between pages. Every source can show all of its matching apps; Orbit automatically reduces icon size as a page becomes denser.",
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
                                leadingContent = { Icon(Icons.Outlined.Apps, null) },
                                headlineContent = { Text("Preferred icon size") },
                                supportingContent = { Text("Automatically reduced on high-density pages") },
                                trailingContent = { Text(iconScale.title, color = MaterialTheme.colorScheme.primary) }
                            )
                        }
                        item { ChoiceChips(IconScale.entries.toList(), iconScale, { it.title }, onIconScale) }
                        item { SettingSwitch("Haptic feedback", "Vibrate on orbit interactions", hapticsEnabled, onHapticsEnabled) }
                    }

                    SettingsCategory.WALLPAPERS -> {
                        item { SectionTitle("Gallery") }
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
                        item {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Home, null) },
                                headlineContent = { Text("Set as default home") },
                                trailingContent = { Button(onClick = onRequestDefaultHome) { Text("Set") } }
                            )
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
                                headlineContent = { Text("Email") },
                                supportingContent = { Text("simchamaronapp@gmail.app") }
                            )
                        }
                        item {
                            ListItem(
                                headlineContent = { Text("Version") },
                                supportingContent = { Text("0.7") }
                            )
                        }
                        item {
                            ListItem(
                                headlineContent = { Text("Repository") },
                                supportingContent = { Text("https://github.com/simchamaron999-jpg/orbit-launcher") }
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
    LazyRow(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(Modifier.size(16.dp, 1.dp)) }
        items(SettingsCategory.entries.toList()) { cat ->
            FilterChip(selected == cat, { onSelect(cat) }, label = { Text(cat.title) })
        }
        item { Spacer(Modifier.size(16.dp, 1.dp)) }
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
        color = MaterialTheme.colorScheme.primary
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
