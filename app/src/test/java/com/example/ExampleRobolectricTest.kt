package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DrawingSerializer
import com.example.drawing.engine.DrawingRenderer
import com.example.drawing.model.BrushStyle
import com.example.drawing.model.DrawingItem
import com.example.drawing.model.PointD
import com.example.drawing.model.ShapeType
import com.example.drawing.model.ToolType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Creative Paint Studio", appName)
    }

    @Test
    fun `serialize and deserialize drawing strokes`() {
        val freehand = DrawingItem.Freehand(
            id = "test_stroke_1",
            points = listOf(PointD(10f, 20f), PointD(30f, 40f)),
            color = -16777216,
            strokeWidth = 12f,
            alpha = 1.0f,
            tool = ToolType.PEN,
            brushStyle = BrushStyle.ROUND,
            isEraser = false
        )

        val shape = DrawingItem.Shape(
            id = "test_shape_1",
            shapeType = ShapeType.RECTANGLE,
            startX = 50f,
            startY = 60f,
            endX = 150f,
            endY = 160f,
            strokeColor = -65536,
            fillColor = -16711936,
            isFilled = true,
            strokeWidth = 4f,
            alpha = 0.9f
        )

        val originalList = listOf(freehand, shape)
        val json = DrawingSerializer.serializeDrawingItems(originalList)
        val deserialized = DrawingSerializer.deserializeDrawingItems(json)

        assertEquals(2, deserialized.size)
        assertTrue(deserialized[0] is DrawingItem.Freehand)
        assertTrue(deserialized[1] is DrawingItem.Shape)

        val restoredFreehand = deserialized[0] as DrawingItem.Freehand
        assertEquals("test_stroke_1", restoredFreehand.id)
        assertEquals(2, restoredFreehand.points.size)
        assertEquals(10f, restoredFreehand.points[0].x, 0.01f)
    }

    @Test
    fun `smooth android path creation`() {
        val points = listOf(
            PointD(0f, 0f),
            PointD(50f, 50f),
            PointD(100f, 100f)
        )
        val path = DrawingRenderer.createSmoothAndroidPath(points)
        assertNotNull(path)
    }
}
