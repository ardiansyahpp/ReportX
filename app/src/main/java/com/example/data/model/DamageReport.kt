package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "damage_reports")
data class DamageReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reportId: String = "",
    val sku: String = "",
    val store: String = "Collection",
    val brand: String = "",
    val itemDescription: String = "",
    val qty: Int = 1,
    val severity: String = "Sedang", // Ringan, Sedang, Berat
    val status: String = "Baru",     // Baru, Diproses, Selesai
    val photoPaths: String = "",     // Comma-separated file paths or URIs
    val damageDescription: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getPhotoList(): List<String> {
        if (photoPaths.isBlank()) return emptyList()
        return photoPaths.split("|||").filter { it.isNotBlank() }
    }

    companion object {
        fun createReportId(timestamp: Long = System.currentTimeMillis()): String {
            val date = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
            val randomSuffix = (10000..99999).random()
            return "DMG-$date-$randomSuffix"
        }
    }
}
