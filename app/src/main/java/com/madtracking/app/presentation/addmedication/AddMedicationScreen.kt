package com.madtracking.app.presentation.addmedication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.madtracking.app.domain.model.DosageUnit
import com.madtracking.app.domain.model.MealRelation
import com.madtracking.app.domain.model.MedicationForm
import com.madtracking.app.domain.model.MedicationImportance
import com.madtracking.app.ui.components.BottomActionButton
import com.madtracking.app.ui.components.SectionCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationScreen(
    profileId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AddMedicationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ViewModel'e profileId'yi set et
    LaunchedEffect(profileId) {
        viewModel.setProfileId(profileId)
    }

    // Kayıt başarılıysa geri dön
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "İlaç Ekle",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomActionButton(
                text = "Kaydet",
                onClick = { viewModel.onSave() },
                enabled = uiState.isValid,
                isLoading = uiState.isLoading
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
                // === GENEL BİLGİLER ===
                SectionCard(title = "Genel Bilgiler") {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("İlaç Adı") },
                        placeholder = { Text("örn: Aspirin") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    FormDropdown(
                        selectedForm = uiState.form,
                        onFormSelected = { viewModel.onFormChange(it) }
                    )
                }

                // === DOZ & BİRİM ===
                SectionCard(title = "Doz Bilgileri") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.dosageAmount,
                            onValueChange = { viewModel.onDosageAmountChange(it) },
                            label = { Text("Miktar") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        DosageUnitDropdown(
                            selectedUnit = uiState.dosageUnit,
                            onUnitSelected = { viewModel.onDosageUnitChange(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // === ZAMANLAMA ===
                SectionCard(title = "Alım Saatleri") {
                    Text(
                        text = "Günde hangi saatlerde alınacak?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Seçili saatler chip olarak
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val times = uiState.timesInput.split(",").filter { it.isNotBlank() }
                        times.forEach { time ->
                            @OptIn(ExperimentalMaterial3Api::class)
                            InputChip(
                                selected = true,
                                onClick = { 
                                    val newTimes = times.filter { it != time }.joinToString(",")
                                    viewModel.onTimesInputChange(newTimes)
                                },
                                label = { Text(time.trim()) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Kaldır",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Hızlı saat ekleme
                    Text(
                        text = "Hızlı Ekle:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("08:00", "12:00", "14:00", "18:00", "20:00", "22:00").forEach { time ->
                            val isSelected = uiState.timesInput.contains(time)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    val currentTimes = uiState.timesInput.split(",").filter { it.isNotBlank() }.toMutableList()
                                    if (isSelected) {
                                        currentTimes.remove(time)
                                    } else {
                                        currentTimes.add(time)
                                    }
                                    viewModel.onTimesInputChange(currentTimes.sorted().joinToString(","))
                                },
                                label = { Text(time) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Manuel giriş
                    OutlinedTextField(
                        value = uiState.timesInput,
                        onValueChange = { viewModel.onTimesInputChange(it) },
                        label = { Text("Manuel Giriş") },
                        placeholder = { Text("08:00,14:00,20:00") },
                        supportingText = { Text("Virgülle ayırarak birden fazla saat girebilirsiniz") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // === TEDAVİ SÜRESİ ===
                SectionCard(title = "Tedavi Süresi") {
                    // Süresiz seçeneği
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.isIndefinite,
                            onClick = { viewModel.onIndefiniteChange(true) }
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Belirli süre seçeneği
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !uiState.isIndefinite,
                            onClick = { viewModel.onIndefiniteChange(false) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Belirli süre kullanacağım",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Gün sayısı girişi
                    AnimatedVisibility(visible = !uiState.isIndefinite) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 40.dp, top = 12.dp)
                        ) {
                            // Hızlı seçim
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5, 7, 10, 14, 21, 30).forEach { days ->
                                    FilterChip(
                                        selected = uiState.durationDays == days.toString(),
                                        onClick = { viewModel.onDurationDaysChange(days.toString()) },
                                        label = { Text("$days gün") }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.durationDays,
                                    onValueChange = { viewModel.onDurationDaysChange(it) },
                                    label = { Text("Gün sayısı") },
                                    modifier = Modifier.width(120.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Text(
                                    text = "gün",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            // Bitiş tarihi
                            if (uiState.getEndDateDisplay().isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
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
                                                text = uiState.getEndDateDisplay(),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // === KULLANIM TALİMATI ===
                SectionCard(title = "Kullanım Talimatı") {
                    MealRelation.entries.forEach { relation ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.mealRelation == relation,
                                onClick = { viewModel.onMealRelationChange(relation) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = relation.toShortDisplayText(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // === ÖNEM DERECESİ ===
                SectionCard(title = "Önem Derecesi") {
                    ImportanceSelector(
                        selectedImportance = uiState.importance,
                        onImportanceSelected = { viewModel.onImportanceChange(it) }
                    )
                }

                // === NOTLAR ===
                SectionCard(title = "Notlar (Opsiyonel)") {
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.onNotesChange(it) },
                        placeholder = { Text("Ekstra notlar...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Bottom padding
                Spacer(modifier = Modifier.height(80.dp))
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
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
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
            modifier = Modifier.menuAnchor(),
            shape = RoundedCornerShape(12.dp)
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
private fun ImportanceSelector(
    selectedImportance: MedicationImportance,
    onImportanceSelected: (MedicationImportance) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MedicationImportance.entries.forEach { importance ->
            val isSelected = selectedImportance == importance
            FilterChip(
                selected = isSelected,
                onClick = { onImportanceSelected(importance) },
                label = { 
                    Text(
                        text = importance.toDisplayName(),
                        style = MaterialTheme.typography.labelMedium
                    ) 
                },
                modifier = Modifier.weight(1f)
            )
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
