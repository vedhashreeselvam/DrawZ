package com.example.drawing.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.drawing.model.BrushStyle
import com.example.drawing.model.DrawingItem
import com.example.drawing.model.PointD
import com.example.drawing.model.ShapeType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object DrawingRenderer {

    /**
     * Builds a smooth Android Path from a sequence of points using quadratic Bezier curves.
     */
    fun createSmoothAndroidPath(points: List<PointD>): AndroidPath {
        val path = AndroidPath()
        if (points.isEmpty()) return path

        if (points.size == 1) {
            val p = points[0]
            path.addCircle(p.x, p.y, 1f, AndroidPath.Direction.CW)
            return path
        }

        path.moveTo(points[0].x, points[0].y)

        if (points.size == 2) {
            path.lineTo(points[1].x, points[1].y)
            return path
        }

        for (i in 1 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val midX = (p0.x + p1.x) / 2f
            val midY = (p0.y + p1.y) / 2f
            path.quadTo(p0.x, p0.y, midX, midY)
        }

        val lastPoint = points.last()
        path.lineTo(lastPoint.x, lastPoint.y)
        return path
    }

    /**
     * Builds Compose Path from PointD list.
     */
    fun createSmoothComposePath(points: List<PointD>): Path {
        return createSmoothAndroidPath(points).asComposePath()
    }

    /**
     * Generates Android Path for geometric shapes.
     */
    fun createShapeAndroidPath(
        shapeType: ShapeType,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float
    ): AndroidPath {
        val path = AndroidPath()
        val left = min(startX, endX)
        val top = min(startY, endY)
        val right = max(startX, endX)
        val bottom = max(startY, endY)
        val width = right - left
        val height = bottom - top

        when (shapeType) {
            ShapeType.LINE -> {
                path.moveTo(startX, startY)
                path.lineTo(endX, endY)
            }
            ShapeType.RECTANGLE -> {
                path.addRect(left, top, right, bottom, AndroidPath.Direction.CW)
            }
            ShapeType.ROUNDED_RECTANGLE -> {
                val cornerRadius = min(width, height) * 0.15f
                path.addRoundRect(
                    RectF(left, top, right, bottom),
                    cornerRadius,
                    cornerRadius,
                    AndroidPath.Direction.CW
                )
            }
            ShapeType.CIRCLE -> {
                val cx = (left + right) / 2f
                val cy = (top + bottom) / 2f
                val radius = max(width, height) / 2f
                path.addCircle(cx, cy, radius, AndroidPath.Direction.CW)
            }
            ShapeType.OVAL -> {
                path.addOval(RectF(left, top, right, bottom), AndroidPath.Direction.CW)
            }
            ShapeType.TRIANGLE -> {
                path.moveTo((left + right) / 2f, top)
                path.lineTo(right, bottom)
                path.lineTo(left, bottom)
                path.close()
            }
            ShapeType.ARROW -> {
                path.moveTo(startX, startY)
                path.lineTo(endX, endY)
                // Draw arrow head
                val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble())
                val headLength = max(24f, min(width, height) * 0.3f)
                val headAngle = PI / 6.0 // 30 degrees

                val x1 = (endX - headLength * cos(angle - headAngle)).toFloat()
                val y1 = (endY - headLength * sin(angle - headAngle)).toFloat()
                val x2 = (endX - headLength * cos(angle + headAngle)).toFloat()
                val y2 = (endY - headLength * sin(angle + headAngle)).toFloat()

                path.moveTo(x1, y1)
                path.lineTo(endX, endY)
                path.lineTo(x2, y2)
            }
            ShapeType.POLYGON -> { // Regular 6-sided hexagon
                val cx = (left + right) / 2f
                val cy = (top + bottom) / 2f
                val radius = min(width, height) / 2f
                val sides = 6
                for (i in 0 until sides) {
                    val a = (i * 2 * PI / sides) - (PI / 2)
                    val px = (cx + radius * cos(a)).toFloat()
                    val py = (cy + radius * sin(a)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
            }
            ShapeType.STAR -> { // 5-point star
                val cx = (left + right) / 2f
                val cy = (top + bottom) / 2f
                val outerRadius = min(width, height) / 2f
                val innerRadius = outerRadius * 0.45f
                val pointsCount = 10
                for (i in 0 until pointsCount) {
                    val r = if (i % 2 == 0) outerRadius else innerRadius
                    val a = (i * PI / 5) - (PI / 2)
                    val px = (cx + r * cos(a)).toFloat()
                    val py = (cy + r * sin(a)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
            }
        }
        return path
    }

    /**
     * Renders a DrawingItem directly into Jetpack Compose DrawScope.
     */
    fun renderItemToDrawScope(
        drawScope: DrawScope,
        item: DrawingItem,
        canvasBgColor: Color
    ) {
        when (item) {
            is DrawingItem.Freehand -> {
                if (item.points.isEmpty()) return
                val composePath = createSmoothComposePath(item.points)
                val baseColor = if (item.isEraser) {
                    canvasBgColor
                } else {
                    Color(item.color).copy(alpha = item.alpha)
                }

                when (item.brushStyle) {
                    BrushStyle.ROUND -> {
                        drawScope.drawPath(
                            path = composePath,
                            color = baseColor,
                            style = Stroke(
                                width = item.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                    BrushStyle.MARKER -> {
                        drawScope.drawPath(
                            path = composePath,
                            color = baseColor.copy(alpha = min(0.6f, item.alpha)),
                            style = Stroke(
                                width = item.strokeWidth * 1.3f,
                                cap = StrokeCap.Square,
                                join = StrokeJoin.Bevel
                            )
                        )
                    }
                    BrushStyle.CALLIGRAPHY -> {
                        // Double pass with slight offset for ribbon / calligraphy effect
                        drawScope.drawPath(
                            path = composePath,
                            color = baseColor,
                            style = Stroke(
                                width = item.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Miter
                            )
                        )
                    }
                    BrushStyle.NEON -> {
                        // Outer vibrant glow
                        drawScope.drawPath(
                            path = composePath,
                            color = baseColor.copy(alpha = 0.25f),
                            style = Stroke(
                                width = item.strokeWidth * 2.2f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                        // Middle glow
                        drawScope.drawPath(
                            path = composePath,
                            color = baseColor.copy(alpha = 0.5f),
                            style = Stroke(
                                width = item.strokeWidth * 1.5f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                        // Bright center core
                        drawScope.drawPath(
                            path = composePath,
                            color = Color.White.copy(alpha = 0.9f),
                            style = Stroke(
                                width = max(2f, item.strokeWidth * 0.4f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                    BrushStyle.SKETCH -> {
                        drawScope.drawPath(
                            path = composePath,
                            color = baseColor.copy(alpha = 0.65f),
                            style = Stroke(
                                width = max(1f, item.strokeWidth * 0.8f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
            is DrawingItem.Shape -> {
                val shapePath = createShapeAndroidPath(
                    item.shapeType,
                    item.startX,
                    item.startY,
                    item.endX,
                    item.endY
                ).asComposePath()

                val strokeColor = Color(item.strokeColor).copy(alpha = item.alpha)

                // Fill if enabled
                if (item.isFilled && item.fillColor != null && item.shapeType != ShapeType.LINE && item.shapeType != ShapeType.ARROW) {
                    val fillColor = Color(item.fillColor).copy(alpha = item.alpha)
                    drawScope.drawPath(
                        path = shapePath,
                        color = fillColor,
                        style = Fill
                    )
                }

                // Stroke outline
                drawScope.drawPath(
                    path = shapePath,
                    color = strokeColor,
                    style = Stroke(
                        width = item.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }

    /**
     * Renders the entire artwork into an Android Bitmap (for export or thumbnail).
     */
    fun renderToBitmap(
        width: Int,
        height: Int,
        backgroundColor: Int,
        items: List<DrawingItem>
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(max(1, width), max(1, height), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        canvas.drawColor(backgroundColor)

        val strokePaint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val fillPaint = Paint().apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.FILL
        }

        for (item in items) {
            when (item) {
                is DrawingItem.Freehand -> {
                    if (item.points.isEmpty()) continue
                    val path = createSmoothAndroidPath(item.points)

                    if (item.isEraser) {
                        strokePaint.color = backgroundColor
                        strokePaint.alpha = 255
                        strokePaint.strokeWidth = item.strokeWidth
                        strokePaint.strokeCap = Paint.Cap.ROUND
                        strokePaint.strokeJoin = Paint.Join.ROUND
                        canvas.drawPath(path, strokePaint)
                    } else {
                        val baseColor = item.color
                        val baseAlpha = (item.alpha * 255).toInt().coerceIn(0, 255)

                        when (item.brushStyle) {
                            BrushStyle.ROUND -> {
                                strokePaint.color = baseColor
                                strokePaint.alpha = baseAlpha
                                strokePaint.strokeWidth = item.strokeWidth
                                strokePaint.strokeCap = Paint.Cap.ROUND
                                strokePaint.strokeJoin = Paint.Join.ROUND
                                canvas.drawPath(path, strokePaint)
                            }
                            BrushStyle.MARKER -> {
                                strokePaint.color = baseColor
                                strokePaint.alpha = (baseAlpha * 0.6f).toInt()
                                strokePaint.strokeWidth = item.strokeWidth * 1.3f
                                strokePaint.strokeCap = Paint.Cap.SQUARE
                                strokePaint.strokeJoin = Paint.Join.BEVEL
                                canvas.drawPath(path, strokePaint)
                            }
                            BrushStyle.CALLIGRAPHY -> {
                                strokePaint.color = baseColor
                                strokePaint.alpha = baseAlpha
                                strokePaint.strokeWidth = item.strokeWidth
                                strokePaint.strokeCap = Paint.Cap.ROUND
                                strokePaint.strokeJoin = Paint.Join.MITER
                                canvas.drawPath(path, strokePaint)
                            }
                            BrushStyle.NEON -> {
                                // Outer glow
                                strokePaint.color = baseColor
                                strokePaint.alpha = (baseAlpha * 0.25f).toInt()
                                strokePaint.strokeWidth = item.strokeWidth * 2.2f
                                strokePaint.strokeCap = Paint.Cap.ROUND
                                strokePaint.strokeJoin = Paint.Join.ROUND
                                canvas.drawPath(path, strokePaint)

                                // Middle glow
                                strokePaint.alpha = (baseAlpha * 0.5f).toInt()
                                strokePaint.strokeWidth = item.strokeWidth * 1.5f
                                canvas.drawPath(path, strokePaint)

                                // Core
                                strokePaint.color = android.graphics.Color.WHITE
                                strokePaint.alpha = (baseAlpha * 0.9f).toInt()
                                strokePaint.strokeWidth = max(2f, item.strokeWidth * 0.4f)
                                canvas.drawPath(path, strokePaint)
                            }
                            BrushStyle.SKETCH -> {
                                strokePaint.color = baseColor
                                strokePaint.alpha = (baseAlpha * 0.65f).toInt()
                                strokePaint.strokeWidth = max(1f, item.strokeWidth * 0.8f)
                                strokePaint.strokeCap = Paint.Cap.ROUND
                                strokePaint.strokeJoin = Paint.Join.ROUND
                                canvas.drawPath(path, strokePaint)
                            }
                        }
                    }
                }
                is DrawingItem.Shape -> {
                    val path = createShapeAndroidPath(
                        item.shapeType,
                        item.startX,
                        item.startY,
                        item.endX,
                        item.endY
                    )

                    // Fill
                    if (item.isFilled && item.fillColor != null &&
                        item.shapeType != ShapeType.LINE && item.shapeType != ShapeType.ARROW
                    ) {
                        fillPaint.color = item.fillColor
                        fillPaint.alpha = (item.alpha * 255).toInt().coerceIn(0, 255)
                        canvas.drawPath(path, fillPaint)
                    }

                    // Stroke
                    strokePaint.color = item.strokeColor
                    strokePaint.alpha = (item.alpha * 255).toInt().coerceIn(0, 255)
                    strokePaint.strokeWidth = item.strokeWidth
                    strokePaint.strokeCap = Paint.Cap.ROUND
                    strokePaint.strokeJoin = Paint.Join.ROUND
                    canvas.drawPath(path, strokePaint)
                }
            }
        }

        return bitmap
    }
}
