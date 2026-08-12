package com.manus.orbitlauncher.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.manus.orbitlauncher.data.AmbientBackdrop
import com.manus.orbitlauncher.data.AppTrigger
import com.manus.orbitlauncher.data.BuiltinWallpaper
import com.manus.orbitlauncher.data.CenterAction
import com.manus.orbitlauncher.data.CenterGesture
import com.manus.orbitlauncher.data.CenterMode
import com.manus.orbitlauncher.data.CenterSize
import com.manus.orbitlauncher.data.IconScale
import com.manus.orbitlauncher.data.LaunchableApp
import com.manus.orbitlauncher.data.LauncherPage
import com.manus.orbitlauncher.data.RingMode
import com.manus.orbitlauncher.data.RotationSpeed
import com.manus.orbitlauncher.data.VoiceUiState
import com.manus.orbitlauncher.widget.OrbitWidgetHost
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val DOUBLE_TAP_WINDOW_MS = 240L
private const val TRIPLE_TAP_WINDOW_MS = 480L

@Composable
fun OrbitHomeScreen(
    pages: List<LauncherPage>,
    appsByPage: List<List<LaunchableApp>>,
    allApps: List<LaunchableApp>,
    centerMode: CenterMode,
    centerSize: CenterSize,
    centerActions: Map<CenterGesture, CenterAction>,
    appTrigger: AppTrigger,
    rotationSpeed: RotationSpeed,
    iconScale: IconScale,
    labelsVisible: Boolean,
    hapticsEnabled: Boolean,
    ambientBackdrop: AmbientBackdrop,
    builtinWallpaper: BuiltinWallpaper?,
    wallpaperUri: String?,
    widgetId: Int,
    tileWidgetIds: List<Int>,
    widgetHost: OrbitWidgetHost,
    voiceState: VoiceUiState,
    searchRequestToken: Int,
    onLaunchApp: (LaunchableApp) -> Unit,
    onStartVoice: () -> Unit,
    onOpenSearch: () -> Unit,
    onAiAssistant: () -> Unit,
    onCenterAction: (CenterAction) -> Unit,
    onSettings: () -> Unit
) {
    val context = LocalContext.current
    val safePages = pages.ifEmpty { listOf(LauncherPage("fallback", "All apps", RingMode.ALL_APPS)) }
    val pageCount = safePages.size
    var page by remember(safePages.map { it.id }) { mutableIntStateOf(0) }
    var searchVisible by remember { mutableStateOf(false) }
    val activePage = safePages[page.coerceIn(0, pageCount - 1)]
    val activeApps = appsByPage.getOrElse(page.coerceIn(0, appsByPage.lastIndex.coerceAtLeast(0))) { allApps }
    val wallpaperIsDark = remember(builtinWallpaper, wallpaperUri, ambientBackdrop) {
        detectDarkWallpaper(context, builtinWallpaper, wallpaperUri, ambientBackdrop)
    }
    val controlTint = if (wallpaperIsDark) Color.White else Color(0xFF1A1C1E)
    val controlContainer = if (wallpaperIsDark) Color.Black.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.64f)

    LaunchedEffect(pageCount) {
        page = page.coerceIn(0, pageCount - 1)
    }
    LaunchedEffect(searchRequestToken) {
        if (searchRequestToken > 0) searchVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backdropBrush(ambientBackdrop))
    ) {
        when {
            builtinWallpaper != null -> BuiltinWallpaperLayer(builtinWallpaper)
            wallpaperUri != null -> UserWallpaperLayer(wallpaperUri)
        }

        FullCircleOrbit(
            apps = activeApps,
            page = page,
            pageCount = pageCount,
            centerSize = centerSize,
            iconScale = iconScale,
            labelsVisible = labelsVisible,
            appTrigger = appTrigger,
            hapticsEnabled = hapticsEnabled,
            rotationSpeed = rotationSpeed,
            onPageChange = { page = it.coerceIn(0, pageCount - 1) },
            onLaunchApp = onLaunchApp
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = controlContainer,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = activePage.name.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = controlTint
                )
            }
            if (pageCount > 1) {
                Text(
                    text = "${page + 1} / $pageCount",
                    modifier = Modifier.padding(top = 5.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = controlTint
                )
            }
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(CircleShape)
                .background(controlContainer),
            onClick = onSettings
        ) {
            Icon(
                Icons.Outlined.Tune,
                contentDescription = "Launcher settings",
                tint = controlTint
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            val shortestSide = if (maxWidth < maxHeight) maxWidth else maxHeight
            val centralDiameter = shortestSide * 0.52f * centerSize.scale
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CentralSurface(
                    modifier = Modifier.size(centralDiameter),
                    centerMode = centerMode,
                    widgetId = widgetId,
                    widgetHost = widgetHost,
                    voiceState = voiceState,
                    centerActions = centerActions,
                    controlTint = controlTint,
                    controlContainer = controlContainer,
                    onVoice = onStartVoice,
                    onSearch = onOpenSearch,
                    onAiAssistant = onAiAssistant,
                    onCenterAction = onCenterAction
                )
                if (tileWidgetIds.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    WidgetTileStrip(tileWidgetIds, widgetHost)
                }
            }
        }

        if (activeApps.isEmpty()) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 34.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.94f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Text(
                    text = emptyOrbitMessage(activePage.source),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (searchVisible) {
            AppSearchSheet(
                apps = allApps,
                onDismiss = { searchVisible = false },
                onLaunch = {
                    searchVisible = false
                    onLaunchApp(it)
                }
            )
        }
    }
}

