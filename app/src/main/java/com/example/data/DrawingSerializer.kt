package com.example.data

import com.example.drawing.model.BrushStyle
import com.example.drawing.model.DrawingItem
import com.example.drawing.model.PointD
import com.example.drawing.model.ShapeType
import com.example.drawing.model.ToolType
import org.json.JSONArray
import org.json.JSONObject

object DrawingSerializer {

    fun serializeDrawingItems(items: List<DrawingItem>): String {
        val rootArray = JSONArray()

        for (item in items) {
            val obj = JSONObject()
            when (item) {
                is DrawingItem.Freehand -> {
                    obj.put("type", "freehand")
                    obj.put("id", item.id)
                    obj.put("color", item.color)
                    obj.put("strokeWidth", item.strokeWidth.toDouble())
                    obj.put("alpha", item.alpha.toDouble())
                    obj.put("tool", item.tool.name)
                    obj.put("brushStyle", item.brushStyle.name)
                    obj.put("isEraser", item.isEraser)

                    val ptsArray = JSONArray()
                    for (p in item.points) {
                        val ptObj = JSONObject()
                        ptObj.put("x", p.x.toDouble())
                        ptObj.put("y", p.y.toDouble())
                        ptObj.put("p", p.pressure.toDouble())
                        ptsArray.put(ptObj)
                    }
                    obj.put("points", ptsArray)
                }
                is DrawingItem.Shape -> {
                    obj.put("type", "shape")
                    obj.put("id", item.id)
                    obj.put("shapeType", item.shapeType.name)
                    obj.put("startX", item.startX.toDouble())
                    obj.put("startY", item.startY.toDouble())
                    obj.put("endX", item.endX.toDouble())
                    obj.put("endY", item.endY.toDouble())
                    obj.put("strokeColor", item.strokeColor)
                    if (item.fillColor != null) {
                        obj.put("fillColor", item.fillColor)
                    }
                    obj.put("isFilled", item.isFilled)
                    obj.put("strokeWidth", item.strokeWidth.toDouble())
                    obj.put("alpha", item.alpha.toDouble())
                }
            }
            rootArray.put(obj)
        }

        return rootArray.toString()
    }

    fun deserializeDrawingItems(jsonString: String): List<DrawingItem> {
        val result = mutableListOf<DrawingItem>()
        if (jsonString.isBlank()) return result

        try {
            val rootArray = JSONArray(jsonString)
            for (i in 0 until rootArray.length()) {
                val obj = rootArray.getJSONObject(i)
                val type = obj.optString("type")
                val id = obj.optString("id", System.currentTimeMillis().toString() + "_$i")

                if (type == "freehand") {
                    val color = obj.optInt("color", -16777216)
                    val strokeWidth = obj.optDouble("strokeWidth", 8.0).toFloat()
                    val alpha = obj.optDouble("alpha", 1.0).toFloat()
                    val toolName = obj.optString("tool", ToolType.PEN.name)
                    val brushName = obj.optString("brushStyle", BrushStyle.ROUND.name)
                    val isEraser = obj.optBoolean("isEraser", false)

                    val tool = try { ToolType.valueOf(toolName) } catch (e: Exception) { ToolType.PEN }
                    val brushStyle = try { BrushStyle.valueOf(brushName) } catch (e: Exception) { BrushStyle.ROUND }

                    val pts = mutableListOf<PointD>()
                    val ptsArray = obj.optJSONArray("points")
                    if (ptsArray != null) {
                        for (pIdx in 0 until ptsArray.length()) {
                            val ptObj = ptsArray.getJSONObject(pIdx)
                            pts.add(
                                PointD(
                                    x = ptObj.optDouble("x", 0.0).toFloat(),
                                    y = ptObj.optDouble("y", 0.0).toFloat(),
                                    pressure = ptObj.optDouble("p", 1.0).toFloat()
                                )
                            )
                        }
                    }

                    result.add(
                        DrawingItem.Freehand(
                            id = id,
                            points = pts,
                            color = color,
                            strokeWidth = strokeWidth,
                            alpha = alpha,
                            tool = tool,
                            brushStyle = brushStyle,
                            isEraser = isEraser
                        )
                    )
                } else if (type == "shape") {
                    val shapeTypeName = obj.optString("shapeType", ShapeType.RECTANGLE.name)
                    val shapeType = try { ShapeType.valueOf(shapeTypeName) } catch (e: Exception) { ShapeType.RECTANGLE }
                    val startX = obj.optDouble("startX", 0.0).toFloat()
                    val startY = obj.optDouble("startY", 0.0).toFloat()
                    val endX = obj.optDouble("endX", 0.0).toFloat()
                    val endY = obj.optDouble("endY", 0.0).toFloat()
                    val strokeColor = obj.optInt("strokeColor", -16777216)
                    val fillColor = if (obj.has("fillColor")) obj.optInt("fillColor") else null
                    val isFilled = obj.optBoolean("isFilled", false)
                    val strokeWidth = obj.optDouble("strokeWidth", 6.0).toFloat()
                    val alpha = obj.optDouble("alpha", 1.0).toFloat()

                    result.add(
                        DrawingItem.Shape(
                            id = id,
                            shapeType = shapeType,
                            startX = startX,
                            startY = startY,
                            endX = endX,
                            endY = endY,
                            strokeColor = strokeColor,
                            fillColor = fillColor,
                            isFilled = isFilled,
                            strokeWidth = strokeWidth,
                            alpha = alpha
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }
}
