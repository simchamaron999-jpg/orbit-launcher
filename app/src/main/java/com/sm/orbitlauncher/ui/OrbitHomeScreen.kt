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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateBefore
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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
private const val DENSE_ORBIT_APP_THRESHOLD = 16

/** Semantic, wallpaper-aware surface roles for Orbit's custom home controls. */
private data class OrbitSurfaceStyle(
    val controlContent: Color,
    val controlContainer: Color,
    val podContainer: Color,
    val podContent: Color,
    val labelContainer: Color,
    val labelContent: Color
)

@Composable
fun OrbitHomeScreen(
    pages: List<LauncherPage>,
    appsByPage: List<List<LaunchableApp>>,
    allApps: List<LaunchableApp>,
    centerMode: CenterMode,
    homeLayoutMode: HomeLayoutMode,
    homeDensity: Float,
    centerActions: Map<CenterGesture, CenterAction>,
    appTrigger: AppTrigger,
    rotationSpeed: RotationSpeed,
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
    val colors = MaterialTheme.colorScheme
    val orbitStyle = if (wallpaperIsDark) {
        OrbitSurfaceStyle(
            controlContent = colors.onSurface,
            controlContainer = colors.surfaceContainerHigh.copy(alpha = 0.90f),
            podContainer = colors.surfaceContainerHighest.copy(alpha = 0.94f),
            podContent = colors.onSurface,
            labelContainer = colors.surfaceContainer.copy(alpha = 0.94f),
            labelContent = colors.onSurface
        )
    } else {
        OrbitSurfaceStyle(
            controlContent = colors.onSurface,
            controlContainer = colors.surfaceContainer.copy(alpha = 0.90f),
            podContainer = colors.surfaceContainerHigh.copy(alpha = 0.94f),
            podContent = colors.onSurface,
            labelContainer = colors.surfaceContainerLowest.copy(alpha = 0.94f),
            labelContent = colors.onSurface
        )
    }
    val controlTint = orbitStyle.controlContent
    val controlContainer = orbitStyle.controlContainer
    val effectiveDensity = if (homeLayoutMode == HomeLayoutMode.CUSTOM) homeDensity.coerceIn(-0.10f, 0.10f) else 0f

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
            homeDensity = effectiveDensity,
            labelsVisible = labelsVisible,
            appTrigger = appTrigger,
            hapticsEnabled = hapticsEnabled,
            rotationSpeed = rotationSpeed,
            orbitStyle = orbitStyle,
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
                .size(46.dp)
                .clip(CircleShape)
                .background(controlContainer),
            onClick = onSettings
        ) {
            Icon(
                Icons.Outlined.Settings,
                contentDescription = "Customize Orbit",
                tint = controlTint,
                modifier = Modifier.size(24.dp)
            )
        }

        // Four-icon navigation dock: intentionally below the orbit, above the Android navigation area.
        OrbitShortcutDock(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 42.dp),
            onPreviousPage = { page = (page - 1).coerceAtLeast(0) },
            onAllApps = onOpenSearch,
            onFavorites = {
                safePages.indexOfFirst { it.source == RingMode.FAVORITES }
                    .takeIf { it >= 0 }
                    ?.let { page = it }
            },
            onNextPage = { page = (page + 1).coerceAtMost(pageCount - 1) },
            tint = controlTint,
            container = controlContainer
        )

        // Central Area
        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            val safeWidth = (maxWidth - 40.dp).coerceAtLeast(240.dp)
            val safeHeight = (maxHeight - 156.dp).coerceAtLeast(240.dp)
            val compositionSide = if (safeWidth < safeHeight) safeWidth else safeHeight
            val centralRatio = (0.46f + effectiveDensity * 0.10f).coerceIn(0.42f, 0.50f)
            val centralDiameter = compositionSide * centralRatio
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
    homeDensity: Float,
    labelsVisible: Boolean,
    appTrigger: AppTrigger,
    hapticsEnabled: Boolean,
    rotationSpeed: RotationSpeed,
    orbitStyle: OrbitSurfaceStyle,
    onPageChange: (Int) -> Unit,
    onLaunchApp: (LaunchableApp) -> Unit
) {
    var dragDistance by remember { mutableFloatStateOf(0f) }
    var focusedAppId by remember(page) { mutableStateOf<String?>(null) }
    val rotationDegrees by animateFloatAsState(
        targetValue = page * -360f,
        animationSpec = tween(rotationSpeed.durationMs),
        label = "full orbit rotation"
    )

    val density = LocalDensity.current
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
        val reservedVerticalPx = with(density) { 156.dp.toPx() }
        val horizontalGutterPx = with(density) { 40.dp.toPx() }
        val usableHeightPx = (heightPx - reservedVerticalPx).coerceAtLeast(widthPx * 0.52f)
        val compositionSidePx = min(widthPx - horizontalGutterPx, usableHeightPx)
        val centre = Offset(widthPx / 2f, heightPx / 2f)
        val radiusRatio = (0.405f + homeDensity.coerceIn(-0.10f, 0.10f) * 0.055f).coerceIn(0.38f, 0.43f)
        val radius = compositionSidePx * radiusRatio
        val appCount = maxOf(1, apps.size)
        val denseOrbit = appCount > DENSE_ORBIT_APP_THRESHOLD
        val podSize = responsivePodSize(
            preferred = 52f + homeDensity.coerceIn(-0.10f, 0.10f) * 120f,
            appCount = appCount,
            orbitRadiusPx = radius,
            labelsVisible = labelsVisible
        ).dp
        // Dense rings reveal one readable label on hold rather than shrinking every label into overlap.
        val dynamicLabelFont = if (denseOrbit) 11.sp else 10.sp
        val labelHeight = if (labelsVisible) (if (denseOrbit) 20.dp else 18.dp) else 0.dp
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
                showLabel = labelsVisible && (!denseOrbit || focusedAppId == app.stableId),
                podSize = podSize,
                labelFontSize = dynamicLabelFont,
                orbitStyle = orbitStyle,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (x - podSize.toPx() / 2f).roundToInt(),
                            (y - slotHeight.toPx() / 2f).roundToInt()
                        )
                    }
                    .size(width = podSize, height = slotHeight),
                onFocus = { focusedAppId = app.stableId },
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
    showLabel: Boolean,
    podSize: Dp,
    labelFontSize: androidx.compose.ui.unit.TextUnit,
    orbitStyle: OrbitSurfaceStyle,
    modifier: Modifier,
    onFocus: () -> Unit,
    onLaunch: () -> Unit
) {
    val view = LocalView.current
    val launch = {
        if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onLaunch()
    }
    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                role = androidx.compose.ui.semantics.Role.Button
                contentDescription = "Open ${app.label}"
            }
            .pointerInput(appTrigger) {
                detectTapGestures(
                    onTap = if (appTrigger == AppTrigger.TAP) ({ launch() }) else null,
                    onDoubleTap = if (appTrigger == AppTrigger.DOUBLE_TAP) ({ launch() }) else null,
                    onLongPress = { onFocus() }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(podSize),
            shape = CircleShape,
            color = orbitStyle.podContainer,
            shadowElevation = 3.dp,
            tonalElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, orbitStyle.podContent.copy(alpha = 0.12f))
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(podSize * 0.22f)) {
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
        if (showLabel) {
            Surface(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .widthIn(max = podSize * 1.25f),
                shape = RoundedCornerShape(8.dp),
                color = orbitStyle.labelContainer,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, orbitStyle.labelContent.copy(alpha = 0.12f))
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = labelFontSize, fontWeight = FontWeight.SemiBold),
                    color = orbitStyle.labelContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
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
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f),
        shadowElevation = 18.dp,
        tonalElevation = 10.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
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
                                    fontSize = 68.sp,
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

            // The AI control is deliberately above the clock; microphone and search stay below it.
            OrbitControlButton(
                icon = Icons.Outlined.AutoAwesome,
                contentDescription = "Ask Orbit with AI",
                onClick = onAiAssistant,
                tint = controlTint,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp)
                    .shadow(2.dp, CircleShape)
                    .background(controlContainer, CircleShape)
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
                shape = CircleShape,
                color = controlContainer,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, controlTint.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OrbitControlButton(Icons.Outlined.Mic, "Voice launch", onVoice, controlTint)
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
    tint: Color,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp)
    ) {
        Icon(icon, contentDescription, modifier = Modifier.size(21.dp), tint = tint.copy(alpha = 0.9f))
    }
}

