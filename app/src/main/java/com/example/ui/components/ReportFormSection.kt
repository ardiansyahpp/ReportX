package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DangerLight
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessLight
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.FormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFormSection(
    formState: FormState,
    hasDraft: Boolean,
    onSkuChange: (String) -> Unit,
    onStoreChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onItemDescriptionChange: (String) -> Unit,
    onQtyChange: (String) -> Unit,
    onSeverityChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onDamageDescriptionChange: (String) -> Unit,
    onPhotosSelected: (List<Uri>) -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onRestoreDraft: () -> Unit,
    onDiscardDraft: () -> Unit,
    onSubmit: () -> Unit,
    onCancelEdit: () -> Unit,
    onPreviewPhoto: (List<String>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onPhotosSelected(uris)
        }
    }

    var storeDropdownExpanded by remember { mutableStateOf(false) }
    val storeOptions = listOf("Collection", "Lacoste", "Sarinah", "Grand Indonesia", "Pondok Indah")

    var statusDropdownExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Baru", "Diproses", "Selesai")

    val animatedProgress by animateFloatAsState(
        targetValue = formState.progress,
        label = "progressAnim"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header of Form
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📝 Form Laporan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(30.dp),
                            color = if (formState.isEditing) AmberLight else TealLight
                        ) {
                            Text(
                                text = if (formState.isEditing) "Edit" else "Baru",
                                color = if (formState.isEditing) AmberDark else TealDark,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (formState.isEditing) {
                        Surface(
                            shape = RoundedCornerShape(30.dp),
                            color = AmberLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "✏️ Sedang Mengedit",
                                color = AmberDark,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Completion Progress Track
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (animatedProgress >= 1f) SuccessGreen else AmberPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Draft Banner
                AnimatedVisibility(
                    visible = hasDraft && !formState.isEditing,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AmberLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📝 Draft tersimpan ditemukan. Lanjutkan?",
                                color = AmberDark,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = onRestoreDraft,
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberDark),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Lanjutkan", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                                TextButton(
                                    onClick = onDiscardDraft,
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("Buang", color = AmberDark, fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }

                // Grid Inputs: SKU & Store
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = formState.sku,
                        onValueChange = onSkuChange,
                        label = { Text("SKU *") },
                        placeholder = { Text("Contoh: 10429") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sku_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenuBox(
                        expanded = storeDropdownExpanded,
                        onExpandedChange = { storeDropdownExpanded = !storeDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = formState.store,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Store *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = storeDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("store_dropdown"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = storeDropdownExpanded,
                            onDismissRequest = { storeDropdownExpanded = false }
                        ) {
                            storeOptions.forEach { store ->
                                DropdownMenuItem(
                                    text = { Text(store) },
                                    onClick = {
                                        onStoreChange(store)
                                        storeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Brand (Optional)
                OutlinedTextField(
                    value = formState.brand,
                    onValueChange = onBrandChange,
                    label = { Text("Brand (opsional)") },
                    placeholder = { Text("Contoh: Lacoste / Nike / Zara") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("brand_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Item Description (Required)
                OutlinedTextField(
                    value = formState.itemDescription,
                    onValueChange = onItemDescriptionChange,
                    label = { Text("Description / Nama Barang *") },
                    placeholder = { Text("Contoh: Kemeja Polo Pria Navy Size L") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_description_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Qty & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = formState.qty,
                        onValueChange = onQtyChange,
                        label = { Text("Qty Rusak *") },
                        placeholder = { Text("1") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("qty_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    ExposedDropdownMenuBox(
                        expanded = statusDropdownExpanded,
                        onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = formState.status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("status_dropdown"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            statusOptions.forEach { st ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (st) {
                                                "Baru" -> "🆕 Baru"
                                                "Diproses" -> "⏳ Diproses"
                                                else -> "✅ Selesai"
                                            }
                                        )
                                    },
                                    onClick = {
                                        onStatusChange(st)
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Tingkat Keparahan / Severity Pills
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tingkat Keparahan",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val levels = listOf(
                            Triple("Ringan", "🟢 Ringan", SuccessLight),
                            Triple("Sedang", "🟡 Sedang", AmberLight),
                            Triple("Berat", "🔴 Berat", DangerLight)
                        )

                        levels.forEach { (level, label, activeBg) ->
                            val isActive = formState.severity == level
                            val activeBorder = when (level) {
                                "Ringan" -> SuccessGreen
                                "Sedang" -> AmberPrimary
                                else -> DangerRed
                            }
                            val activeText = when (level) {
                                "Ringan" -> SuccessGreen
                                "Sedang" -> AmberDark
                                else -> DangerRed
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isActive) activeBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = if (isActive) 1.8.dp else 1.dp,
                                        color = if (isActive) activeBorder else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSeverityChange(level) }
                                    .padding(vertical = 12.dp)
                                    .testTag("severity_pill_$level"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isActive) activeText else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Photo Upload Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📸 Foto Kerusakan (maks. 3)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formState.photos.size}/3 foto",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Dropzone / Picker Box
                    if (formState.photos.size < 3) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    photoPickerLauncher.launch("image/*")
                                }
                                .padding(vertical = 16.dp, horizontal = 12.dp)
                                .testTag("photo_dropzone"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Upload Foto",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "Ketuk untuk memilih foto bukti kerusakan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Otomatis dikompres & disimpan secara aman",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Thumbnails list
                    if (formState.photos.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            formState.photos.forEachIndexed { index, path ->
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                        .clickable { onPreviewPhoto(formState.photos, index) }
                                ) {
                                    AsyncImage(
                                        model = path,
                                        contentDescription = "Foto kerusakan ${index + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Remove button overlay
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .clickable { onRemovePhoto(index) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus foto",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Damage Description Textarea
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = formState.damageDescription,
                        onValueChange = onDamageDescriptionChange,
                        label = { Text("Damage Description / Uraian Kerusakan *") },
                        placeholder = { Text("Deskripsikan bagian yang rusak, noda, sobek, jahitan lepas, dsb.") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("damage_description_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Text(
                        text = "${formState.damageDescription.length}/500 karakter",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                // Form Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (formState.isEditing) {
                        OutlinedButton(
                            onClick = onCancelEdit,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("cancel_edit_button")
                        ) {
                            Text("↩️ Batal", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onSubmit,
                        enabled = !formState.isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (formState.isEditing) AmberDark else TealPrimary
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("submit_report_button")
                    ) {
                        if (formState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Menyimpan...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = if (formState.isEditing) Icons.Default.Edit else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (formState.isEditing) "Update Laporan" else "Tambahkan Laporan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp
                            )
                        }
                    }
                }
            }

            // Success Stamp Overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = formState.showSuccessStamp,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(4.dp, SuccessGreen, CircleShape)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(42.dp)
                            )
                            Text(
                                text = "TERSIMPAN",
                                color = SuccessGreen,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
