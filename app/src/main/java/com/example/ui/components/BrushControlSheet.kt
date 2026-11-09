package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drawing.model.BrushStyle

data class BrushStyleOption(
    val style: BrushStyle,
    val title: String,
    val icon: ImageVector,
    val description: String
)

val BRUSH_STYLE_OPTIONS = listOf(
    BrushStyleOption(BrushStyle.ROUND, "Round Pen", Icons.Default.Create, "Smooth, consistent stroke"),
    BrushStyleOption(BrushStyle.MARKER, "Marker", Icons.Default.Highlight, "Semi-transparent highlighter"),
    BrushStyleOption(BrushStyle.CALLIGRAPHY, "Calligraphy", Icons.Default.Edit, "Angled ribbon stroke"),
    BrushStyleOption(BrushStyle.NEON, "Neon Glow", Icons.Default.FlashOn, "Vibrant glowing outline"),
    BrushStyleOption(BrushStyle.SKETCH, "Pencil Sketch", Icons.Default.Brush, "Textured artistic pencil")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrushControlSheet(
    strokeWidth: Float,
    opacity: Float,
    currentColor: Int,
    currentStyle: BrushStyle,
    isEraser: Boolean = false,
    onStrokeWidthChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onStyleChange: (BrushStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Sheet Title & Live Preview Card
            Text(
                text = if (isEraser) "Eraser Settings" else "Brush & Stroke Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Live Stroke Preview Box
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Live Preview",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val previewColor = if (isEraser) {
                            MaterialTheme.colorScheme.outline
                        } else {
                            Color(currentColor).copy(alpha = opacity)
                        }
                        Box(
                            modifier = Modifier
                                .size(strokeWidth.coerceIn(4f, 96f).dp)
                                .clip(CircleShape)
                                .background(previewColor)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        )
                    }
                    Text(
                        text = "${strokeWidth.toInt()} px",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Size presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Thickness",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(4f, 12f, 24f, 48f, 72f).forEach { preset ->
                        FilterChip(
                            selected = strokeWidth.toInt() == preset.toInt(),
                            onClick = { onStrokeWidthChange(preset) },
                            label = { Text("${preset.toInt()}p") }
                        )
                    }
                }
            }

            // Thickness Slider
            Slider(
                value = strokeWidth,
                onValueChange = onStrokeWidthChange,
                valueRange = 1f..100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("brush_width_slider")
            )

            // Opacity Slider (only when not eraser)
            if (!isEraser) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Opacity",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(opacity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Slider(
                    value = opacity,
                    onValueChange = onOpacityChange,
                    valueRange = 0.05f..1.0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("brush_opacity_slider")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Brush Styles
                Text(
                    text = "Brush Style",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BRUSH_STYLE_OPTIONS.forEach { opt ->
                        val isSelected = currentStyle == opt.style
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onStyleChange(opt.style) }
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = opt.icon,
                                    contentDescription = opt.title,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = opt.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
