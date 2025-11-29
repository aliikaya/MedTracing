package com.madtracking.app.presentation.addmedication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.madtracking.app.domain.model.DosageUnit
import com.madtracking.app.domain.model.MedicationForm
import com.madtracking.app.domain.model.MedicationImportance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Kayıt başarılıysa geri dön
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İlaç Ekle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onSave() },
                        enabled = uiState.isValid && !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Kaydet")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // İlaç adı
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("İlaç Adı") },
                    placeholder = { Text("örn: Aspirin") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Form seçimi
                FormDropdown(
                    selectedForm = uiState.form,
                    onFormSelected = { viewModel.onFormChange(it) }
                )

                // Doz bilgileri
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.dosageAmount,
                        onValueChange = { viewModel.onDosageAmountChange(it) },
                        label = { Text("Miktar") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    DosageUnitDropdown(
                        selectedUnit = uiState.dosageUnit,
                        onUnitSelected = { viewModel.onDosageUnitChange(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Saat girişi
                OutlinedTextField(
                    value = uiState.timesInput,
                    onValueChange = { viewModel.onTimesInputChange(it) },
                    label = { Text("Alım Saatleri") },
                    placeholder = { Text("08:00,14:00,20:00") },
                    supportingText = { Text("Virgülle ayırarak birden fazla saat girebilirsiniz") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Tedavi süresi bölümü
                DurationSection(
                    isIndefinite = uiState.isIndefinite,
                    durationDays = uiState.durationDays,
                    endDateDisplay = uiState.getEndDateDisplay(),
                    onIndefiniteChange = { viewModel.onIndefiniteChange(it) },
                    onDurationDaysChange = { viewModel.onDurationDaysChange(it) }
                )

                // Önem derecesi
                ImportanceDropdown(
                    selectedImportance = uiState.importance,
                    onImportanceSelected = { viewModel.onImportanceChange(it) }
                )

                // Notlar
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    label = { Text("Notlar (opsiyonel)") },
                    placeholder = { Text("Yemeklerden sonra al...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Kaydet butonu
                Button(
                    onClick = { viewModel.onSave() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isValid && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Kaydet")
                    }
                }
            }

            // Hata gösterimi
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Tamam")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationSection(
    isIndefinite: Boolean,
    durationDays: String,
    endDateDisplay: String,
    onIndefiniteChange: (Boolean) -> Unit,
    onDurationDaysChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tedavi Süresi",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Süresiz seçeneği
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isIndefinite,
                    onClick = { onIndefiniteChange(true) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Süresiz kullanım",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Uzun dönem / düzenli kullanım",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Belirli süre seçeneği
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !isIndefinite,
                    onClick = { onIndefiniteChange(false) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Belirli süre kullanacağım",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Gün sayısı girişi (sadece belirli süre seçiliyse göster)
            AnimatedVisibility(visible = !isIndefinite) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = durationDays,
                            onValueChange = onDurationDaysChange,
                            label = { Text("Gün sayısı") },
                            placeholder = { Text("7") },
                            modifier = Modifier.width(120.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Text(
                            text = "gün",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Hızlı seçim butonları
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 7, 10, 14, 21, 30).forEach { days ->
                            FilterChip(
                                selected = durationDays == days.toString(),
                                onClick = { onDurationDaysChange(days.toString()) },
                                label = { Text("$days") }
                            )
                        }
                    }

                    // Bitiş tarihi bilgisi
                    if (endDateDisplay.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📅", style = MaterialTheme.typography.titleMedium)
                                Column {
                                    Text(
                                        text = "Bitiş Tarihi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = endDateDisplay,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormDropdown(
    selectedForm: MedicationForm,
    onFormSelected: (MedicationForm) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedForm.toDisplayName(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Form") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MedicationForm.entries.forEach { form ->
                DropdownMenuItem(
                    text = { Text(form.toDisplayName()) },
                    onClick = {
                        onFormSelected(form)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DosageUnitDropdown(
    selectedUnit: DosageUnit,
    onUnitSelected: (DosageUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedUnit.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Birim") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DosageUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.displayName) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportanceDropdown(
    selectedImportance: MedicationImportance,
    onImportanceSelected: (MedicationImportance) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedImportance.toDisplayName(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Önem Derecesi") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MedicationImportance.entries.forEach { importance ->
                DropdownMenuItem(
                    text = { Text(importance.toDisplayName()) },
                    onClick = {
                        onImportanceSelected(importance)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun MedicationForm.toDisplayName(): String = when (this) {
    MedicationForm.TABLET -> "Tablet"
    MedicationForm.CAPSULE -> "Kapsül"
    MedicationForm.SYRUP -> "Şurup"
    MedicationForm.DROP -> "Damla"
    MedicationForm.INJECTION -> "Enjeksiyon"
    MedicationForm.CREAM -> "Krem"
    MedicationForm.SPRAY -> "Sprey"
    MedicationForm.POWDER -> "Toz"
    MedicationForm.OTHER -> "Diğer"
}

private fun MedicationImportance.toDisplayName(): String = when (this) {
    MedicationImportance.CRITICAL -> "🔴 Kritik"
    MedicationImportance.REGULAR -> "🟡 Normal"
    MedicationImportance.OPTIONAL -> "🟢 Opsiyonel"
}