@Composable
private fun FullCircleOrbit(
    apps: List<LaunchableApp>,
    page: Int,
    pageCount: Int,
    centerSize: CenterSize,
    iconScale: IconScale,
    labelsVisible: Boolean,
    appTrigger: AppTrigger,
    hapticsEnabled: Boolean,
    rotationSpeed: RotationSpeed,
    onPageChange: (Int) -> Unit,
    onLaunchApp: (LaunchableApp) -> Unit
) {
    var dragDistance by remember { mutableFloatStateOf(0f) }
    val rotationDegrees by animateFloatAsState(
        targetValue = page * -360f,
        animationSpec = tween(rotationSpeed.durationMs),
        label = "full orbit rotation"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(page, pageCount) {
                detectDragGestures(
                    onDragStart = { dragDistance = 0f },
                    onDrag = { change, amount ->
                        change.consume()
                        dragDistance += amount.x - amount.y * 0.22f
                    },
                    onDragEnd = {
                        if (abs(dragDistance) > 38f) onPageChange(page + if (dragDistance < 0f) 1 else -1)
                    }
                )
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val shortest = min(widthPx, heightPx)
        val centre = Offset(widthPx / 2f, heightPx / 2f)
        val radius = shortest * (0.398f + (centerSize.scale - 1f) * 0.075f)
        val podSize = responsivePodSize(
            preferred = iconScale.podSizeDp.toFloat(),
            appCount = apps.size,
            orbitRadiusPx = radius,
            labelsVisible = labelsVisible
        ).dp
        val labelHeight = if (labelsVisible) 18.dp else 0.dp
        val slotHeight = podSize + labelHeight
        apps.forEachIndexed { index, app ->
            val degrees = -90f + index * (360f / maxOf(1, apps.size)) + rotationDegrees
            val radians = degrees * PI / 180.0
            val x = centre.x + radius * cos(radians).toFloat()
            val y = centre.y + radius * sin(radians).toFloat()
            OrbitAppSlot(
                app = app,
                appTrigger = appTrigger,
                hapticsEnabled = hapticsEnabled,
                labelsVisible = labelsVisible,
                podSize = podSize,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (x - podSize.toPx() / 2f).roundToInt(),
                            (y - slotHeight.toPx() / 2f).roundToInt()
                        )
                    }
                    .size(width = podSize, height = slotHeight),
                onLaunch = { onLaunchApp(app) }
            )
        }
    }
}

