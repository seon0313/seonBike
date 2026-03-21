package com.seon06.seonbike

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.roundToInt

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    defaultZoom: Double = 15.0,
    zoomControlAlignment: Alignment = Alignment.CenterEnd
) {
    val mapView: MutableState<MapView?> = remember { mutableStateOf(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    setMultiTouchControls(true)
                    @Suppress("DEPRECATION")
                    setBuiltInZoomControls(false)
                    controller.setZoom(defaultZoom)
                    
                    val size = (22 * context.resources.displayMetrics.density).toInt()
                    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    
                    paint.color = android.graphics.Color.WHITE
                    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
                    
                    paint.color = android.graphics.Color.parseColor("#4285F4")
                    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)

                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this).apply {
                        enableMyLocation()
                        enableFollowLocation()
                        setDrawAccuracyEnabled(false)
                        setPersonIcon(bitmap)
                        setDirectionIcon(bitmap)
                        setPersonAnchor(0.5f, 0.5f)
                        setDirectionAnchor(0.5f, 0.5f)
                        runOnFirstFix {
                            post { controller.animateTo(myLocation) }
                        }
                    }
                    overlays.add(locationOverlay)
                    mapView.value = this
                }
            }
        )

        ZoomPillControl(
            mapView = mapView.value,
            modifier = Modifier
                .align(zoomControlAlignment)
                .padding(16.dp)
        )
    }
}

@Composable
fun ZoomPillControl(
    mapView: MapView?,
    modifier: Modifier = Modifier
) {
    var isSliderMode by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var lastHapticTick by remember { mutableIntStateOf(0) }
    
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val tickStepPx = with(density) { 10.dp.toPx() } 

    val pillHeight by animateDpAsState(
        targetValue = if (isSliderMode) 240.dp else 110.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "PillHeight"
    )
    
    val pillWidth by animateDpAsState(
        targetValue = if (isSliderMode) 56.dp else 44.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "PillWidth"
    )

    val contentScale by animateFloatAsState(
        targetValue = if (isSliderMode) 1.05f else 1.0f,
        label = "ContentScale"
    )

    Surface(
        modifier = modifier
            .size(width = pillWidth, height = pillHeight)
            .scale(contentScale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        if (offset.y < size.height / 2) {
                            mapView?.controller?.zoomIn()
                        } else {
                            mapView?.controller?.zoomOut()
                        }
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        isSliderMode = true 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragEnd = { isSliderMode = false; dragOffset = 0f },
                    onDragCancel = { isSliderMode = false; dragOffset = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount.y
                        
                        val currentTick = (dragOffset / tickStepPx).roundToInt()
                        if (currentTick != lastHapticTick) {
                            if (currentTick % 5 == 0) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            lastHapticTick = currentTick
                        }

                        mapView?.let {
                            val sensitivity = 0.008 
                            it.controller.setZoom(it.zoomLevelDouble + (dragAmount.y * -sensitivity))
                        }
                    }
                )
            },
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 12.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isSliderMode) {
                val indicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(vertical = 30.dp)) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2f
                    val centerH = width / 2f
                    
                    val centerTickIndex = -(dragOffset / tickStepPx).roundToInt()
                    
                    for (k in (centerTickIndex - 15)..(centerTickIndex + 15)) {
                        val yPos = centerY + (k * tickStepPx) + dragOffset
                        
                        if (yPos in 0f..height) {
                            val isMajor = k % 5 == 0
                            val lineW = if (isMajor) 18.dp.toPx() else 10.dp.toPx()
                            
                            val distanceFromCenter = kotlin.math.abs(yPos - centerY)
                            val alpha = (1f - (distanceFromCenter / (height / 2f))).coerceIn(0f, 1f)
                            
                            drawLine(
                                color = indicatorColor.copy(alpha = alpha * 0.5f),
                                start = Offset(centerH - lineW / 2f, yPos),
                                end = Offset(centerH + lineW / 2f, yPos),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onSurface, 
                        modifier = Modifier.size(26.dp)
                    )
                    Box(modifier = Modifier.width(24.dp).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(12.dp)) {
                        drawLine(
                            color = Color.Black,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
            }
        }
    }
}