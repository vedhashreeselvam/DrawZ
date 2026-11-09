package com.example.ui.components

import android.graphics.Color as AndroidColor
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CanvasPreset(
    val name: String,
    val width: Int,
    val height: Int,
    val ratioLabel: String,
    val icon: ImageVector
)

val CANVAS_PRESETS = listOf(
    CanvasPreset("Square", 1080, 1080, "1:1", Icons.Default.CropSquare),
    CanvasPreset("Portrait", 1080, 1920, "9:16", Icons.Default.CropPortrait),
    CanvasPreset("Landscape", 1920, 1080, "16:9", Icons.Default.CropLandscape),
    CanvasPreset("Poster A4", 1240, 1754, "A4", Icons.Default.Description)
)

@Composable
fun NewCanvasDialog(
    initialBgColor: Int = AndroidColor.WHITE,
    onDismiss: () -> Unit,
    onCreate: (title: String, width: Int, height: Int, bgColor: Int) -> Unit
) {
    var title by remember { mutableStateOf("Artwork ${System.currentTimeMillis() % 1000}") }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    var selectedBgColor by remember { mutableIntStateOf(initialBgColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Canvas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Drawing Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_canvas_title_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Canvas Dimensions",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                CANVAS_PRESETS.forEachIndexed { index, preset ->
                    val isSelected = selectedPresetIndex == index
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
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedPresetIndex = index }
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = preset.icon,
                                    contentDescription = preset.name,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${preset.width} × ${preset.height} px (${preset.ratioLabel})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Initial Background Color",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        AndroidColor.WHITE,
                        AndroidColor.parseColor("#FFFDF0"),
                        AndroidColor.parseColor("#1E293B"),
                        AndroidColor.parseColor("#050505"),
                        AndroidColor.parseColor("#1E3A8A")
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(
                                    width = if (selectedBgColor == color) 2.5.dp else 1.dp,
                                    color = if (selectedBgColor == color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .clickable { selectedBgColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = CANVAS_PRESETS[selectedPresetIndex]
                    onCreate(title.ifBlank { "Untitled Artwork" }, p.width, p.height, selectedBgColor)
                },
                modifier = Modifier.testTag("confirm_create_canvas_button")
            ) {
                Text("Start Drawing")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
