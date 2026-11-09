package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.drawing.engine.DrawingRenderer
import com.example.drawing.model.DrawingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageExporter {

    enum class ExportFormat(val extension: String, val mimeType: String, val compressFormat: Bitmap.CompressFormat) {
        PNG("png", "image/png", Bitmap.CompressFormat.PNG),
        JPG("jpg", "image/jpeg", Bitmap.CompressFormat.JPEG),
        JPEG("jpeg", "image/jpeg", Bitmap.CompressFormat.JPEG)
    }

    /**
     * Renders items to full-resolution Bitmap.
     */
    fun createArtworkBitmap(
        width: Int,
        height: Int,
        backgroundColor: Int,
        items: List<DrawingItem>
    ): Bitmap {
        return DrawingRenderer.renderToBitmap(width, height, backgroundColor, items)
    }

    /**
     * Saves thumbnail to app-internal storage for gallery fast display.
     */
    suspend fun saveInternalThumbnail(
        context: Context,
        projectId: Long,
        bitmap: Bitmap
    ): String = withContext(Dispatchers.IO) {
        val thumbDir = File(context.filesDir, "thumbnails").apply { if (!exists()) mkdirs() }
        val thumbFile = File(thumbDir, "thumb_$projectId.png")

        // Scale down to max 400x400 maintaining aspect ratio
        val maxDim = 400
        val scale = if (bitmap.width > bitmap.height) {
            maxDim.toFloat() / bitmap.width
        } else {
            maxDim.toFloat() / bitmap.height
        }
        val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
        val thumbBitmap = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)

        FileOutputStream(thumbFile).use { out ->
            thumbBitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }
        if (thumbBitmap != bitmap) {
            thumbBitmap.recycle()
        }
        thumbFile.absolutePath
    }

    /**
     * Saves project stroke data JSON to app-internal storage.
     */
    suspend fun saveProjectData(
        context: Context,
        projectId: Long,
        jsonData: String
    ): String = withContext(Dispatchers.IO) {
        val projectDir = File(context.filesDir, "projects").apply { if (!exists()) mkdirs() }
        val projectFile = File(projectDir, "project_$projectId.json")
        projectFile.writeText(jsonData)
        projectFile.absolutePath
    }

    /**
     * Reads project stroke data JSON from storage.
     */
    suspend fun loadProjectData(projectPath: String): String = withContext(Dispatchers.IO) {
        val file = File(projectPath)
        if (file.exists()) {
            file.readText()
        } else {
            ""
        }
    }

    /**
     * Saves the artwork directly to Android Device Gallery using Scoped Storage / MediaStore.
     * Returns the saved content URI string or throws exception.
     */
    suspend fun saveToGallery(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        format: ExportFormat,
        quality: Int = 95
    ): Uri = withContext(Dispatchers.IO) {
        val cleanFileName = if (fileName.endsWith(".${format.extension}", ignoreCase = true)) {
            fileName
        } else {
            "$fileName.${format.extension}"
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, cleanFileName)
            put(MediaStore.Images.Media.MIME_TYPE, format.mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CreativePaintStudio")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("Failed to create MediaStore entry")

        var outputStream: OutputStream? = null
        try {
            outputStream = resolver.openOutputStream(uri)
                ?: throw IllegalStateException("Failed to open MediaStore output stream")

            val compressQuality = quality.coerceIn(10, 100)
            val success = bitmap.compress(format.compressFormat, compressQuality, outputStream)
            if (!success) {
                throw IllegalStateException("Failed to compress bitmap into output stream")
            }
        } finally {
            outputStream?.close()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        uri
    }

    /**
     * Prepares a shareable image file in cache and launches Android Share intent.
     */
    suspend fun shareArtwork(
        context: Context,
        bitmap: Bitmap,
        title: String,
        format: ExportFormat,
        quality: Int = 95
    ): Intent = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "shared_drawings").apply { if (!exists()) mkdirs() }
        val shareFile = File(cacheDir, "share_${System.currentTimeMillis()}.${format.extension}")

        FileOutputStream(shareFile).use { out ->
            bitmap.compress(format.compressFormat, quality.coerceIn(10, 100), out)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            shareFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Created with Creative Paint Studio 🎨")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        Intent.createChooser(shareIntent, "Share Artwork")
    }
}
