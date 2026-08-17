package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DamageReport
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DangerLight
import com.example.ui.theme.DangerRed
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessLight
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.SortField
import com.example.ui.viewmodel.SortOrder
import com.example.util.ReportExporter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportListSection(
    reports: List<DamageReport>,
    totalReportCount: Int,
    searchQuery: String,
    selectedStoreFilter: String,
    selectedStatusFilter: String,
    sortField: SortField,
    sortOrder: SortOrder,
    selectedIds: Set<Long>,
    onSearchChange: (String) -> Unit,
    onStoreFilterChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onSortChange: (SortField) -> Unit,
    onResetFilters: () -> Unit,
    onToggleSelect: (Long) -> Unit,
    onToggleSelectAll: (Boolean) -> Unit,
    onEditReport: (DamageReport) -> Unit,
    onDeleteReport: (DamageReport) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onShareSingleWhatsApp: (DamageReport) -> Unit,
    onShareAllWhatsApp: () -> Unit,
    onPrintPdf: () -> Unit,
    onExportCsv: () -> Unit,
    onBackupJson: () -> Unit,
    onImportJsonClick: () -> Unit,
    onClearAllClick: () -> Unit,
    onPreviewPhoto: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var moreMenuExpanded by remember { mutableStateOf(false) }
    var reportToDelete by remember { mutableStateOf<DamageReport?>(null) }
    var showClearAllConfirm by remember { mutableStateOf(false) }

    val allSelected = reports.isNotEmpty() && reports.all { selectedIds.contains(it.id) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Action Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "📋 Daftar Laporan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "(${reports.size} dari $totalReportCount laporan)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = onShareAllWhatsApp,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("send_wa_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedIds.isNotEmpty()) "Kirim WA (${selectedIds.size})" else "Kirim WA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onPrintPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("print_pdf_button")
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box {
                        IconButton(
                            onClick = { moreMenuExpanded = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("more_menu_button")
                        ) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu Lainnya")
                        }

                        DropdownMenu(
                            expanded = moreMenuExpanded,
                            onDismissRequest = { moreMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("📊 Export ke Excel (CSV)") },
                                leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null) },
                                onClick = {
                                    moreMenuExpanded = false
                                    onExportCsv()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("💾 Backup Data (JSON)") },
                                leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                                onClick = {
                                    moreMenuExpanded = false
                                    onBackupJson()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📥 Import Data (JSON)") },
                                leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) },
                                onClick = {
                                    moreMenuExpanded = false
                                    onImportJsonClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗑️ Hapus Semua Laporan", color = DangerRed) },
                                leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DangerRed) },
                                onClick = {
                                    moreMenuExpanded = false
                                    showClearAllConfirm = true
                                }
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari SKU, Store, Brand, atau Deskripsi...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Bersihkan pencarian")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Filter & Sort Row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Store Filter Chips
                val storeOptions = listOf("Semua Store" to "", "Collection" to "Collection", "Lacoste" to "Lacoste", "Sarinah" to "Sarinah")
                storeOptions.forEach { (label, value) ->
                    val selected = selectedStoreFilter == value
                    FilterChip(
                        selected = selected,
                        onClick = { onStoreFilterChange(value) },
                        label = { Text(label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealLight,
                            selectedLabelColor = TealDark
                        )
                    )
                }

                // Status Filter Chips
                val statusOptions = listOf("Semua Status" to "", "🆕 Baru" to "Baru", "⏳ Diproses" to "Diproses", "✅ Selesai" to "Selesai")
                statusOptions.forEach { (label, value) ->
                    val selected = selectedStatusFilter == value
                    FilterChip(
                        selected = selected,
                        onClick = { onStatusFilterChange(value) },
                        label = { Text(label, fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = InfoLight,
                            selectedLabelColor = InfoBlue
                        )
                    )
                }

                // Reset Button
                if (searchQuery.isNotBlank() || selectedStoreFilter.isNotBlank() || selectedStatusFilter.isNotBlank()) {
                    OutlinedButton(
                        onClick = onResetFilters,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 11.5.sp)
                    }
                }
            }

            // Sort Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { onToggleSelectAll(it) },
                        colors = CheckboxDefaults.colors(checkedColor = TealPrimary),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (selectedIds.isNotEmpty()) "${selectedIds.size} dipilih" else "Pilih Semua",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Urutkan:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    val sortButtons = listOf(
                        SortField.DATE to "Tanggal",
                        SortField.SKU to "SKU",
                        SortField.QTY to "Qty"
                    )

                    sortButtons.forEach { (field, label) ->
                        val isCurrent = sortField == field
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCurrent) TealLight else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onSortChange(field) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) TealDark else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isCurrent) {
                                    Icon(
                                        imageVector = if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = TealDark,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // List or Empty State
            if (reports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (totalReportCount == 0) "Belum ada laporan kerusakan." else "Tidak ada laporan yang sesuai filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        if (totalReportCount > 0) {
                            TextButton(onClick = onResetFilters) {
                                Text("Reset Filter")
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    reports.forEachIndexed { index, report ->
                        ReportCardItem(
                            index = index + 1,
                            report = report,
                            isSelected = selectedIds.contains(report.id),
                            onToggleSelect = { onToggleSelect(report.id) },
                            onEdit = { onEditReport(report) },
                            onDelete = { reportToDelete = report },
                            onUpdateStatus = { newStatus -> onUpdateStatus(report.id, newStatus) },
                            onShareWhatsApp = { onShareSingleWhatsApp(report) },
                            onPreviewPhoto = onPreviewPhoto
                        )
                    }
                }
            }
        }
    }

    // Delete Single Confirmation Dialog
    reportToDelete?.let { report ->
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            title = { Text("Hapus Laporan?") },
            text = { Text("Apakah Anda yakin ingin menghapus laporan SKU ${report.sku} (${report.itemDescription})?") },
            confirmButton = {
                Button(
                    onClick = {
                        val toDel = report
                        reportToDelete = null
                        onDeleteReport(toDel)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { reportToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text("⚠️ Hapus Semua Laporan?") },
            text = { Text("Tindakan ini akan menghapus semua $totalReportCount laporan secara permanen. Pastikan Anda sudah membuat Backup JSON jika diperlukan.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearAllConfirm = false
                        onClearAllClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Ya, Hapus Semua")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearAllConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun ReportCardItem(
    index: Int,
    report: DamageReport,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onShareWhatsApp: () -> Unit,
    onPreviewPhoto: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val photos = report.getPhotoList()
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TealLight.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row of Item Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(checkedColor = TealPrimary),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "#$index",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = report.reportId,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealDark
                    )
                }

                Text(
                    text = "${ReportExporter.formatDate(report.createdAt)}, ${ReportExporter.formatTime(report.createdAt)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // SKU, Store, Brand & Qty Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "SKU: ${report.sku}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = TealLight
                ) {
                    Text(
                        text = report.store,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                if (report.brand.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(30.dp),
                        color = InfoLight
                    ) {
                        Text(
                            text = report.brand,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InfoBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Qty: ${report.qty}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Item Name & Description
            Text(
                text = report.itemDescription,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Severity & Damage Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                val severityBg = when (report.severity) {
                    "Ringan" -> SuccessLight
                    "Sedang" -> AmberLight
                    else -> DangerLight
                }
                val severityText = when (report.severity) {
                    "Ringan" -> SuccessGreen
                    "Sedang" -> AmberDark
                    else -> DangerRed
                }

                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = severityBg
                ) {
                    Text(
                        text = report.severity,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = severityText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = report.damageDescription,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // Photo Gallery Thumbnails (if any)
            if (photos.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    photos.forEachIndexed { pIdx, pPath ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .clickable { onPreviewPhoto(photos, pIdx) }
                        ) {
                            AsyncImage(
                                model = pPath,
                                contentDescription = "Bukti foto ${pIdx + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Footer of Item: Status Selector & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Status Dropdown selector
                Box {
                    val (statusColor, statusBg) = when (report.status) {
                        "Baru" -> InfoBlue to InfoLight
                        "Diproses" -> AmberDark to AmberLight
                        else -> SuccessGreen to SuccessLight
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { statusMenuExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = when (report.status) {
                                    "Baru" -> "🆕 Baru"
                                    "Diproses" -> "⏳ Diproses"
                                    else -> "✅ Selesai"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Ganti Status",
                                tint = statusColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        listOf("Baru" to "🆕 Baru", "Diproses" to "⏳ Diproses", "Selesai" to "✅ Selesai").forEach { (st, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontWeight = if (report.status == st) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    statusMenuExpanded = false
                                    onUpdateStatus(st)
                                }
                            )
                        }
                    }
                }

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onShareWhatsApp,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SuccessLight)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Kirim ke WA", tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(InfoLight)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Laporan", tint = InfoBlue, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerLight)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Laporan", tint = DangerRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
