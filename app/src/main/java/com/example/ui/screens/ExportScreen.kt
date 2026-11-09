package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ImageExporter
import com.example.viewmodel.DrawingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: DrawingViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var fileName by remember { mutableStateOf(uiState.title.replace("\\s+".toRegex(), "_")) }
    var selectedFormatIndex by remember { mutableIntStateOf(0) } // 0: PNG, 1: JPG, 2: JPEG
    var jpegQuality by remember { mutableFloatStateOf(95f) }

    var isExporting by remember { mutableStateOf(false) }
    var exportedUri by remember { mutableStateOf<Uri?>(null) }
    var exportMessage by remember { mutableStateOf<String?>(null) }

    // Render full artwork bitmap
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(uiState.strokes, uiState.backgroundColor) {
        withContext(Dispatchers.Default) {
            previewBitmap = viewModel.getExportBitmap()
        }
    }

    val formats = listOf(
        ImageExporter.ExportFormat.PNG,
        ImageExporter.ExportFormat.JPG,
        ImageExporter.ExportFormat.JPEG
    )
    val activeFormat = formats[selectedFormatIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Export Artwork",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("export_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Artwork Live Preview Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap!!.asImageBitmap(),
                            contentDescription = "Artwork Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    }
                }
            }

            // Canvas Specifications Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Resolution: ${uiState.canvasWidth} × ${uiState.canvasHeight} px",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${uiState.strokes.size} Strokes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // File Name Input
            OutlinedTextField(
                value = fileName,
                onValueChange = { fileName = it },
                label = { Text("File Name") },
                trailingIcon = { Text(".${activeFormat.extension}", modifier = Modifier.padding(end = 12.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_filename_input")
            )

            // Format Selection Tabs
            Column {
                Text(
                    text = "Export Format",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                TabRow(
                    selectedTabIndex = selectedFormatIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    formats.forEachIndexed { index, format ->
                        Tab(
                            selected = selectedFormatIndex == index,
                            onClick = { selectedFormatIndex = index },
                            text = { Text(format.extension.uppercase()) },
                            modifier = Modifier.testTag("export_format_tab_${format.extension}")
                        )
                    }
                }
            }

            // Quality Slider (for JPG and JPEG)
            if (activeFormat != ImageExporter.ExportFormat.PNG) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "JPEG Image Quality",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${jpegQuality.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = jpegQuality,
                        onValueChange = { jpegQuality = it },
                        valueRange = 50f..100f,
                        modifier = Modifier.testTag("export_quality_slider")
                    )
                }
            }

            // Export Result Notification Banner
            if (exportMessage != null) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = exportMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val bmp = previewBitmap
                        if (bmp == null) {
                            Toast.makeText(context, "Rendering artwork...", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isExporting = true
                        exportMessage = null
                        coroutineScope.launch {
                            try {
                                val uri = ImageExporter.saveToGallery(
                                    context = context,
                                    bitmap = bmp,
                                    fileName = fileName.ifBlank { "Artwork" },
                                    format = activeFormat,
                                    quality = jpegQuality.toInt()
                                )
                                exportedUri = uri
                                exportMessage = "Artwork saved to Device Gallery! (${activeFormat.extension.uppercase()})"
                                Toast.makeText(context, "Saved to Photos & Gallery!", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                exportMessage = "Export failed: ${e.message}"
                                Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isExporting = false
                            }
                        }
                    },
                    enabled = !isExporting && previewBitmap != null,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_to_gallery_button")
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Exporting Image...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save to Gallery",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Save to Device Gallery", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = {
                        val bmp = previewBitmap
                        if (bmp == null) return@OutlinedButton
                        coroutineScope.launch {
                            try {
                                val shareIntent = ImageExporter.shareArtwork(
                                    context = context,
                                    bitmap = bmp,
                                    title = fileName,
                                    format = activeFormat,
                                    quality = jpegQuality.toInt()
                                )
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = previewBitmap != null,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("share_artwork_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Artwork",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Share Artwork", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
