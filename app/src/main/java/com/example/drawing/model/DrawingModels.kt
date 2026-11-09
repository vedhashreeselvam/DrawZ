package com.example.drawing.model

enum class ToolType {
    PEN,
    BRUSH,
    ERASER,
    SHAPE
}

enum class BrushStyle {
    ROUND,       // Standard smooth round stroke
    MARKER,      // Semi-transparent chiseled stroke
    CALLIGRAPHY, // Slanted calligraphy stroke
    NEON,        // Glowing vibrant neon line
    SKETCH       // Multi-line pencil sketch feel
}

enum class ShapeType {
    LINE,
    RECTANGLE,
    ROUNDED_RECTANGLE,
    CIRCLE,
    OVAL,
    TRIANGLE,
    ARROW,
    POLYGON,
    STAR
}

data class PointD(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f
)

sealed class DrawingItem {
    abstract val id: String
    abstract val alpha: Float

    data class Freehand(
        override val id: String,
        val points: List<PointD>,
        val color: Int, // ARGB
        val strokeWidth: Float,
        override val alpha: Float = 1.0f,
        val tool: ToolType = ToolType.PEN,
        val brushStyle: BrushStyle = BrushStyle.ROUND,
        val isEraser: Boolean = false
    ) : DrawingItem()

    data class Shape(
        override val id: String,
        val shapeType: ShapeType,
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val strokeColor: Int,
        val fillColor: Int? = null,
        val isFilled: Boolean = false,
        val strokeWidth: Float = 6f,
        override val alpha: Float = 1.0f
    ) : DrawingItem()
}