@Composable
private fun OrbitShortcutDock(
    modifier: Modifier = Modifier,
    onPreviousPage: () -> Unit,
    onAllApps: () -> Unit,
    onFavorites: () -> Unit,
    onNextPage: () -> Unit,
    tint: Color,
    container: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = container.copy(alpha = 0.82f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, tint.copy(alpha = 0.16f)),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockButton(Icons.AutoMirrored.Outlined.NavigateBefore, "Previous page", onPreviousPage, tint)
            DockButton(Icons.Outlined.GridView, "All apps", onAllApps, tint)
            DockButton(Icons.Outlined.FavoriteBorder, "Favorites", onFavorites, tint)
            DockButton(Icons.AutoMirrored.Outlined.NavigateNext, "Next page", onNextPage, tint)
        }
    }
}

@Composable
private fun DockButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color
) {
    IconButton(onClick = onClick, modifier = Modifier.size(42.dp)) {
        Icon(icon, description, tint = tint, modifier = Modifier.size(22.dp))
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
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.24f)))
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
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.24f)))
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
    val sortedApps = remember(apps) { apps.sortedBy { it.label.lowercase(Locale.getDefault()) } }
    val filtered = remember(query, sortedApps) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) sortedApps
        else sortedApps.filter { it.label.contains(normalizedQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "App Library",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Search your apps or browse the full library",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search your apps") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search apps") },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Text(
                text = if (query.isBlank()) "${sortedApps.size} installed apps" else "${filtered.size} matching apps",
                modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (filtered.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No apps match \"${query.trim()}\"", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Try a different app name.",
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 84.dp),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 28.dp)
                ) {
                    items(filtered, key = { it.stableId }) { app ->
                        AppLibraryItem(app = app, onLaunch = { onLaunch(app) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AppLibraryItem(app: LaunchableApp, onLaunch: () -> Unit) {
    Surface(
        onClick = onLaunch,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                if (app.icon != null) {
                    AndroidView(
                        factory = { context ->
                            android.widget.ImageView(context).apply {
                                setImageDrawable(app.icon)
                                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                            }
                        },
                        modifier = Modifier.padding(8.dp).fillMaxSize()
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
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

