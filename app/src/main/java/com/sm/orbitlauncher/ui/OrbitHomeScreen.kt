package com.sm.orbitlauncher.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
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
import com.sm.orbitlauncher.data.*
import com.sm.orbitlauncher.widget.OrbitWidgetHost
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

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
    val controlContainer = if (wallpaperIsDark) Color.Black.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.68f)

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

        // Page Indicator
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = controlContainer,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, controlTint.copy(alpha = 0.1f))
            ) {
                Text(
                    text = activePage.name.uppercase(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = controlTint,
                    letterSpacing = 1.sp
                )
            }
            if (pageCount > 1) {
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(pageCount) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (i == page) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (i == page) controlTint else controlTint.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }

        // Settings Button
        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 42.dp, end = 20.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(controlContainer),
            onClick = onSettings
        ) {
            Icon(
                Icons.Outlined.Tune,
                contentDescription = "Launcher settings",
                tint = controlTint,
                modifier = Modifier.size(24.dp)
            )
        }

        // Central Area
        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            val shortestSide = if (maxWidth < maxHeight) maxWidth else maxHeight
            val centralDiameter = shortestSide * 0.54f * centerSize.scale
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
                    Spacer(Modifier.height(24.dp))
                    WidgetTileStrip(tileWidgetIds, widgetHost)
                }
            }
        }

        if (activeApps.isEmpty()) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp),
                shape = RoundedCornerShape(24.dp),
                color = controlContainer,
                contentColor = controlTint
            ) {
                Text(
                    text = emptyOrbitMessage(activePage.source),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
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
                        if (abs(dragDistance) > 40f) onPageChange(page + if (dragDistance < 0f) 1 else -1)
                    }
                )
            }
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val shortest = min(widthPx, heightPx)
        val centre = Offset(widthPx / 2f, heightPx / 2f)
        val radius = shortest * (0.41f + (centerSize.scale - 1f) * 0.08f)
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
            modifier = Modifier.size(podSize),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
            tonalElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(podSize * 0.18f)) {
                if (app.icon != null) {
                    AndroidView(factory = { context ->
                        android.widget.ImageView(context).apply {
                            setImageDrawable(app.icon)
                            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                        }
                    }, modifier = Modifier.fillMaxSize())
                }
            }
        }
        if (labelsVisible) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp).width(podSize * 1.2f)
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
    val view = LocalView.current

    fun perform(gesture: CenterGesture) {
        onCenterAction(centerActions[gesture] ?: CenterAction.NONE)
    }

    Surface(
        modifier = modifier
            .shadow(24.dp, CircleShape, clip = false)
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
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
        tonalElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (voiceState) {
                is VoiceUiState.Listening -> {
                    VoiceRipple()
                    Icon(Icons.Outlined.Mic, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                }
                is VoiceUiState.Heard -> {
                    Text(
                        text = voiceState.phrase,
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                is VoiceUiState.Failed -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = voiceState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    val time = SimpleDateFormat("H:mm", Locale.getDefault()).format(Date())
                    when (centerMode) {
                        CenterMode.CLOCK, CenterMode.CLOCK_AND_DATE -> {
                            Text(
                                text = time,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 78.sp,
                                    letterSpacing = (-2).sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        CenterMode.APP_WIDGET -> {
                            if (widgetId >= 0) {
                                val widgetView = remember(widgetId) { widgetHost.viewFor(widgetId) }
                                if (widgetView != null) {
                                    AndroidView(
                                        modifier = Modifier.fillMaxSize(),
                                        factory = { widgetView }
                                    )
                                }
                            } else {
                                Text("Long press to add widget", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            // Refined Quick Action Row (Floating Glass Style)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                shape = CircleShape,
                color = controlContainer.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, controlTint.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OrbitControlButton(Icons.Outlined.Mic, "Voice launch", onVoice, controlTint)
                    OrbitControlButton(Icons.Outlined.AutoAwesome, "AI Assistant", onAiAssistant, controlTint)
                    OrbitControlButton(Icons.Outlined.Search, "App search", onSearch, controlTint)
                }
            }
        }
    }
}

@Composable
private fun OrbitControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp)
    ) {
        Icon(icon, contentDescription, modifier = Modifier.size(22.dp), tint = tint.copy(alpha = 0.9f))
    }
}

@Composable
private fun VoiceRipple() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "voice ripple")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(1200),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "ripple scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(1200),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "ripple alpha"
    )
    Box(
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
    )
}

@Composable
private fun BuiltinWallpaperLayer(wallpaper: BuiltinWallpaper) {
    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = wallpaper.resourceId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)))
    }
}

@Composable
private fun UserWallpaperLayer(uri: String) {
    val context = LocalContext.current
    val image = remember(uri) {
        decodeBitmapSampled(context, Uri.parse(uri), 1440, 2560)?.asImageBitmap()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (image != null) androidx.compose.foundation.Image(
            bitmap = image,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)))
    }
}

@Composable
private fun WidgetTileStrip(ids: List<Int>, host: OrbitWidgetHost) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ids.forEach { id ->
            Surface(
                modifier = Modifier.size(72.dp).padding(4.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                tonalElevation = 4.dp
            ) {
                val view = remember(id) { host.viewFor(id) }
                if (view != null) {
                    AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSearchSheet(
    apps: List<LaunchableApp>,
    onDismiss: () -> Unit,
    onLaunch: (LaunchableApp) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, apps) {
        if (query.isBlank()) emptyList()
        else apps.filter { it.label.contains(query, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(16.dp)) {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search apps...") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(filtered) { app ->
                    ListItem(
                        headlineContent = { Text(app.label) },
                        leadingContent = {
                            if (app.icon != null) {
                                AndroidView(factory = { context ->
                                    android.widget.ImageView(context).apply {
                                        setImageDrawable(app.icon)
                                    }
                                }, modifier = Modifier.size(40.dp))
                            }
                        },
                        modifier = Modifier.clickable { onLaunch(app) }
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
    val spacingPx = if (labelsVisible) 16f else 10f
    val circumference = 2f * PI.toFloat() * orbitRadiusPx
    val maxPodSize = (circumference / appCount) - spacingPx
    return min(preferred, maxPodSize).coerceAtLeast(32f)
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
            wallpaperUri != null -> decodeBitmapSampled(context, Uri.parse(wallpaperUri), 256, 256)
            else -> null
        }
    }.getOrNull()
    if (bitmap == null) return ambientBackdrop != AmbientBackdrop.CLAY
    return bitmap.isDarkByAverageLuminance()
}

private fun decodeBitmapSampled(context: Context, uri: Uri, targetWidth: Int, targetHeight: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    } ?: return null
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > targetWidth || bounds.outHeight / sampleSize > targetHeight) {
        sampleSize *= 2
    }
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
    }
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

