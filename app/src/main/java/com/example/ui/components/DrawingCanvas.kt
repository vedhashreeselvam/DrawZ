package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import com.example.drawing.engine.DrawingRenderer
import com.example.drawing.model.DrawingItem
import kotlin.math.max
import kotlin.math.min

@Composable
fun DrawingCanvas(
    canvasWidth: Int,
    canvasHeight: Int,
    backgroundColor: Int,
    strokes: List<DrawingItem>,
    activeItem: DrawingItem?,
    zoom: Float,
    panX: Float,
    panY: Float,
    isPanZoomMode: Boolean,
    onStartStroke: (Float, Float, Float) -> Unit,
    onMoveStroke: (Float, Float, Float) -> Unit,
    onEndStroke: () -> Unit,
    onTransform: (Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val canvasBgColor = Color(backgroundColor)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        val viewW = constraints.maxWidth.toFloat()
        val viewH = constraints.maxHeight.toFloat()

        // Fit canvas nicely in viewport
        val fitScale = min(viewW / canvasWidth, viewH / canvasHeight) * 0.94f
        val baseLeft = (viewW - canvasWidth * fitScale) / 2f
        val baseTop = (viewH - canvasHeight * fitScale) / 2f

        val effectiveScale = fitScale * zoom
        val totalLeft = baseLeft + panX
        val totalTop = baseTop + panY

        val gestureModifier = if (isPanZoomMode) {
            Modifier.pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    onTransform(pan.x, pan.y, gestureZoom)
                }
            }
        } else {
            Modifier.pointerInput(canvasWidth, canvasHeight, effectiveScale, totalLeft, totalTop) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val logicalX = (offset.x - totalLeft) / effectiveScale
                        val logicalY = (offset.y - totalTop) / effectiveScale
                        onStartStroke(logicalX, logicalY, 1.0f)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val logicalX = (change.position.x - totalLeft) / effectiveScale
                        val logicalY = (change.position.y - totalTop) / effectiveScale
                        val pressure = change.pressure.takeIf { it > 0f } ?: 1.0f
                        onMoveStroke(logicalX, logicalY, pressure)
                    },
                    onDragEnd = {
                        onEndStroke()
                    },
                    onDragCancel = {
                        onEndStroke()
                    }
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(gestureModifier)
        ) {
            // Apply pan and zoom transform to canvas coordinates
            withTransform({
                translate(left = totalLeft, top = totalTop)
                scale(scaleX = effectiveScale, scaleY = effectiveScale, pivot = Offset.Zero)
            }) {
                // Drop shadow behind canvas sheet
                drawRect(
                    color = Color.Black.copy(alpha = 0.12f),
                    topLeft = Offset(8f, 8f),
                    size = Size(canvasWidth.toFloat(), canvasHeight.toFloat())
                )

                // Canvas Background
                drawRect(
                    color = canvasBgColor,
                    topLeft = Offset.Zero,
                    size = Size(canvasWidth.toFloat(), canvasHeight.toFloat())
                )

                // Clip drawing to canvas dimensions
                clipRect(left = 0f, top = 0f, right = canvasWidth.toFloat(), bottom = canvasHeight.toFloat()) {
                    // Render completed strokes
                    for (item in strokes) {
                        DrawingRenderer.renderItemToDrawScope(this, item, canvasBgColor)
                    }

                    // Render active preview stroke
                    if (activeItem != null) {
                        DrawingRenderer.renderItemToDrawScope(this, activeItem, canvasBgColor)
                    }
                }

                // Canvas Border
                drawRect(
                    color = Color.LightGray.copy(alpha = 0.6f),
                    topLeft = Offset.Zero,
                    size = Size(canvasWidth.toFloat(), canvasHeight.toFloat()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f / effectiveScale)
                )
            }
        }
    }
}
