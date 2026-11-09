package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DrawingProject
import com.example.data.DrawingSerializer
import com.example.data.ImageExporter
import com.example.data.UserPreferences
import com.example.drawing.model.BrushStyle
import com.example.drawing.model.DrawingItem
import com.example.drawing.model.PointD
import com.example.drawing.model.ShapeType
import com.example.drawing.model.ToolType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class DrawingUiState(
    val projectId: Long = 0L,
    val title: String = "Untitled Artwork",
    val canvasWidth: Int = 1080,
    val canvasHeight: Int = 1080,
    val backgroundColor: Int = AndroidColor.WHITE,
    val strokes: List<DrawingItem> = emptyList(),
    val redoStack: List<DrawingItem> = emptyList(),
    val currentTool: ToolType = ToolType.PEN,
    val currentBrushStyle: BrushStyle = BrushStyle.ROUND,
    val currentColor: Int = AndroidColor.BLACK,
    val strokeWidth: Float = 10f,
    val opacity: Float = 1.0f,
    val eraserWidth: Float = 30f,
    val currentShape: ShapeType = ShapeType.RECTANGLE,
    val isShapeFilled: Boolean = false,
    val shapeFillColor: Int = AndroidColor.parseColor("#3B82F6"),
    val activeItem: DrawingItem? = null,
    val zoom: Float = 1.0f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val isPanZoomMode: Boolean = false,
    val isFullScreen: Boolean = false,
    val isSaving: Boolean = false,
    val hasUnsavedChanges: Boolean = false
) {
    val canUndo: Boolean get() = strokes.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
}

class DrawingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val drawingDao = db.drawingDao()
    val userPrefs = UserPreferences(application)

    private val _uiState = MutableStateFlow(
        DrawingUiState(
            backgroundColor = userPrefs.defaultCanvasColor,
            strokeWidth = userPrefs.defaultBrushWidth
        )
    )
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Temporary storage for in-progress stroke points
    private val currentPoints = mutableListOf<PointD>()
    private var shapeStartX = 0f
    private var shapeStartY = 0f

    fun initNewCanvas(
        title: String = "Untitled Artwork",
        width: Int = 1080,
        height: Int = 1080,
        bgColor: Int = userPrefs.defaultCanvasColor
    ) {
        _uiState.value = DrawingUiState(
            projectId = 0L,
            title = title,
            canvasWidth = width,
            canvasHeight = height,
            backgroundColor = bgColor,
            strokeWidth = userPrefs.defaultBrushWidth,
            currentColor = AndroidColor.BLACK
        )
        currentPoints.clear()
    }

    fun loadProject(projectId: Long) {
        viewModelScope.launch {
            val project = drawingDao.getProjectById(projectId) ?: return@launch
            val json = ImageExporter.loadProjectData(project.projectDataPath)
            val items = DrawingSerializer.deserializeDrawingItems(json)

            _uiState.value = _uiState.value.copy(
                projectId = project.id,
                title = project.title,
                canvasWidth = project.canvasWidth,
                canvasHeight = project.canvasHeight,
                backgroundColor = project.backgroundColor,
                strokes = items,
                redoStack = emptyList(),
                activeItem = null,
                hasUnsavedChanges = false
            )
        }
    }

    fun onStartStroke(canvasX: Float, canvasY: Float, pressure: Float = 1.0f) {
        val state = _uiState.value
        if (state.isPanZoomMode) return

        when (state.currentTool) {
            ToolType.PEN, ToolType.BRUSH, ToolType.ERASER -> {
                currentPoints.clear()
                val pt = PointD(canvasX, canvasY, pressure)
                currentPoints.add(pt)

                val item = DrawingItem.Freehand(
                    id = UUID.randomUUID().toString(),
                    points = listOf(pt),
                    color = state.currentColor,
                    strokeWidth = if (state.currentTool == ToolType.ERASER) state.eraserWidth else state.strokeWidth,
                    alpha = state.opacity,
                    tool = state.currentTool,
                    brushStyle = if (state.currentTool == ToolType.PEN) BrushStyle.ROUND else state.currentBrushStyle,
                    isEraser = state.currentTool == ToolType.ERASER
                )
                _uiState.value = state.copy(activeItem = item)
            }
            ToolType.SHAPE -> {
                shapeStartX = canvasX
                shapeStartY = canvasY
                val item = DrawingItem.Shape(
                    id = UUID.randomUUID().toString(),
                    shapeType = state.currentShape,
                    startX = canvasX,
                    startY = canvasY,
                    endX = canvasX,
                    endY = canvasY,
                    strokeColor = state.currentColor,
                    fillColor = state.shapeFillColor,
                    isFilled = state.isShapeFilled,
                    strokeWidth = state.strokeWidth,
                    alpha = state.opacity
                )
                _uiState.value = state.copy(activeItem = item)
            }
        }
    }

    fun onMoveStroke(canvasX: Float, canvasY: Float, pressure: Float = 1.0f) {
        val state = _uiState.value
        if (state.isPanZoomMode) return

        when (state.currentTool) {
            ToolType.PEN, ToolType.BRUSH, ToolType.ERASER -> {
                currentPoints.add(PointD(canvasX, canvasY, pressure))
                val active = state.activeItem as? DrawingItem.Freehand
                if (active != null) {
                    val updated = active.copy(points = ArrayList(currentPoints))
                    _uiState.value = state.copy(activeItem = updated)
                }
            }
            ToolType.SHAPE -> {
                val active = state.activeItem as? DrawingItem.Shape
                if (active != null) {
                    val updated = active.copy(endX = canvasX, endY = canvasY)
                    _uiState.value = state.copy(activeItem = updated)
                }
            }
        }
    }

    fun onEndStroke() {
        val state = _uiState.value
        if (state.isPanZoomMode) return

        val active = state.activeItem
        if (active != null) {
            val newStrokes = state.strokes + active
            _uiState.value = state.copy(
                strokes = newStrokes,
                redoStack = emptyList(), // Clear redo on new action
                activeItem = null,
                hasUnsavedChanges = true
            )
            // Add current color to recent colors if not eraser
            if (state.currentTool != ToolType.ERASER) {
                userPrefs.addRecentColor(state.currentColor)
            }
        }
        currentPoints.clear()
    }

    fun undo() {
        val state = _uiState.value
        if (state.strokes.isEmpty()) return
        val lastItem = state.strokes.last()
        val newStrokes = state.strokes.dropLast(1)
        val newRedo = state.redoStack + lastItem
        _uiState.value = state.copy(
            strokes = newStrokes,
            redoStack = newRedo,
            hasUnsavedChanges = true
        )
    }

    fun redo() {
        val state = _uiState.value
        if (state.redoStack.isEmpty()) return
        val itemToRestore = state.redoStack.last()
        val newRedo = state.redoStack.dropLast(1)
        val newStrokes = state.strokes + itemToRestore
        _uiState.value = state.copy(
            strokes = newStrokes,
            redoStack = newRedo,
            hasUnsavedChanges = true
        )
    }

    fun clearCanvas() {
        val state = _uiState.value
        if (state.strokes.isEmpty()) return
        _uiState.value = state.copy(
            strokes = emptyList(),
            redoStack = emptyList(),
            activeItem = null,
            hasUnsavedChanges = true
        )
    }

    fun setTool(tool: ToolType) {
        _uiState.value = _uiState.value.copy(currentTool = tool, isPanZoomMode = false)
    }

    fun setBrushStyle(style: BrushStyle) {
        _uiState.value = _uiState.value.copy(currentBrushStyle = style, currentTool = ToolType.BRUSH)
    }

    fun setColor(color: Int) {
        _uiState.value = _uiState.value.copy(currentColor = color)
        userPrefs.addRecentColor(color)
    }

    fun setStrokeWidth(width: Float) {
        _uiState.value = _uiState.value.copy(strokeWidth = width.coerceIn(1f, 100f))
    }

    fun setOpacity(alpha: Float) {
        _uiState.value = _uiState.value.copy(opacity = alpha.coerceIn(0.05f, 1.0f))
    }

    fun setEraserWidth(width: Float) {
        _uiState.value = _uiState.value.copy(eraserWidth = width.coerceIn(1f, 100f))
    }

    fun setShapeType(shape: ShapeType) {
        _uiState.value = _uiState.value.copy(currentShape = shape, currentTool = ToolType.SHAPE)
    }

    fun setShapeFilled(filled: Boolean) {
        _uiState.value = _uiState.value.copy(isShapeFilled = filled)
    }

    fun setShapeFillColor(color: Int) {
        _uiState.value = _uiState.value.copy(shapeFillColor = color)
    }

    fun setBackgroundColor(color: Int) {
        _uiState.value = _uiState.value.copy(backgroundColor = color, hasUnsavedChanges = true)
    }

    fun setZoom(zoom: Float) {
        _uiState.value = _uiState.value.copy(zoom = zoom.coerceIn(0.5f, 5.0f))
    }

    fun setPan(panX: Float, panY: Float) {
        _uiState.value = _uiState.value.copy(panX = panX, panY = panY)
    }

    fun resetZoomAndPan() {
        _uiState.value = _uiState.value.copy(zoom = 1.0f, panX = 0f, panY = 0f)
    }

    fun togglePanZoomMode() {
        _uiState.value = _uiState.value.copy(isPanZoomMode = !_uiState.value.isPanZoomMode)
    }

    fun toggleFullScreen() {
        _uiState.value = _uiState.value.copy(isFullScreen = !_uiState.value.isFullScreen)
    }

    fun updateTitle(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle, hasUnsavedChanges = true)
    }

    fun saveProject(
        customTitle: String? = null,
        onSuccess: (Long) -> Unit = {}
    ) {
        val state = _uiState.value
        val titleToSave = customTitle?.takeIf { it.isNotBlank() } ?: state.title
        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val existingId = state.projectId

                // 1. Generate thumbnail
                val fullBitmap = ImageExporter.createArtworkBitmap(
                    state.canvasWidth,
                    state.canvasHeight,
                    state.backgroundColor,
                    state.strokes
                )

                val idToUse = if (existingId > 0) existingId else System.currentTimeMillis()
                val thumbPath = ImageExporter.saveInternalThumbnail(context, idToUse, fullBitmap)

                // 2. Serialize stroke data
                val jsonStrokes = DrawingSerializer.serializeDrawingItems(state.strokes)
                val projectPath = ImageExporter.saveProjectData(context, idToUse, jsonStrokes)

                val project = DrawingProject(
                    id = if (existingId > 0) existingId else 0,
                    title = titleToSave,
                    thumbnailPath = thumbPath,
                    projectDataPath = projectPath,
                    canvasWidth = state.canvasWidth,
                    canvasHeight = state.canvasHeight,
                    backgroundColor = state.backgroundColor,
                    strokeCount = state.strokes.size,
                    modifiedAt = System.currentTimeMillis()
                )

                val savedId = if (existingId > 0) {
                    drawingDao.update(project)
                    existingId
                } else {
                    drawingDao.insert(project)
                }

                _uiState.value = _uiState.value.copy(
                    projectId = savedId,
                    title = titleToSave,
                    isSaving = false,
                    hasUnsavedChanges = false
                )
                _toastEvent.emit("Artwork saved to Studio Gallery!")
                onSuccess(savedId)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isSaving = false)
                _toastEvent.emit("Error saving artwork: ${e.message}")
            }
        }
    }

    fun getExportBitmap(): Bitmap {
        val state = _uiState.value
        return ImageExporter.createArtworkBitmap(
            state.canvasWidth,
            state.canvasHeight,
            state.backgroundColor,
            state.strokes
        )
    }
}
