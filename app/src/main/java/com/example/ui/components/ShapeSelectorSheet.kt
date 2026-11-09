package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Hexagon
import androidx.compose.material.icons.filled.PanoramaFishEye
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import com.example.drawing.model.ShapeType

data class ShapeOption(
    val type: ShapeType,
    val name: String,
    val icon: ImageVector
)

val SHAPE_OPTIONS = listOf(
    ShapeOption(ShapeType.LINE, "Line", Icons.Default.Timeline),
    ShapeOption(ShapeType.RECTANGLE, "Rectangle", Icons.Default.CropSquare),
    ShapeOption(ShapeType.ROUNDED_RECTANGLE, "Round Rect", Icons.Default.CropSquare),
    ShapeOption(ShapeType.CIRCLE, "Circle", Icons.Default.PanoramaFishEye),
    ShapeOption(ShapeType.OVAL, "Oval", Icons.Default.RadioButtonUnchecked),
    ShapeOption(ShapeType.TRIANGLE, "Triangle", Icons.Default.ChangeHistory),
    ShapeOption(ShapeType.ARROW, "Arrow", Icons.AutoMirrored.Filled.ArrowForward),
    ShapeOption(ShapeType.POLYGON, "Polygon", Icons.Default.Hexagon),
    ShapeOption(ShapeType.STAR, "Star", Icons.Default.Star)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ShapeSelectorSheet(
    currentShape: ShapeType,
    strokeWidth: Float,
    isFilled: Boolean,
    strokeColor: Int,
    fillColor: Int,
    onShapeSelected: (ShapeType) -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    onFilledChange: (Boolean) -> Unit,
    onFillColorChange: (Int) -> Unit,
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
            Text(
                text = "Shape Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Shapes Grid
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SHAPE_OPTIONS.forEach { opt ->
                    val isSelected = currentShape == opt.type
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
                            .size(68.dp)
                            .clickable { onShapeSelected(opt.type) }
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = opt.icon,
                                contentDescription = opt.name,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = opt.name,
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

            Spacer(modifier = Modifier.height(20.dp))

            // Outline Width Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Outline Thickness",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${strokeWidth.toInt()} px",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Slider(
                value = strokeWidth,
                onValueChange = onStrokeWidthChange,
                valueRange = 1f..60f,
                modifier = Modifier.fillMaxWidth()
            )

            // Fill Toggle (if not line or arrow)
            if (currentShape != ShapeType.LINE && currentShape != ShapeType.ARROW) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Fill Shape",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isFilled) "Filled shape interior" else "Outlined wireframe only",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isFilled,
                        onCheckedChange = onFilledChange,
                        modifier = Modifier.testTag("shape_fill_switch")
                    )
                }

                if (isFilled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Fill Color Preset",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AndroidColor.parseColor("#3B82F6"), // Blue
                            AndroidColor.parseColor("#EF4444"), // Red
                            AndroidColor.parseColor("#10B981"), // Green
                            AndroidColor.parseColor("#F59E0B"), // Amber
                            AndroidColor.parseColor("#8B5CF6"), // Purple
                            AndroidColor.parseColor("#EC4899"), // Pink
                            AndroidColor.WHITE,
                            AndroidColor.BLACK
                        ).forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .border(
                                        width = if (c == fillColor) 2.5.dp else 1.dp,
                                        color = if (c == fillColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    )
                                    .clickable { onFillColorChange(c) }
                            )
                        }
                    }
                }
            }
        }
    }
}
