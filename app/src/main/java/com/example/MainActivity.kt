package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DashboardStatsSection
import com.example.ui.components.HeaderSection
import com.example.ui.components.PhotoGalleryDialog
import com.example.ui.components.ReminderDialog
import com.example.ui.components.ReportFormSection
import com.example.ui.components.ReportListSection
import com.example.ui.components.SupervisorPhoneDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.DamageReportViewModel
import com.example.ui.viewmodel.UiEffect
import com.example.util.ReportExporter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: DamageReportViewModel = viewModel()
            val systemDark = isSystemInDarkTheme()
            val customDarkState by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val effectiveDark = customDarkState ?: systemDark

            MyApplicationTheme(darkTheme = effectiveDark) {
                MainInspectionApp(viewModel = viewModel, isDarkTheme = effectiveDark)
            }
        }
    }
}

@Composable
fun MainInspectionApp(
    viewModel: DamageReportViewModel,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val allReports by viewModel.allReports.collectAsStateWithLifecycle()
    val filteredReports by viewModel.filteredReports.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val hasDraft by viewModel.hasDraft.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val storeFilter by viewModel.storeFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val sortField by viewModel.sortField.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val supervisorPhone by viewModel.supervisorPhone.collectAsStateWithLifecycle()
    val previewGallery by viewModel.previewGallery.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var reminderInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    // JSON import launcher
    val jsonImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importJsonFromUri(uri)
        }
    }

    // Handle UI Effects from ViewModel
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is UiEffect.ShowSnackbar -> {
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = effect.message,
                            actionLabel = effect.actionLabel,
                            duration = if (effect.actionLabel != null) SnackbarDuration.Short else SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            effect.onAction?.invoke()
                        }
                    }
                }
                is UiEffect.OpenWhatsApp -> {
                    ReportExporter.openWhatsApp(context, effect.text, effect.phone)
                    reminderInfo = Pair(
                        "📄 Jangan Lupa Kirim PDF!",
                        "Laporan ringkasan telah disiapkan di WhatsApp.\n\nSetelah kirim pesan WA, lampirkan dokumen PDF dari tombol PDF di aplikasi ini."
                    )
                }
                is UiEffect.ShareFile -> {
                    ReportExporter.shareFile(context, effect.file, effect.mimeType, effect.title)
                    if (effect.mimeType == "application/pdf") {
                        reminderInfo = Pair(
                            "💬 Jangan Lupa Kirim WA!",
                            "Setelah print atau simpan PDF, jangan lupa kirimkan ringkasan laporan ke WhatsApp Supervisor via tombol 'Kirim WA'."
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                HeaderSection(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleDarkTheme() },
                    onOpenSettings = { showSettingsDialog = true }
                )
            }

            // Dashboard Metric Cards
            item {
                DashboardStatsSection(stats = stats)
            }

            // Form Section
            item {
                ReportFormSection(
                    formState = formState,
                    hasDraft = hasDraft,
                    onSkuChange = { viewModel.onSkuChanged(it) },
                    onStoreChange = { viewModel.onStoreChanged(it) },
                    onBrandChange = { viewModel.onBrandChanged(it) },
                    onItemDescriptionChange = { viewModel.onItemDescriptionChanged(it) },
                    onQtyChange = { viewModel.onQtyChanged(it) },
                    onSeverityChange = { viewModel.onSeverityChanged(it) },
                    onStatusChange = { viewModel.onStatusChanged(it) },
                    onDamageDescriptionChange = { viewModel.onDamageDescriptionChanged(it) },
                    onPhotosSelected = { viewModel.addPhotosFromUris(it) },
                    onRemovePhoto = { viewModel.removePhoto(it) },
                    onRestoreDraft = { viewModel.restoreDraft() },
                    onDiscardDraft = { viewModel.discardDraft() },
                    onSubmit = { viewModel.submitReport() },
                    onCancelEdit = { viewModel.cancelEdit() },
                    onPreviewPhoto = { photos, idx -> viewModel.openPhotoGallery(photos, idx) }
                )
            }

            // Report List Section
            item {
                ReportListSection(
                    reports = filteredReports,
                    totalReportCount = allReports.size,
                    searchQuery = searchQuery,
                    selectedStoreFilter = storeFilter,
                    selectedStatusFilter = statusFilter,
                    sortField = sortField,
                    sortOrder = sortOrder,
                    selectedIds = selectedIds,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onStoreFilterChange = { viewModel.setStoreFilter(it) },
                    onStatusFilterChange = { viewModel.setStatusFilter(it) },
                    onSortChange = { viewModel.setSort(it) },
                    onResetFilters = { viewModel.resetFilters() },
                    onToggleSelect = { viewModel.toggleSelection(it) },
                    onToggleSelectAll = { viewModel.toggleSelectAll(it) },
                    onEditReport = { report ->
                        viewModel.editReport(report)
                        coroutineScope.launch {
                            listState.animateScrollToItem(2)
                        }
                    },
                    onDeleteReport = { viewModel.deleteReport(it) },
                    onUpdateStatus = { id, st -> viewModel.updateStatus(id, st) },
                    onShareSingleWhatsApp = { viewModel.shareWhatsApp(it) },
                    onShareAllWhatsApp = { viewModel.shareWhatsApp() },
                    onPrintPdf = { viewModel.exportPdf() },
                    onExportCsv = { viewModel.exportCsv() },
                    onBackupJson = { viewModel.backupJson() },
                    onImportJsonClick = { jsonImportLauncher.launch("application/json") },
                    onClearAllClick = { viewModel.clearAllReports() },
                    onPreviewPhoto = { photos, idx -> viewModel.openPhotoGallery(photos, idx) }
                )
            }

            // Footer Credit
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Dibuat oleh Ardiansyah Putra Pinem",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Sistem Laporan Operasional & Inspeksi v3.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    // Photo Gallery Preview Dialog
    previewGallery?.let { (photos, index) ->
        PhotoGalleryDialog(
            photos = photos,
            initialIndex = index,
            onDismiss = { viewModel.closePhotoGallery() }
        )
    }

    // Supervisor WhatsApp Phone Dialog
    if (showSettingsDialog) {
        SupervisorPhoneDialog(
            currentPhone = supervisorPhone,
            onPhoneSave = { viewModel.setSupervisorPhone(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Operational Reminder Dialog
    reminderInfo?.let { (title, message) ->
        ReminderDialog(
            title = title,
            message = message,
            onDismiss = { reminderInfo = null }
        )
    }
}