@Composable
private fun OrbitAppSlot(
    app: LaunchableApp,
    appTrigger: AppTrigger,
    hapticsEnabled: Boolean,
    labelsVisible: Boolean,
    podSize: Dp,
    modifier: Modifier,
    onLaunch: () -> Unit
) {
    val view = LocalView.current
    val launch = {
        if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onLaunch()
    }
    Column(
        modifier = modifier.pointerInput(appTrigger) {
            detectTapGestures(
                onTap = if (appTrigger == AppTrigger.TAP) ({ launch() }) else null,
                onDoubleTap = if (appTrigger == AppTrigger.DOUBLE_TAP) ({ launch() }) else null
            )
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
                        Surface(
            modifier = Modifier.size(podSize).shadow(if (podSize >= 42.dp) 6.dp else 3.dp, CircleShape, clip = false),

            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
            tonalElevation = 3.dp
        ) {
            AppIcon(app.icon, Modifier.padding((podSize.value * 0.16f).dp))
        }
        if (labelsVisible) {
            Text(
                text = app.label,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = max(8f, min(12f, podSize.value * 0.22f)).sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CentralSurface(
    modifier: Modifier,
    centerMode: CenterMode,
    widgetId: Int,
    widgetHost: OrbitWidgetHost,
    voiceState: VoiceUiState,
    centerActions: Map<CenterGesture, CenterAction>,
    controlTint: Color,
    controlContainer: Color,
    onVoice: () -> Unit,
    onSearch: () -> Unit,
    onAiAssistant: () -> Unit,
    onCenterAction: (CenterAction) -> Unit
) {
    val scope = rememberCoroutineScope()
    var delayedDoubleAction by remember { mutableStateOf<Job?>(null) }
    var lastDoubleTapAt by remember { mutableLongStateOf(0L) }
    fun perform(gesture: CenterGesture) {
        onCenterAction(centerActions[gesture] ?: CenterAction.NONE)
    }

    Surface(
        modifier = modifier
            .shadow(20.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .pointerInput(centerActions) {
                detectTapGestures(
                    onTap = {
                        val now = System.currentTimeMillis()
                        if (now - lastDoubleTapAt < TRIPLE_TAP_WINDOW_MS) {
                            delayedDoubleAction?.cancel()
                            lastDoubleTapAt = 0L
                            perform(CenterGesture.TRIPLE_TAP)
                        } else {
                            delayedDoubleAction = scope.launch {
                                delay(DOUBLE_TAP_WINDOW_MS)
                                perform(CenterGesture.TAP)
                            }
                        }
                    },
                    onDoubleTap = {
                        delayedDoubleAction?.cancel()
                        lastDoubleTapAt = System.currentTimeMillis()
                        perform(CenterGesture.DOUBLE_TAP)
                    },
                    onLongPress = { perform(CenterGesture.LONG_PRESS) }
                )
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
        tonalElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            val time = SimpleDateFormat("H:mm", Locale.getDefault()).format(Date())
            when (centerMode) {
                CenterMode.CLOCK, CenterMode.CLOCK_AND_DATE -> {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 72.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                CenterMode.APP_WIDGET -> {
                    if (widgetId >= 0) {
                        val view = remember(widgetId) { widgetHost.viewFor(widgetId) }
                        if (view != null) {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { view }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrbitControlButton(
                    onClick = onVoice,
                    contentDescription = "Voice launch",
                    tint = controlTint,
                    container = controlContainer
                ) {
                    Icon(Icons.Outlined.Mic, contentDescription = null, tint = controlTint)
                }
                OrbitControlButton(
                    onClick = onAiAssistant,
                    contentDescription = "Ask Orbit by voice",
                    tint = controlTint,
                    container = controlContainer
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = controlTint)
                }
                OrbitControlButton(
                    onClick = onSearch,
                    contentDescription = "App search",
                    tint = controlTint,
                    container = controlContainer
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = controlTint)
                }
            }

            if (voiceState != VoiceUiState.Idle) {
                VoiceOverlay(voiceState)
            }
        }
    }
}

@Composable
private fun OrbitControlButton(
    onClick: () -> Unit,
    contentDescription: String,
    tint: Color,
    container: Color,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = container,
        contentColor = tint,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun VoiceOverlay(state: VoiceUiState) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (icon, text) = when (state) {
                is VoiceUiState.Listening -> Icons.Outlined.Mic to "Listening..."
                is VoiceUiState.Heard -> Icons.Outlined.Mic to state.phrase
                is VoiceUiState.Failed -> Icons.Outlined.Mic to state.message
                is VoiceUiState.RequestingPermission -> Icons.Outlined.Mic to "Requesting microphone..."
                is VoiceUiState.Idle -> Icons.Outlined.Mic to ""
            }
            Icon(icon, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(text, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun WidgetTileStrip(ids: List<Int>, host: OrbitWidgetHost) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        ids.forEach { id ->
            Surface(
                modifier = Modifier.size(80.dp).padding(4.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 2.dp
            ) {
                val view = remember(id) { host.viewFor(id) }
                if (view != null) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { view }
                    )
                }
            }
        }
    }
}

@Composable
private fun BuiltinWallpaperLayer(wallpaper: BuiltinWallpaper) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageResource(wallpaper.resourceId)
            }
        },
        update = { view -> view.setImageResource(wallpaper.resourceId) }
    )
}

@Composable
private fun UserWallpaperLayer(uri: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(android.net.Uri.parse(uri))
            }
        },
        update = { view -> view.setImageURI(android.net.Uri.parse(uri)) }
    )
}

@Composable
private fun AppIcon(drawable: Drawable?, modifier: Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                setImageDrawable(drawable)
            }
        },
        update = { view -> view.setImageDrawable(drawable) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSearchSheet(
    apps: List<LaunchableApp>,
    onDismiss: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit
) {
    var query by mutableStateOf("")
    val filtered = remember(query, apps) {
        apps.filter { it.label.contains(query, ignoreCase = true) }.sortedBy { it.label }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().height(480.dp)) {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                shape = RoundedCornerShape(28.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                items(filtered, key = { it.stableId }) { app ->
                    ListItem(
                        modifier = Modifier.clickable { onLaunch(app) },
                        headlineContent = { Text(app.label) },
                        supportingContent = { Text(app.packageName) },
                        leadingContent = { Box(Modifier.size(40.dp)) { AppIcon(app.icon, Modifier.fillMaxSize()) } }
                    )
                }
            }
        }
    }
}

