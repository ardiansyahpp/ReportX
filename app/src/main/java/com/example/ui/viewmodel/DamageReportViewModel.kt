package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DamageReport
import com.example.data.repository.DamageReportRepository
import com.example.util.ReportExporter
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormState(
    val editingId: Long? = null,
    val reportId: String = "",
    val sku: String = "",
    val store: String = "Collection",
    val brand: String = "",
    val itemDescription: String = "",
    val qty: String = "1",
    val severity: String = "Sedang",
    val status: String = "Baru",
    val photos: List<String> = emptyList(),
    val damageDescription: String = "",
    val isSaving: Boolean = false,
    val showSuccessStamp: Boolean = false
) {
    val isEditing: Boolean get() = editingId != null
    val progress: Float
        get() {
            var filled = 0
            if (sku.isNotBlank()) filled++
            if (itemDescription.isNotBlank()) filled++
            if (qty.isNotBlank() && (qty.toIntOrNull() ?: 0) > 0) filled++
            if (damageDescription.isNotBlank()) filled++
            return filled / 4f
        }
    val hasContent: Boolean
        get() = sku.isNotBlank() || brand.isNotBlank() || itemDescription.isNotBlank() ||
                damageDescription.isNotBlank() || photos.isNotEmpty()
}

data class DashboardStats(
    val totalReports: Int = 0,
    val totalQty: Int = 0,
    val withPhotosCount: Int = 0,
    val pendingCount: Int = 0
)

enum class SortField { DATE, SKU, QTY }
enum class SortOrder { ASCENDING, DESCENDING }

sealed class UiEffect {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null, val onAction: (() -> Unit)? = null) : UiEffect()
    data class OpenWhatsApp(val text: String, val phone: String) : UiEffect()
    data class ShareFile(val file: java.io.File, val mimeType: String, val title: String) : UiEffect()
}

class DamageReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DamageReportRepository
    val allReports: StateFlow<List<DamageReport>>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _storeFilter = MutableStateFlow("")
    val storeFilter = _storeFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow("")
    val statusFilter = _statusFilter.asStateFlow()

    private val _sortField = MutableStateFlow(SortField.DATE)
    val sortField = _sortField.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DESCENDING)
    val sortOrder = _sortOrder.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    private val _formState = MutableStateFlow(FormState())
    val formState = _formState.asStateFlow()

    private val _hasDraft = MutableStateFlow(false)
    val hasDraft = _hasDraft.asStateFlow()

    private var draftBackup: FormState? = null
    private var lastDeletedReport: DamageReport? = null

    private val _previewGallery = MutableStateFlow<Pair<List<String>, Int>?>(null)
    val previewGallery = _previewGallery.asStateFlow()

    private val _effects = MutableSharedFlow<UiEffect>()
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    private val _supervisorPhone = MutableStateFlow("6285179688760")
    val supervisorPhone = _supervisorPhone.asStateFlow()

    private val _isDarkTheme = MutableStateFlow<Boolean?>(null)
    val isDarkTheme = _isDarkTheme.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = DamageReportRepository(db.damageReportDao())
        allReports = repository.allReports
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        checkDraft()
    }

    data class FilterParams(
        val query: String,
        val store: String,
        val status: String,
        val sortField: SortField,
        val sortOrder: SortOrder
    )

    private val filterParams = combine(
        _searchQuery,
        _storeFilter,
        _statusFilter,
        _sortField,
        _sortOrder
    ) { query, store, status, sortField, sortOrder ->
        FilterParams(query, store, status, sortField, sortOrder)
    }

    val filteredReports: StateFlow<List<DamageReport>> = combine(
        allReports,
        filterParams
    ) { reports, params ->
        val query = params.query
        val store = params.store
        val status = params.status
        val sortField = params.sortField
        val sortOrder = params.sortOrder

        var list = reports.filter { report ->
            val matchesQuery = query.isBlank() ||
                    report.reportId.contains(query, ignoreCase = true) ||
                    report.sku.contains(query, ignoreCase = true) ||
                    report.store.contains(query, ignoreCase = true) ||
                    report.brand.contains(query, ignoreCase = true) ||
                    report.itemDescription.contains(query, ignoreCase = true) ||
                    report.damageDescription.contains(query, ignoreCase = true)

            val matchesStore = store.isBlank() || report.store.equals(store, ignoreCase = true)
            val matchesStatus = status.isBlank() || report.status.equals(status, ignoreCase = true)

            matchesQuery && matchesStore && matchesStatus
        }

        list = when (sortField) {
            SortField.DATE -> if (sortOrder == SortOrder.DESCENDING) list.sortedByDescending { it.createdAt } else list.sortedBy { it.createdAt }
            SortField.SKU -> if (sortOrder == SortOrder.DESCENDING) list.sortedByDescending { it.sku.toLongOrNull() ?: 0L } else list.sortedBy { it.sku.toLongOrNull() ?: 0L }
            SortField.QTY -> if (sortOrder == SortOrder.DESCENDING) list.sortedByDescending { it.qty } else list.sortedBy { it.qty }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<DashboardStats> = allReports.combine(_searchQuery) { reports, _ ->
        val total = reports.size
        val totalQty = reports.sumOf { it.qty }
        val withPhotos = reports.count { it.photoPaths.isNotBlank() }
        val pending = reports.count { !it.status.equals("Selesai", ignoreCase = true) }
        DashboardStats(total, totalQty, withPhotos, pending)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    fun toggleDarkTheme() {
        _isDarkTheme.update { current ->
            if (current == null) true else !current
        }
    }

    fun setSupervisorPhone(phone: String) {
        _supervisorPhone.value = phone
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStoreFilter(store: String) {
        _storeFilter.value = store
    }

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun setSort(field: SortField) {
        if (_sortField.value == field) {
            _sortOrder.value = if (_sortOrder.value == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
        } else {
            _sortField.value = field
            _sortOrder.value = SortOrder.DESCENDING
        }
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _storeFilter.value = ""
        _statusFilter.value = ""
        _sortField.value = SortField.DATE
        _sortOrder.value = SortOrder.DESCENDING
    }

    fun toggleSelection(reportId: Long) {
        _selectedIds.update { current ->
            if (current.contains(reportId)) current - reportId else current + reportId
        }
    }

    fun toggleSelectAll(selectAll: Boolean) {
        if (selectAll) {
            _selectedIds.value = filteredReports.value.map { it.id }.toSet()
        } else {
            _selectedIds.value = emptySet()
        }
    }

    // --- FORM ACTIONS ---

    fun onSkuChanged(sku: String) {
        _formState.update { it.copy(sku = sku.filter { c -> c.isDigit() }) }
        saveDraft()
    }

    fun onStoreChanged(store: String) {
        _formState.update { it.copy(store = store) }
        saveDraft()
    }

    fun onBrandChanged(brand: String) {
        _formState.update { it.copy(brand = brand) }
        saveDraft()
    }

    fun onItemDescriptionChanged(desc: String) {
        _formState.update { it.copy(itemDescription = desc) }
        saveDraft()
    }

    fun onQtyChanged(qty: String) {
        _formState.update { it.copy(qty = qty.filter { c -> c.isDigit() }) }
        saveDraft()
    }

    fun onSeverityChanged(severity: String) {
        _formState.update { it.copy(severity = severity) }
        saveDraft()
    }

    fun onStatusChanged(status: String) {
        _formState.update { it.copy(status = status) }
        saveDraft()
    }

    fun onDamageDescriptionChanged(desc: String) {
        if (desc.length <= 500) {
            _formState.update { it.copy(damageDescription = desc) }
            saveDraft()
        }
    }

    fun addPhotosFromUris(uris: List<Uri>) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val currentPhotos = _formState.value.photos.toMutableList()
            for (uri in uris) {
                if (currentPhotos.size >= 3) break
                val savedPath = ReportExporter.saveImageToInternalStorage(context, uri)
                if (savedPath != null) {
                    currentPhotos.add(savedPath)
                }
            }
            _formState.update { it.copy(photos = currentPhotos) }
            saveDraft()
        }
    }

    fun removePhoto(index: Int) {
        val currentPhotos = _formState.value.photos.toMutableList()
        if (index in currentPhotos.indices) {
            currentPhotos.removeAt(index)
            _formState.update { it.copy(photos = currentPhotos) }
            saveDraft()
        }
    }

    fun editReport(report: DamageReport) {
        _formState.value = FormState(
            editingId = report.id,
            reportId = report.reportId,
            sku = report.sku,
            store = report.store,
            brand = report.brand,
            itemDescription = report.itemDescription,
            qty = report.qty.toString(),
            severity = report.severity,
            status = report.status,
            photos = report.getPhotoList(),
            damageDescription = report.damageDescription
        )
    }

    fun cancelEdit() {
        resetForm()
    }

    fun resetForm() {
        _formState.value = FormState()
        clearDraft()
    }

    private fun checkDraft() {
        val sp = getApplication<Application>().getSharedPreferences("report_draft", Application.MODE_PRIVATE)
        val raw = sp.getString("draft_json", null)
        if (!raw.isNullOrBlank()) {
            try {
                val obj = org.json.JSONObject(raw)
                draftBackup = FormState(
                    sku = obj.optString("sku", ""),
                    store = obj.optString("store", "Collection"),
                    brand = obj.optString("brand", ""),
                    itemDescription = obj.optString("itemDescription", ""),
                    qty = obj.optString("qty", "1"),
                    severity = obj.optString("severity", "Sedang"),
                    status = obj.optString("status", "Baru"),
                    damageDescription = obj.optString("damageDescription", "")
                )
                _hasDraft.value = true
            } catch (e: Exception) {
                _hasDraft.value = false
            }
        }
    }

    private fun saveDraft() {
        if (_formState.value.isEditing) return
        val state = _formState.value
        val sp = getApplication<Application>().getSharedPreferences("report_draft", Application.MODE_PRIVATE)
        if (state.hasContent) {
            val obj = org.json.JSONObject().apply {
                put("sku", state.sku)
                put("store", state.store)
                put("brand", state.brand)
                put("itemDescription", state.itemDescription)
                put("qty", state.qty)
                put("severity", state.severity)
                put("status", state.status)
                put("damageDescription", state.damageDescription)
            }
            sp.edit().putString("draft_json", obj.toString()).apply()
        } else {
            sp.edit().remove("draft_json").apply()
        }
    }

    fun restoreDraft() {
        draftBackup?.let { draft ->
            _formState.value = draft
            _hasDraft.value = false
            viewModelScope.launch {
                _effects.emit(UiEffect.ShowSnackbar("Draft formulir berhasil dipulihkan."))
            }
        }
    }

    fun discardDraft() {
        clearDraft()
        _hasDraft.value = false
    }

    private fun clearDraft() {
        val sp = getApplication<Application>().getSharedPreferences("report_draft", Application.MODE_PRIVATE)
        sp.edit().remove("draft_json").apply()
        draftBackup = null
    }

    fun submitReport() {
        val state = _formState.value
        val sku = state.sku.trim()
        val itemDesc = state.itemDescription.trim()
        val qtyNum = state.qty.toIntOrNull() ?: 0
        val damageDesc = state.damageDescription.trim()

        if (sku.isBlank() || itemDesc.isBlank() || qtyNum < 1 || damageDesc.isBlank()) {
            viewModelScope.launch {
                _effects.emit(UiEffect.ShowSnackbar("Semua field bertanda * wajib diisi dengan benar!"))
            }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            val photoPathsString = state.photos.joinToString("|||")

            if (state.isEditing && state.editingId != null) {
                val updated = DamageReport(
                    id = state.editingId,
                    reportId = if (state.reportId.isNotBlank()) state.reportId else DamageReport.createReportId(),
                    sku = sku,
                    store = state.store,
                    brand = state.brand.trim(),
                    itemDescription = itemDesc,
                    qty = qtyNum,
                    severity = state.severity,
                    status = state.status,
                    photoPaths = photoPathsString,
                    damageDescription = damageDesc,
                    createdAt = now, // or keep existing if found
                    updatedAt = now
                )
                repository.updateReport(updated)
                _formState.update { it.copy(isSaving = false, showSuccessStamp = true) }
                _effects.emit(UiEffect.ShowSnackbar("Laporan berhasil diperbarui!"))
            } else {
                val newReport = DamageReport(
                    reportId = DamageReport.createReportId(now),
                    sku = sku,
                    store = state.store,
                    brand = state.brand.trim(),
                    itemDescription = itemDesc,
                    qty = qtyNum,
                    severity = state.severity,
                    status = state.status,
                    photoPaths = photoPathsString,
                    damageDescription = damageDesc,
                    createdAt = now,
                    updatedAt = now
                )
                repository.insertReport(newReport)
                _formState.update { it.copy(isSaving = false, showSuccessStamp = true) }
                _effects.emit(UiEffect.ShowSnackbar("Laporan berhasil disimpan!"))
            }

            clearDraft()
            _hasDraft.value = false
            // Reset form after short delay
            kotlinx.coroutines.delay(1000)
            resetForm()
        }
    }

    fun updateStatus(reportId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateStatus(reportId, newStatus)
            _effects.emit(UiEffect.ShowSnackbar("Status diperbarui menjadi \"$newStatus\"."))
        }
    }

    fun deleteReport(report: DamageReport) {
        viewModelScope.launch {
            lastDeletedReport = report
            repository.deleteReport(report)
            if (_formState.value.editingId == report.id) {
                resetForm()
            }
            _effects.emit(
                UiEffect.ShowSnackbar(
                    message = "Laporan ${report.sku} dihapus.",
                    actionLabel = "Urungkan",
                    onAction = { undoDelete() }
                )
            )
        }
    }

    fun undoDelete() {
        val deleted = lastDeletedReport ?: return
        viewModelScope.launch {
            repository.insertReport(deleted)
            lastDeletedReport = null
            _effects.emit(UiEffect.ShowSnackbar("Penghapusan dibatalkan."))
        }
    }

    fun clearAllReports() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedIds.value = emptySet()
            if (_formState.value.isEditing) resetForm()
            _effects.emit(UiEffect.ShowSnackbar("Semua laporan telah dihapus."))
        }
    }

    // --- PHOTO VIEWER ---
    fun openPhotoGallery(photos: List<String>, initialIndex: Int = 0) {
        _previewGallery.value = Pair(photos, initialIndex)
    }

    fun closePhotoGallery() {
        _previewGallery.value = null
    }

    // --- EXPORT & SHARE ---

    fun getSelectedOrAllReports(): List<DamageReport> {
        val all = allReports.value
        val selected = _selectedIds.value
        return if (selected.isNotEmpty()) {
            all.filter { selected.contains(it.id) }
        } else {
            all
        }
    }

    fun shareWhatsApp(specificReport: DamageReport? = null) {
        viewModelScope.launch {
            val list = if (specificReport != null) listOf(specificReport) else getSelectedOrAllReports()
            if (list.isEmpty()) {
                _effects.emit(UiEffect.ShowSnackbar("Tidak ada laporan untuk dikirim!"))
                return@launch
            }
            val text = ReportExporter.buildWhatsAppMessage(list)
            _effects.emit(UiEffect.OpenWhatsApp(text, _supervisorPhone.value))
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            val list = getSelectedOrAllReports()
            if (list.isEmpty()) {
                _effects.emit(UiEffect.ShowSnackbar("Belum ada data untuk diexport!"))
                return@launch
            }
            val context = getApplication<Application>()
            val csvFile = ReportExporter.generateCsvFile(context, list)
            _effects.emit(UiEffect.ShareFile(csvFile, "text/csv", "Export Laporan Kerusakan (CSV)"))
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            val list = getSelectedOrAllReports()
            if (list.isEmpty()) {
                _effects.emit(UiEffect.ShowSnackbar("Belum ada data untuk dicetak PDF!"))
                return@launch
            }
            val context = getApplication<Application>()
            val pdfFile = ReportExporter.generatePdfDocument(context, list)
            _effects.emit(UiEffect.ShareFile(pdfFile, "application/pdf", "Cetak / Bagikan Laporan PDF"))
        }
    }

    fun backupJson() {
        viewModelScope.launch {
            val list = allReports.value
            if (list.isEmpty()) {
                _effects.emit(UiEffect.ShowSnackbar("Belum ada data untuk dibackup!"))
                return@launch
            }
            val jsonText = ReportExporter.exportToJson(list)
            val context = getApplication<Application>()
            val exportDir = java.io.File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
            val file = java.io.File(exportDir, "backup_laporan_${System.currentTimeMillis()}.json")
            file.writeText(jsonText)
            _effects.emit(UiEffect.ShareFile(file, "application/json", "Backup Data Laporan (JSON)"))
        }
    }

    fun importJsonFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (jsonString.isNullOrBlank()) {
                    _effects.emit(UiEffect.ShowSnackbar("File JSON kosong atau tidak dapat dibaca."))
                    return@launch
                }
                val reports = ReportExporter.parseJson(jsonString)
                if (reports.isEmpty()) {
                    _effects.emit(UiEffect.ShowSnackbar("Tidak ditemukan data laporan yang valid dalam JSON."))
                    return@launch
                }
                repository.importReports(reports)
                _effects.emit(UiEffect.ShowSnackbar("Berhasil mengimpor ${reports.size} laporan!"))
            } catch (e: Exception) {
                _effects.emit(UiEffect.ShowSnackbar("Gagal import: ${e.localizedMessage ?: "Format tidak sesuai"}"))
            }
        }
    }
}
