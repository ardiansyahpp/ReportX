package com.example.data.repository

import com.example.data.local.DamageReportDao
import com.example.data.model.DamageReport
import kotlinx.coroutines.flow.Flow

class DamageReportRepository(private val dao: DamageReportDao) {

    val allReports: Flow<List<DamageReport>> = dao.getAllReports()

    suspend fun getReportById(id: Long): DamageReport? = dao.getReportById(id)

    suspend fun insertReport(report: DamageReport): Long = dao.insertReport(report)

    suspend fun updateReport(report: DamageReport) = dao.updateReport(report)

    suspend fun updateStatus(id: Long, newStatus: String) = dao.updateStatus(id, newStatus)

    suspend fun deleteReport(report: DamageReport) = dao.deleteReport(report)

    suspend fun deleteReportById(id: Long) = dao.deleteReportById(id)

    suspend fun clearAll() = dao.clearAll()

    suspend fun importReports(reports: List<DamageReport>) {
        dao.clearAll()
        dao.insertAll(reports)
    }
}
