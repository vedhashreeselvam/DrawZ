package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawing_projects ORDER BY modifiedAt DESC")
    fun getAllProjects(): Flow<List<DrawingProject>>

    @Query("SELECT * FROM drawing_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): DrawingProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: DrawingProject): Long

    @Update
    suspend fun update(project: DrawingProject)

    @Delete
    suspend fun delete(project: DrawingProject)

    @Query("DELETE FROM drawing_projects WHERE id = :id")
    suspend fun deleteById(id: Long)
}
