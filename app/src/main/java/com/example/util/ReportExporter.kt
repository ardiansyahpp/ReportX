package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.DamageReport
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale("id", "ID"))
    private val fullDateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))

    fun formatDate(timestamp: Long): String = dateFormatter.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = timeFormatter.format(Date(timestamp))
    fun formatFullDate(timestamp: Long): String = fullDateFormatter.format(Date(timestamp))

    /**
     * Copies an image from content URI into the internal storage directory
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val photosDir = File(context.filesDir, "report_photos").apply { if (!exists()) mkdirs() }
            val fileName = "img_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
            val destFile = File(photosDir, fileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input: InputStream ->
                val bitmap = BitmapFactory.decodeStream(input) ?: return null
                // Compress to max 1200px width with 80% JPEG quality
                val scaledBitmap = if (bitmap.width > 1200) {
                    val ratio = 1200f / bitmap.width
                    Bitmap.createScaledBitmap(bitmap, 1200, (bitmap.height * ratio).toInt(), true)
                } else {
                    bitmap
                }
                FileOutputStream(destFile).use { output ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Builds structured WhatsApp message text
     */
    fun buildWhatsAppMessage(reports: List<DamageReport>): String {
        if (reports.isEmpty()) return ""
        val sb = StringBuilder()
        sb.append("*📋 LAPORAN KERUSAKAN BARANG*\n")
        sb.append("*Total: ${reports.size} laporan*\n\n")

        reports.forEachIndexed { index, r ->
            val dateStr = formatDate(r.createdAt)
            val timeStr = formatTime(r.createdAt)
            sb.append("*${index + 1}.* $dateStr $timeStr\n")
            sb.append("📌 SKU: ${r.sku}\n")
            sb.append("🏪 Store: ${r.store}\n")
            if (r.brand.isNotBlank()) sb.append("🏷️ Brand: ${r.brand}\n")
            sb.append("📦 Barang: ${r.itemDescription}\n")
            sb.append("🔢 Qty: ${r.qty}\n")
            sb.append("🚦 Tingkat: ${r.severity}\n")
            sb.append("📍 Status: ${r.status}\n")
            sb.append("📝 Kerusakan: ${r.damageDescription}\n\n")
        }
        sb.append("_— Dikirim dari Sistem Laporan Operasional_")
        return sb.toString()
    }

    /**
     * Launches WhatsApp with pre-filled message
     */
    fun openWhatsApp(context: Context, text: String, phoneNumber: String = "6285179688760") {
        try {
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val cleanPhone = phoneNumber.replace("+", "").replace("-", "").replace(" ", "").trim()
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedText")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to standard share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Kirim Laporan via"))
        }
    }

    /**
     * Generates CSV string and saves to temporary file for sharing
     */
    fun generateCsvFile(context: Context, reports: List<DamageReport>): File {
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val file = File(exportDir, "laporan_kerusakan_${System.currentTimeMillis()}.csv")

        file.bufferedWriter().use { writer ->
            // UTF-8 BOM
            writer.write("\uFEFF")
            writer.write("Report ID,Tanggal,SKU,Store,Brand,Description,Qty,Tingkat Keparahan,Status,Damage Description\n")
            reports.forEach { r ->
                val line = listOf(
                    r.reportId,
                    formatFullDate(r.createdAt),
                    r.sku,
                    r.store,
                    r.brand,
                    r.itemDescription,
                    r.qty.toString(),
                    r.severity,
                    r.status,
                    r.damageDescription
                ).joinToString(",") { escapeCsv(it) }
                writer.write(line + "\n")
            }
        }
        return file
    }

    private fun escapeCsv(value: String): String {
        var escaped = value.replace("\"", "\"\"")
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"$escaped\""
        }
        return escaped
    }

    /**
     * Converts reports to JSON string
     */
    fun exportToJson(reports: List<DamageReport>): String {
        val array = JSONArray()
        reports.forEach { r ->
            val obj = JSONObject().apply {
                put("reportId", r.reportId)
                put("sku", r.sku)
                put("store", r.store)
                put("brand", r.brand)
                put("itemDescription", r.itemDescription)
                put("qty", r.qty)
                put("severity", r.severity)
                put("status", r.status)
                put("damageDescription", r.damageDescription)
                put("photoPaths", r.photoPaths)
                put("createdAt", r.createdAt)
                put("updatedAt", r.updatedAt)
            }
            array.put(obj)
        }
        return array.toString(2)
    }

    /**
     * Parses JSON string back into DamageReport list
     */
    fun parseJson(jsonString: String): List<DamageReport> {
        val reports = mutableListOf<DamageReport>()
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            reports.add(
                DamageReport(
                    reportId = obj.optString("reportId", DamageReport.createReportId()),
                    sku = obj.optString("sku", ""),
                    store = obj.optString("store", "Collection"),
                    brand = obj.optString("brand", ""),
                    itemDescription = obj.optString("itemDescription", ""),
                    qty = obj.optInt("qty", 1),
                    severity = obj.optString("severity", "Sedang"),
                    status = obj.optString("status", "Baru"),
                    photoPaths = obj.optString("photoPaths", ""),
                    damageDescription = obj.optString("damageDescription", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }
        return reports
    }

    /**
     * Creates a multi-page PDF document and saves it
     */
    fun generatePdfDocument(context: Context, reports: List<DamageReport>): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points

        val titlePaint = Paint().apply {
            color = Color.rgb(8, 79, 74) // TealDark
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.rgb(91, 100, 114)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(14, 107, 100)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(18, 24, 31)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(220, 224, 230)
            strokeWidth = 1f
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(225, 241, 239) // TealLight
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Draw Header on first page
        canvas.drawRect(20f, 20f, pageWidth - 20f, 65f, headerBgPaint)
        canvas.drawText("LAPORAN KERUSAKAN BARANG · SISTEM INSPEKSI", 30f, 42f, titlePaint)
        val metaText = "Dicetak pada: ${formatFullDate(System.currentTimeMillis())} · Total Laporan: ${reports.size}"
        canvas.drawText(metaText, 30f, 57f, subPaint)

        var y = 85f
        val startX = 25f

        // Table Header
        canvas.drawRect(startX, y - 12f, pageWidth - 25f, y + 6f, headerBgPaint)
        canvas.drawText("#", startX + 5f, y, headerPaint)
        canvas.drawText("Tanggal", startX + 25f, y, headerPaint)
        canvas.drawText("SKU", startX + 90f, y, headerPaint)
        canvas.drawText("Store", startX + 140f, y, headerPaint)
        canvas.drawText("Barang / Brand", startX + 205f, y, headerPaint)
        canvas.drawText("Qty", startX + 325f, y, headerPaint)
        canvas.drawText("Tingkat", startX + 355f, y, headerPaint)
        canvas.drawText("Status", startX + 410f, y, headerPaint)
        canvas.drawText("Kerusakan", startX + 460f, y, headerPaint)
        y += 18f

        reports.forEachIndexed { idx, item ->
            if (y > pageHeight - 50f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 40f

                // Re-draw table header on new page
                canvas.drawRect(startX, y - 12f, pageWidth - 25f, y + 6f, headerBgPaint)
                canvas.drawText("#", startX + 5f, y, headerPaint)
                canvas.drawText("Tanggal", startX + 25f, y, headerPaint)
                canvas.drawText("SKU", startX + 90f, y, headerPaint)
                canvas.drawText("Store", startX + 140f, y, headerPaint)
                canvas.drawText("Barang / Brand", startX + 205f, y, headerPaint)
                canvas.drawText("Qty", startX + 325f, y, headerPaint)
                canvas.drawText("Tingkat", startX + 355f, y, headerPaint)
                canvas.drawText("Status", startX + 410f, y, headerPaint)
                canvas.drawText("Kerusakan", startX + 460f, y, headerPaint)
                y += 18f
            }

            canvas.drawLine(startX, y - 12f, pageWidth - 25f, y - 12f, linePaint)

            canvas.drawText("${idx + 1}", startX + 5f, y, textPaint)
            canvas.drawText(formatDate(item.createdAt), startX + 25f, y, textPaint)
            canvas.drawText(item.sku, startX + 90f, y, textPaint)
            canvas.drawText(item.store, startX + 140f, y, textPaint)

            val itemDesc = if (item.brand.isNotBlank()) "${item.itemDescription} (${item.brand})" else item.itemDescription
            val truncatedDesc = if (itemDesc.length > 22) itemDesc.take(20) + ".." else itemDesc
            canvas.drawText(truncatedDesc, startX + 205f, y, textPaint)

            canvas.drawText("${item.qty}", startX + 325f, y, textPaint)
            canvas.drawText(item.severity, startX + 355f, y, textPaint)
            canvas.drawText(item.status, startX + 410f, y, textPaint)

            val dmgDesc = if (item.damageDescription.length > 18) item.damageDescription.take(16) + ".." else item.damageDescription
            canvas.drawText(dmgDesc, startX + 460f, y, textPaint)

            y += 20f
        }

        pdfDocument.finishPage(page)

        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val pdfFile = File(exportDir, "laporan_kerusakan_${System.currentTimeMillis()}.pdf")
        pdfFile.outputStream().use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        return pdfFile
    }

    /**
     * Shares a file via Android Share Sheet using FileProvider
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }
}
