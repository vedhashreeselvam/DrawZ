package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawing_projects")
data class DrawingProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val thumbnailPath: String,
    val projectDataPath: String,
    val canvasWidth: Int = 1080,
    val canvasHeight: Int = 1080,
    val backgroundColor: Int = android.graphics.Color.WHITE,
    val strokeCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)