private fun responsivePodSize(
    preferred: Float,
    appCount: Int,
    orbitRadiusPx: Float,
    labelsVisible: Boolean
): Float {
    if (appCount <= 1) return preferred
    val spacingPx = if (labelsVisible) 14f else 10f
    val availablePerApp = ((2f * PI.toFloat() * orbitRadiusPx) / appCount) - spacingPx
    val densityFreeSize = availablePerApp / 3f
    return min(preferred, densityFreeSize).coerceAtLeast(12f)
}

private fun detectDarkWallpaper(
    context: Context,
    builtinWallpaper: BuiltinWallpaper?,
    wallpaperUri: String?,
    ambientBackdrop: AmbientBackdrop
): Boolean {
    val bitmap = runCatching {
        when {
            builtinWallpaper != null -> BitmapFactory.decodeResource(context.resources, builtinWallpaper.resourceId)
            wallpaperUri != null -> context.contentResolver.openInputStream(Uri.parse(wallpaperUri))
                ?.use { BitmapFactory.decodeStream(it) }
            else -> null
        }
    }.getOrNull()
    if (bitmap == null) return ambientBackdrop != AmbientBackdrop.CLAY
    return bitmap.isDarkByAverageLuminance()
}

private fun Bitmap.isDarkByAverageLuminance(): Boolean {
    val sampleX = max(1, width / 40)
    val sampleY = max(1, height / 60)
    var total = 0.0
    var samples = 0
    for (x in 0 until width step sampleX) {
        for (y in 0 until height step sampleY) {
            val pixel = getPixel(x, y)
            val red = ((pixel shr 16) and 0xFF) / 255.0
            val green = ((pixel shr 8) and 0xFF) / 255.0
            val blue = (pixel and 0xFF) / 255.0
            total += 0.2126 * red + 0.7152 * green + 0.0722 * blue
            samples += 1
        }
    }
    return samples == 0 || total / samples < 0.48
}

private fun backdropBrush(backdrop: AmbientBackdrop): Brush = when (backdrop) {
    AmbientBackdrop.ORBIT -> Brush.verticalGradient(listOf(Color(0xFF1A1C1E), Color(0xFF000000)))
    AmbientBackdrop.AURORA -> Brush.verticalGradient(listOf(Color(0xFF2C3E50), Color(0xFF000000)))
    AmbientBackdrop.DUSK -> Brush.verticalGradient(listOf(Color(0xFF141E30), Color(0xFF243B55)))
    AmbientBackdrop.TIDAL -> Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43)))
    AmbientBackdrop.CLAY -> Brush.verticalGradient(listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF)))
}

private fun emptyOrbitMessage(mode: RingMode): String = when (mode) {
    RingMode.FAVORITES -> "Pin your favourite apps in settings."
    RingMode.MOST_USED -> "Usage data will show your top apps here."
    RingMode.RECENT -> "Recently opened apps will appear here."
    RingMode.ALL_APPS -> "No apps found on this device."
}
