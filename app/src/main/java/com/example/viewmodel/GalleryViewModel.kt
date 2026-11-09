package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DrawingProject
import com.example.data.ImageExporter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val drawingDao = db.drawingDao()

    val projects: StateFlow<List<DrawingProject>> = drawingDao.getAllProjects()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    fun deleteProject(project: DrawingProject) {
        viewModelScope.launch {
            try {
                // Remove thumbnail file
                File(project.thumbnailPath).takeIf { it.exists() }?.delete()
                // Remove data file
                File(project.projectDataPath).takeIf { it.exists() }?.delete()
                // Remove from DB
                drawingDao.delete(project)
                _messageEvent.emit("Drawing deleted")
            } catch (e: Exception) {
                _messageEvent.emit("Failed to delete: ${e.message}")
            }
        }
    }

    fun renameProject(project: DrawingProject, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            try {
                drawingDao.update(
                    project.copy(
                        title = newTitle.trim(),
                        modifiedAt = System.currentTimeMillis()
                    )
                )
                _messageEvent.emit("Drawing renamed")
            } catch (e: Exception) {
                _messageEvent.emit("Failed to rename: ${e.message}")
            }
        }
    }

    fun duplicateProject(project: DrawingProject) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val newId = System.currentTimeMillis()

                // Copy JSON file
                val originalData = ImageExporter.loadProjectData(project.projectDataPath)
                val newProjectDataPath = ImageExporter.saveProjectData(context, newId, originalData)

                // Copy thumbnail file
                val originalThumb = File(project.thumbnailPath)
                val thumbDir = File(context.filesDir, "thumbnails").apply { if (!exists()) mkdirs() }
                val newThumbFile = File(thumbDir, "thumb_$newId.png")
                if (originalThumb.exists()) {
                    originalThumb.copyTo(newThumbFile, overwrite = true)
                }

                val duplicated = DrawingProject(
                    id = 0,
                    title = "${project.title} (Copy)",
                    thumbnailPath = newThumbFile.absolutePath,
                    projectDataPath = newProjectDataPath,
                    canvasWidth = project.canvasWidth,
                    canvasHeight = project.canvasHeight,
                    backgroundColor = project.backgroundColor,
                    strokeCount = project.strokeCount,
                    createdAt = System.currentTimeMillis(),
                    modifiedAt = System.currentTimeMillis()
                )

                drawingDao.insert(duplicated)
                _messageEvent.emit("Artwork duplicated!")
            } catch (e: Exception) {
                _messageEvent.emit("Failed to duplicate: ${e.message}")
            }
        }
    }
}
