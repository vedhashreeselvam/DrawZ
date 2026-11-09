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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BgPreset(
    val name: String,
    val color: Int,
    val isDark: Boolean = false
)

val BG_PRESETS = listOf(
    BgPreset("Pure White", AndroidColor.WHITE, isDark = false),
    BgPreset("Warm Cream", AndroidColor.parseColor("#FFFDF0"), isDark = false),
    BgPreset("Art Parchment", AndroidColor.parseColor("#F5EBE0"), isDark = false),
    BgPreset("Soft Grey", AndroidColor.parseColor("#E2E8F0"), isDark = false),
    BgPreset("Slate Dark", AndroidColor.parseColor("#1E293B"), isDark = true),
    BgPreset("Midnight Blue", AndroidColor.parseColor("#0F172A"), isDark = true),
    BgPreset("Studio Black", AndroidColor.parseColor("#050505"), isDark = true),
    BgPreset("Blueprint", AndroidColor.parseColor("#1E3A8A"), isDark = true)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasBackgroundSheet(
    currentBgColor: Int,
    onColorSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCustomPicker by remember { mutableStateOf(false) }

    var red by remember(currentBgColor) { mutableIntStateOf(AndroidColor.red(currentBgColor)) }
    var green by remember(currentBgColor) { mutableIntStateOf(AndroidColor.green(currentBgColor)) }
    var blue by remember(currentBgColor) { mutableIntStateOf(AndroidColor.blue(currentBgColor)) }
    var hexInput by remember(currentBgColor) {
        mutableStateOf(String.format("#%06X", 0xFFFFFF and currentBgColor))
    }

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
                text = "Canvas Background",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Presets List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BG_PRESETS.take(4).forEach { preset ->
                    BackgroundPresetCard(
                        preset = preset,
                        isSelected = currentBgColor == preset.color,
                        modifier = Modifier.weight(1f),
                        onClick = { onColorSelected(preset.color) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BG_PRESETS.drop(4).forEach { preset ->
                    BackgroundPresetCard(
                        preset = preset,
                        isSelected = currentBgColor == preset.color,
                        modifier = Modifier.weight(1f),
                        onClick = { onColorSelected(preset.color) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom RGB / HEX Expander
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCustomPicker = !showCustomPicker }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Custom Background Color",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Custom Color (RGB / HEX)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(currentBgColor))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                }
            }

            if (showCustomPicker) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Red
                    Text("Red: $red", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = red.toFloat(),
                        onValueChange = {
                            red = it.toInt()
                            val c = AndroidColor.rgb(red, green, blue)
                            onColorSelected(c)
                            hexInput = String.format("#%06X", 0xFFFFFF and c)
                        },
                        valueRange = 0f..255f
                    )

                    // Green
                    Text("Green: $green", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = green.toFloat(),
                        onValueChange = {
                            green = it.toInt()
                            val c = AndroidColor.rgb(red, green, blue)
                            onColorSelected(c)
                            hexInput = String.format("#%06X", 0xFFFFFF and c)
                        },
                        valueRange = 0f..255f
                    )

                    // Blue
                    Text("Blue: $blue", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = blue.toFloat(),
                        onValueChange = {
                            blue = it.toInt()
                            val c = AndroidColor.rgb(red, green, blue)
                            onColorSelected(c)
                            hexInput = String.format("#%06X", 0xFFFFFF and c)
                        },
                        valueRange = 0f..255f
                    )

                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { txt ->
                            hexInput = txt
                            try {
                                if (txt.startsWith("#") && (txt.length == 7 || txt.length == 9)) {
                                    val parsed = AndroidColor.parseColor(txt)
                                    onColorSelected(parsed)
                                    red = AndroidColor.red(parsed)
                                    green = AndroidColor.green(parsed)
                                    blue = AndroidColor.blue(parsed)
                                }
                            } catch (e: Exception) {
                                // ignore invalid hex while typing
                            }
                        },
                        label = { Text("Hex Code") },
                        modifier = Modifier.fillMaxWidth().testTag("bg_hex_input"),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
fun BackgroundPresetCard(
    preset: BgPreset,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(preset.color)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .height(54.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = if (preset.isDark) Color.White else Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    maxLines = 1,
                    color = if (preset.isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f)
                )
            }
        }
    }
}
