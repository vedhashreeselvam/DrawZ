package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PRESET_PALETTE = listOf(
    AndroidColor.parseColor("#000000"), // Black
    AndroidColor.parseColor("#FFFFFF"), // White
    AndroidColor.parseColor("#475569"), // Slate Grey
    AndroidColor.parseColor("#94A3B8"), // Light Slate
    AndroidColor.parseColor("#EF4444"), // Red
    AndroidColor.parseColor("#DC2626"), // Crimson
    AndroidColor.parseColor("#F97316"), // Orange
    AndroidColor.parseColor("#EA580C"), // Deep Orange
    AndroidColor.parseColor("#F59E0B"), // Amber
    AndroidColor.parseColor("#EAB308"), // Yellow
    AndroidColor.parseColor("#84CC16"), // Lime
    AndroidColor.parseColor("#10B981"), // Emerald Green
    AndroidColor.parseColor("#059669"), // Dark Green
    AndroidColor.parseColor("#06B6D4"), // Cyan
    AndroidColor.parseColor("#0EA5E9"), // Sky Blue
    AndroidColor.parseColor("#3B82F6"), // Blue
    AndroidColor.parseColor("#1D4ED8"), // Royal Blue
    AndroidColor.parseColor("#6366F1"), // Indigo
    AndroidColor.parseColor("#8B5CF6"), // Violet
    AndroidColor.parseColor("#A855F7"), // Purple
    AndroidColor.parseColor("#D946EF"), // Fuchsia
    AndroidColor.parseColor("#EC4899"), // Pink
    AndroidColor.parseColor("#F43F5E"), // Rose
    AndroidColor.parseColor("#78350F")  // Brown
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColorPickerSheet(
    currentColor: Int,
    recentColors: List<Int>,
    favoriteColors: List<Int>,
    onColorSelected: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Palette, 1: Custom HSV

    // HSV representation of current color
    val hsv = remember(currentColor) {
        val array = FloatArray(3)
        AndroidColor.colorToHSV(currentColor, array)
        array
    }
    var hue by remember(currentColor) { mutableFloatStateOf(hsv[0]) }
    var saturation by remember(currentColor) { mutableFloatStateOf(hsv[1]) }
    var value by remember(currentColor) { mutableFloatStateOf(hsv[2]) }
    val currentAlpha = AndroidColor.alpha(currentColor) / 255f
    var alpha by remember(currentColor) { mutableFloatStateOf(currentAlpha) }

    fun computeCustomColor(): Int {
        val base = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value))
        val a = (alpha * 255).toInt().coerceIn(0, 255)
        return AndroidColor.argb(a, AndroidColor.red(base), AndroidColor.green(base), AndroidColor.blue(base))
    }

    var hexText by remember(currentColor) {
        mutableStateOf(String.format("#%08X", currentColor))
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
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Color Preview & Favorite Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(currentColor))
                            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Color Palette",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("#%06X", 0xFFFFFF and currentColor),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val isFav = favoriteColors.contains(currentColor)
                IconButton(
                    onClick = { onToggleFavorite(currentColor) },
                    modifier = Modifier.testTag("favorite_color_button")
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite Color",
                        tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Standard Palette") },
                    modifier = Modifier.testTag("tab_standard_palette")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Custom Color") },
                    modifier = Modifier.testTag("tab_custom_color")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recently Used Colors
            if (recentColors.isNotEmpty()) {
                Text(
                    text = "Recently Used",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (c in recentColors) {
                        ColorSwatch(
                            color = c,
                            isSelected = c == currentColor,
                            onClick = { onColorSelected(c) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Favorites Row
            if (favoriteColors.isNotEmpty()) {
                Text(
                    text = "Favorite Colors",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (c in favoriteColors) {
                        ColorSwatch(
                            color = c,
                            isSelected = c == currentColor,
                            onClick = { onColorSelected(c) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (selectedTab == 0) {
                // Preset Palette Grid
                Text(
                    text = "Preset Colors",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (c in PRESET_PALETTE) {
                        ColorSwatch(
                            color = c,
                            isSelected = c == currentColor,
                            onClick = { onColorSelected(c) }
                        )
                    }
                }
            } else {
                // Custom HSV Controls
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Hue Slider
                    Text(
                        text = "Hue: ${hue.toInt()}°",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Red, Color.Yellow, Color.Green,
                                        Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                                    )
                                )
                            )
                    )
                    Slider(
                        value = hue,
                        onValueChange = {
                            hue = it
                            val newC = computeCustomColor()
                            onColorSelected(newC)
                            hexText = String.format("#%08X", newC)
                        },
                        valueRange = 0f..360f,
                        modifier = Modifier.testTag("hue_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Saturation Slider
                    Text(
                        text = "Saturation: ${(saturation * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = saturation,
                        onValueChange = {
                            saturation = it
                            val newC = computeCustomColor()
                            onColorSelected(newC)
                            hexText = String.format("#%08X", newC)
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.testTag("saturation_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Value / Brightness Slider
                    Text(
                        text = "Brightness: ${(value * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = value,
                        onValueChange = {
                            value = it
                            val newC = computeCustomColor()
                            onColorSelected(newC)
                            hexText = String.format("#%08X", newC)
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.testTag("brightness_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Opacity / Alpha Slider
                    Text(
                        text = "Opacity: ${(alpha * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = alpha,
                        onValueChange = {
                            alpha = it
                            val newC = computeCustomColor()
                            onColorSelected(newC)
                            hexText = String.format("#%08X", newC)
                        },
                        valueRange = 0.05f..1f,
                        modifier = Modifier.testTag("opacity_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hex Input
                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { input ->
                            hexText = input
                            try {
                                if (input.startsWith("#") && (input.length == 7 || input.length == 9)) {
                                    val parsed = AndroidColor.parseColor(input)
                                    onColorSelected(parsed)
                                }
                            } catch (e: Exception) {
                                // Ignore typing invalid hex
                            }
                        },
                        label = { Text("Hex Color Code") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hex_color_input")
                    )
                }
            }
        }
    }
}

@Composable
fun ColorSwatch(
    color: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val composeColor = Color(color)
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(composeColor)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            val isLight = (AndroidColor.red(color) * 0.299 + AndroidColor.green(color) * 0.587 + AndroidColor.blue(color) * 0.114) > 186
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (isLight) Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
