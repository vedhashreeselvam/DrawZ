package com.example.ui.screens

import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.drawing.model.BrushStyle
import com.example.drawing.model.ShapeType
import com.example.drawing.model.ToolType
import com.example.ui.components.BrushControlSheet
import com.example.ui.components.CanvasBackgroundSheet
import com.example.ui.components.ColorPickerSheet
import com.example.ui.components.DrawingCanvas
import com.example.ui.components.ShapeSelectorSheet
import com.example.viewmodel.DrawingViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    viewModel: DrawingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Bottom Sheet Visibility States
    var showColorPicker by remember { mutableStateOf(false) }
    var showBrushSheet by remember { mutableStateOf(false) }
    var showShapeSheet by remember { mutableStateOf(false) }
    var showBgSheet by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }

    val recentColors by viewModel.userPrefs.recentColors.collectAsStateWithLifecycle()
    val favoriteColors by viewModel.userPrefs.favoriteColors.collectAsStateWithLifecycle()

    // Handle toast messages
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Intercept back button if there are unsaved strokes
    BackHandler {
        if (uiState.hasUnsavedChanges) {
            showUnsavedChangesDialog = true
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = !uiState.isFullScreen,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showRenameDialog = true }
                        ) {
                            Text(
                                text = uiState.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (uiState.hasUnsavedChanges) {
                                    showUnsavedChangesDialog = true
                                } else {
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.testTag("workspace_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // Undo
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = uiState.canUndo,
                            modifier = Modifier.testTag("undo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (uiState.canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }

                        // Redo
                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = uiState.canRedo,
                            modifier = Modifier.testTag("redo_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (uiState.canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }

                        // Pan / Zoom Toggle
                        IconButton(
                            onClick = { viewModel.togglePanZoomMode() },
                            modifier = Modifier.testTag("pan_zoom_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PanTool,
                                contentDescription = "Pan and Zoom",
                                tint = if (uiState.isPanZoomMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Reset Zoom
                        if (uiState.zoom != 1f || uiState.panX != 0f || uiState.panY != 0f) {
                            IconButton(
                                onClick = { viewModel.resetZoomAndPan() },
                                modifier = Modifier.testTag("reset_zoom_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset Zoom"
                                )
                            }
                        }

                        // Full Screen Toggle
                        IconButton(
                            onClick = { viewModel.toggleFullScreen() },
                            modifier = Modifier.testTag("fullscreen_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Full Screen"
                            )
                        }

                        // Clear Canvas
                        IconButton(
                            onClick = { showClearDialog = true },
                            enabled = uiState.strokes.isNotEmpty(),
                            modifier = Modifier.testTag("clear_canvas_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Canvas"
                            )
                        }

                        // Save Button
                        IconButton(
                            onClick = { viewModel.saveProject() },
                            modifier = Modifier.testTag("save_project_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Project",
                                tint = if (uiState.hasUnsavedChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Export Button
                        IconButton(
                            onClick = onNavigateToExport,
                            modifier = Modifier.testTag("export_artwork_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.IosShare,
                                contentDescription = "Export Artwork",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Interactive Drawing Canvas
            DrawingCanvas(
                canvasWidth = uiState.canvasWidth,
                canvasHeight = uiState.canvasHeight,
                backgroundColor = uiState.backgroundColor,
                strokes = uiState.strokes,
                activeItem = uiState.activeItem,
                zoom = uiState.zoom,
                panX = uiState.panX,
                panY = uiState.panY,
                isPanZoomMode = uiState.isPanZoomMode,
                onStartStroke = { x, y, p -> viewModel.onStartStroke(x, y, p) },
                onMoveStroke = { x, y, p -> viewModel.onMoveStroke(x, y, p) },
                onEndStroke = { viewModel.onEndStroke() },
                onTransform = { panX, panY, zoomChange ->
                    viewModel.setPan(uiState.panX + panX, uiState.panY + panY)
                    viewModel.setZoom(uiState.zoom * zoomChange)
                },
                modifier = Modifier.testTag("main_drawing_canvas")
            )

            // Fullscreen Exit button floating at top-end
            if (uiState.isFullScreen) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                ) {
                    IconButton(
                        onClick = { viewModel.toggleFullScreen() },
                        modifier = Modifier.testTag("exit_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Exit Fullscreen"
                        )
                    }
                }
            }

            // Bottom Tool Dock / Floating Action Bar
            AnimatedVisibility(
                visible = !uiState.isFullScreen,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(28.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 1. Pen Tool
                        ToolDockItem(
                            icon = Icons.Default.Create,
                            label = "Pen",
                            isSelected = uiState.currentTool == ToolType.PEN,
                            onClick = { viewModel.setTool(ToolType.PEN) }
                        )

                        // 2. Brush Tool (Click to select, click again or long click opens styles)
                        ToolDockItem(
                            icon = Icons.Default.Brush,
                            label = uiState.currentBrushStyle.name.lowercase().replaceFirstChar { it.uppercase() },
                            isSelected = uiState.currentTool == ToolType.BRUSH,
                            onClick = {
                                if (uiState.currentTool == ToolType.BRUSH) {
                                    showBrushSheet = true
                                } else {
                                    viewModel.setTool(ToolType.BRUSH)
                                }
                            }
                        )

                        // 3. Shape Tool
                        ToolDockItem(
                            icon = Icons.Default.Category,
                            label = uiState.currentShape.name.lowercase().replaceFirstChar { it.uppercase() },
                            isSelected = uiState.currentTool == ToolType.SHAPE,
                            onClick = {
                                if (uiState.currentTool == ToolType.SHAPE) {
                                    showShapeSheet = true
                                } else {
                                    viewModel.setTool(ToolType.SHAPE)
                                }
                            }
                        )

                        // 4. Eraser Tool
                        ToolDockItem(
                            icon = Icons.Default.DeleteSweep,
                            label = "Eraser",
                            isSelected = uiState.currentTool == ToolType.ERASER,
                            onClick = {
                                if (uiState.currentTool == ToolType.ERASER) {
                                    showBrushSheet = true
                                } else {
                                    viewModel.setTool(ToolType.ERASER)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        // 5. Active Color Swatch (Tap opens color picker)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(uiState.currentColor))
                                .border(
                                    width = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { showColorPicker = true }
                                .testTag("color_picker_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            // Inner subtle ring
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                        }

                        // 6. Brush Size / Stroke Width Button
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clickable { showBrushSheet = true }
                                .testTag("brush_size_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(uiState.currentColor).copy(alpha = uiState.opacity))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val sizeToShow = if (uiState.currentTool == ToolType.ERASER) uiState.eraserWidth else uiState.strokeWidth
                                Text(
                                    text = "${sizeToShow.toInt()}px",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // 7. Canvas Background Button
                        ToolDockItem(
                            icon = Icons.Default.ColorLens,
                            label = "Canvas",
                            isSelected = false,
                            onClick = { showBgSheet = true }
                        )
                    }
                }
            }
        }
    }

    // Color Picker Modal Sheet
    if (showColorPicker) {
        ColorPickerSheet(
            currentColor = uiState.currentColor,
            recentColors = recentColors,
            favoriteColors = favoriteColors,
            onColorSelected = { c -> viewModel.setColor(c) },
            onToggleFavorite = { c -> viewModel.userPrefs.toggleFavoriteColor(c) },
            onDismiss = { showColorPicker = false }
        )
    }

    // Brush Control Modal Sheet
    if (showBrushSheet) {
        val isEraserMode = uiState.currentTool == ToolType.ERASER
        BrushControlSheet(
            strokeWidth = if (isEraserMode) uiState.eraserWidth else uiState.strokeWidth,
            opacity = uiState.opacity,
            currentColor = uiState.currentColor,
            currentStyle = uiState.currentBrushStyle,
            isEraser = isEraserMode,
            onStrokeWidthChange = { w ->
                if (isEraserMode) viewModel.setEraserWidth(w) else viewModel.setStrokeWidth(w)
            },
            onOpacityChange = { a -> viewModel.setOpacity(a) },
            onStyleChange = { s -> viewModel.setBrushStyle(s) },
            onDismiss = { showBrushSheet = false }
        )
    }

    // Shape Selector Modal Sheet
    if (showShapeSheet) {
        ShapeSelectorSheet(
            currentShape = uiState.currentShape,
            strokeWidth = uiState.strokeWidth,
            isFilled = uiState.isShapeFilled,
            strokeColor = uiState.currentColor,
            fillColor = uiState.shapeFillColor,
            onShapeSelected = { s -> viewModel.setShapeType(s) },
            onStrokeWidthChange = { w -> viewModel.setStrokeWidth(w) },
            onFilledChange = { f -> viewModel.setShapeFilled(f) },
            onFillColorChange = { c -> viewModel.setShapeFillColor(c) },
            onDismiss = { showShapeSheet = false }
        )
    }

    // Canvas Background Modal Sheet
    if (showBgSheet) {
        CanvasBackgroundSheet(
            currentBgColor = uiState.backgroundColor,
            onColorSelected = { c -> viewModel.setBackgroundColor(c) },
            onDismiss = { showBgSheet = false }
        )
    }

    // Clear Canvas Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Entire Canvas?") },
            text = { Text("All strokes and drawings will be cleared. This action cannot be easily reversed if you save.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCanvas()
                        showClearDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_button")
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Artwork Dialog
    if (showRenameDialog) {
        var newTitle by remember { mutableStateOf(uiState.title) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Artwork") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth().testTag("rename_title_input")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.updateTitle(newTitle.trim())
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Unsaved Changes on Back Dialog
    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes in your artwork. Would you like to save before exiting?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveProject {
                            showUnsavedChangesDialog = false
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Save & Exit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUnsavedChangesDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}

@Composable
fun ToolDockItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            }
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                maxLines = 1,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
