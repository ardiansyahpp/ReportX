package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DamageReport
import kotlinx.coroutines.flow.Flow

@Dao
interface DamageReportDao {
    @Query("SELECT * FROM damage_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<DamageReport>>

    @Query("SELECT * FROM damage_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: Long): DamageReport?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: DamageReport): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reports: List<DamageReport>)

    @Update
    suspend fun updateReport(report: DamageReport)

    @Query("UPDATE damage_reports SET status = :newStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: String, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteReport(report: DamageReport)

    @Query("DELETE FROM damage_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)

    @Query("DELETE FROM damage_reports")
    suspend fun clearAll()
}
