package com.madtracking.app.presentation.addmedication

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.madtracking.app.domain.model.DosageUnit
import com.madtracking.app.domain.model.MealRelation
import com.madtracking.app.domain.model.MedicationForm
import com.madtracking.app.domain.model.MedicationImportance
import com.madtracking.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationWizardScreen(
    profileId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AddMedicationWizardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Set profile ID
    LaunchedEffect(profileId) {
        viewModel.setProfileId(profileId)
    }

    // Navigate back on save
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    // Handle back press
    BackHandler(enabled = uiState.canGoBack) {
        viewModel.goToPreviousStep()
    }

    val wizardSteps = remember {
        listOf(
            WizardStep(
                title = "Genel",
                icon = Icons.Outlined.Info,
                isCompleted = false,
                isCurrent = true
            ),
            WizardStep(
                title = "Dozaj",
                icon = Icons.Outlined.Star,
                isCompleted = false,
                isCurrent = false
            ),
            WizardStep(
                title = "Zaman",
                icon = Icons.Outlined.DateRange,
                isCompleted = false,
                isCurrent = false
            ),
            WizardStep(
                title = "Süre",
                icon = Icons.Outlined.Refresh,
                isCompleted = false,
                isCurrent = false
            ),
            WizardStep(
                title = "Özet",
                icon = Icons.Outlined.Check,
                isCompleted = false,
                isCurrent = false
            )
        )
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
                    IconButton(onClick = {
                        if (uiState.canGoBack) {
                            viewModel.goToPreviousStep()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = if (uiState.canGoBack) Icons.Default.ArrowBack else Icons.Default.Close,
                            contentDescription = if (uiState.canGoBack) "Geri" else "Kapat"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomActionBar(
                primaryText = if (uiState.isLastStep) "Kaydet" else "Devam Et",
                onPrimaryClick = {
                    if (uiState.isLastStep) {
                        viewModel.onSave()
                    } else {
                        viewModel.goToNextStep()
                    }
                },
                primaryEnabled = if (uiState.isLastStep) uiState.isAllValid else uiState.isCurrentStepValid,
                primaryLoading = uiState.isLoading,
                secondaryText = if (uiState.canGoBack) "Geri" else null,
                onSecondaryClick = if (uiState.canGoBack) {{ viewModel.goToPreviousStep() }} else null
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stepper Indicator
            StepperIndicator(
                steps = wizardSteps,
                currentStep = uiState.currentStep.index,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Linear Progress
            LinearStepProgress(
                currentStep = uiState.currentStep.index,
                totalSteps = com.madtracking.app.presentation.addmedication.WizardStep.totalSteps,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Content with animations
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    val direction = if (targetState.index > initialState.index) 1 else -1
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> direction * fullWidth },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(300)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -direction * fullWidth },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(300)
                    )
                },
                label = "wizard_content"
            ) { step ->
                when (step) {
                    com.madtracking.app.presentation.addmedication.WizardStep.GENERAL_INFO -> {
                        Step1GeneralInfo(
                            name = uiState.name,
                            form = uiState.form,
                            onNameChange = viewModel::onNameChange,
                            onFormChange = viewModel::onFormChange
                        )
                    }
                    com.madtracking.app.presentation.addmedication.WizardStep.DOSAGE -> {
                        Step2Dosage(
                            dosageAmount = uiState.dosageAmount,
                            dosageUnit = uiState.dosageUnit,
                            medicationForm = uiState.form,
                            onDosageAmountChange = viewModel::onDosageAmountChange,
                            onDosageUnitChange = viewModel::onDosageUnitChange
                        )
                    }
                    com.madtracking.app.presentation.addmedication.WizardStep.SCHEDULE -> {
                        Step3Schedule(
                            selectedTimes = uiState.selectedTimes,
                            mealRelation = uiState.mealRelation,
                            onTimeToggle = viewModel::toggleTime,
                            onTimeRemove = viewModel::removeTime,
                            onMealRelationChange = viewModel::onMealRelationChange
                        )
                    }
                    com.madtracking.app.presentation.addmedication.WizardStep.DURATION -> {
                        Step4Duration(
                            startDate = uiState.startDate,
                            isIndefinite = uiState.isIndefinite,
                            durationDays = uiState.durationDays,
                            endDateDisplay = uiState.getEndDateDisplay(),
                            startDateDisplay = uiState.getStartDateDisplay(),
                            onIndefiniteChange = viewModel::onIndefiniteChange,
                            onDurationDaysChange = viewModel::onDurationDaysChange
                        )
                    }
                    com.madtracking.app.presentation.addmedication.WizardStep.REVIEW -> {
                        Step5Review(
                            uiState = uiState,
                            onImportanceChange = viewModel::onImportanceChange,
                            onNotesChange = viewModel::onNotesChange
                        )
                    }
                }
            }
        }

        // Error Snackbar
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier
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

// ==================== Step 1: General Info ====================

@Composable
private fun Step1GeneralInfo(
    name: String,
    form: MedicationForm,
    onNameChange: (String) -> Unit,
    onFormChange: (MedicationForm) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        WizardSectionHeader(
            title = "İlaç Bilgileri",
            subtitle = "İlacın adını ve formunu seçin"
        )

        // Medication Name
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("İlaç Adı") },
            placeholder = { Text("örn: Aspirin, Parol") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                Text("💊", modifier = Modifier.padding(start = 8.dp))
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Form Selection
        Text(
            text = "İlaç Formu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MedicationForm.entries.take(6).forEach { formOption ->
                FormSelectionCard(
                    icon = getFormIcon(formOption),
                    title = getFormDisplayName(formOption),
                    isSelected = form == formOption,
                    onClick = { onFormChange(formOption) },
                    modifier = Modifier.width(100.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ==================== Step 2: Dosage ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step2Dosage(
    dosageAmount: String,
    dosageUnit: DosageUnit,
    medicationForm: MedicationForm,
    onDosageAmountChange: (String) -> Unit,
    onDosageUnitChange: (DosageUnit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        WizardSectionHeader(
            title = "Dozaj Bilgileri",
            subtitle = "Her kullanımda ne kadar alınacak?"
        )

        // Quick amount selection
        Text(
            text = "Hızlı Seçim",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("0.5", "1", "1.5", "2", "5", "10").forEach { amount ->
                AnimatedChip(
                    text = amount,
                    isSelected = dosageAmount == amount,
                    onClick = { onDosageAmountChange(amount) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Manual input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = dosageAmount,
                onValueChange = onDosageAmountChange,
                label = { Text("Miktar") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = dosageUnit.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Birim") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(),
                    shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DosageUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.displayName) },
                            onClick = {
                                onDosageUnitChange(unit)
                                expanded = false
                            },
                            leadingIcon = {
                                Text(getUnitIcon(unit))
                            }
                        )
                    }
                }
            }
        }

        // Preview
        val previewText = "${dosageAmount.ifEmpty { "0" }} ${dosageUnit.displayName}"
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = getFormIcon(medicationForm),
                    style = MaterialTheme.typography.headlineMedium
                )
                Column {
                    Text(
                        text = "Her kullanımda",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ==================== Step 3: Schedule ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step3Schedule(
    selectedTimes: List<String>,
    mealRelation: MealRelation,
    onTimeToggle: (String) -> Unit,
    onTimeRemove: (String) -> Unit,
    onMealRelationChange: (MealRelation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        WizardSectionHeader(
            title = "Alım Saatleri",
            subtitle = "Günde hangi saatlerde alınacak?"
        )

        // Selected times display
        if (selectedTimes.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Günde ${selectedTimes.size} kez",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "🕐",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedTimes.sorted().forEach { time ->
                            AnimatedChip(
                                text = time,
                                isSelected = true,
                                onClick = {},
                                onRemove = { onTimeRemove(time) }
                            )
                        }
                    }
                }
            }
        }

        // Quick time selection
        Text(
            text = "Saat Seç",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val commonTimes = listOf(
                "06:00", "07:00", "08:00", "09:00",
                "12:00", "13:00", "14:00",
                "18:00", "19:00", "20:00", "21:00", "22:00"
            )
            commonTimes.forEach { time ->
                AnimatedChip(
                    text = time,
                    isSelected = selectedTimes.contains(time),
                    onClick = { onTimeToggle(time) }
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Meal Relation
        Text(
            text = "Kullanım Talimatı",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MealRelation.entries.forEach { relation ->
                AnimatedCard(
                    onClick = { onMealRelationChange(relation) },
                    isSelected = mealRelation == relation
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = getMealRelationIcon(relation),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = getMealRelationTitle(relation),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = getMealRelationSubtitle(relation),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (mealRelation == relation) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ==================== Step 4: Duration ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step4Duration(
    startDate: java.time.LocalDate,
    isIndefinite: Boolean,
    durationDays: String,
    endDateDisplay: String,
    startDateDisplay: String,
    onIndefiniteChange: (Boolean) -> Unit,
    onDurationDaysChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        WizardSectionHeader(
            title = "Tedavi Süresi",
            subtitle = "Ne kadar süre kullanılacak?"
        )

        // Start Date Info
        InfoCard(
            icon = "📅",
            title = "Başlangıç Tarihi",
            value = startDateDisplay,
            modifier = Modifier.fillMaxWidth()
        )

        // Duration Options
        Text(
            text = "Kullanım Süresi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        // Indefinite Option
        AnimatedCard(
            onClick = { onIndefiniteChange(true) },
            isSelected = isIndefinite
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "♾️",
                    style = MaterialTheme.typography.headlineMedium
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Süresiz Kullanım",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Uzun dönem veya düzenli kullanım",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isIndefinite) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Specific Duration Option
        AnimatedCard(
            onClick = { onIndefiniteChange(false) },
            isSelected = !isIndefinite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "📆",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Belirli Süre",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Antibiyotik, kür tedavisi vb.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!isIndefinite) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Duration selection (only when not indefinite)
                AnimatedVisibility(
                    visible = !isIndefinite,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Quick duration chips
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "3" to "3 gün",
                                "5" to "5 gün",
                                "7" to "1 hafta",
                                "10" to "10 gün",
                                "14" to "2 hafta",
                                "21" to "3 hafta",
                                "30" to "1 ay"
                            ).forEach { (days, label) ->
                                AnimatedChip(
                                    text = label,
                                    isSelected = durationDays == days,
                                    onClick = { onDurationDaysChange(days) }
                                )
                            }
                        }

                        // Manual input
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = durationDays,
                                onValueChange = onDurationDaysChange,
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

                        // End date display
                        if (endDateDisplay.isNotEmpty()) {
                            InfoCard(
                                icon = "🏁",
                                title = "Bitiş Tarihi",
                                value = endDateDisplay,
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ==================== Step 5: Review ====================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step5Review(
    uiState: AddMedicationWizardState,
    onImportanceChange: (MedicationImportance) -> Unit,
    onNotesChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WizardSectionHeader(
            title = "Özet",
            subtitle = "Bilgileri kontrol edin ve kaydedin"
        )

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Medication Name & Form
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = uiState.getFormIcon(),
                        style = MaterialTheme.typography.displaySmall
                    )
                    Column {
                        Text(
                            text = uiState.name.ifEmpty { "İlaç Adı" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = uiState.getFormDisplayName(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                // Details Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryItem(
                        icon = "💊",
                        label = "Dozaj",
                        value = uiState.getDosageText()
                    )
                    SummaryItem(
                        icon = "🕐",
                        label = "Zamanlama",
                        value = uiState.getScheduleText()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryItem(
                        icon = "📅",
                        label = "Süre",
                        value = uiState.getDurationText()
                    )
                    SummaryItem(
                        icon = "🍽️",
                        label = "Talimat",
                        value = uiState.getMealRelationText()
                    )
                }

                // Times
                Text(
                    text = "Alım Saatleri",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.selectedTimes.sorted().forEach { time ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = time,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        // Importance Selection
        Text(
            text = "Önem Derecesi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MedicationImportance.entries.forEach { importance ->
                AnimatedCard(
                    onClick = { onImportanceChange(importance) },
                    isSelected = uiState.importance == importance,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = getImportanceIcon(importance),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getImportanceLabel(importance),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (uiState.importance == importance) 
                                FontWeight.Bold 
                            else 
                                FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Notes
        Text(
            text = "Notlar (Opsiyonel)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = onNotesChange,
            placeholder = { Text("Ekstra notlar, doktor önerileri...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SummaryItem(
    icon: String,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.titleMedium)
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ==================== Helper Functions ====================

private fun getFormIcon(form: MedicationForm): String = when (form) {
    MedicationForm.TABLET -> "💊"
    MedicationForm.CAPSULE -> "💊"
    MedicationForm.SYRUP -> "🧴"
    MedicationForm.DROP -> "💧"
    MedicationForm.INJECTION -> "💉"
    MedicationForm.CREAM -> "🧴"
    MedicationForm.SPRAY -> "💨"
    MedicationForm.POWDER -> "🧂"
    MedicationForm.OTHER -> "💊"
}

private fun getFormDisplayName(form: MedicationForm): String = when (form) {
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

private fun getUnitIcon(unit: DosageUnit): String = when (unit) {
    DosageUnit.TABLET -> "💊"
    DosageUnit.CAPSULE -> "💊"
    DosageUnit.ML -> "🧴"
    DosageUnit.DROP -> "💧"
    DosageUnit.SPOON -> "🥄"
}

private fun getMealRelationIcon(relation: MealRelation): String = when (relation) {
    MealRelation.BEFORE_MEAL -> "⏪"
    MealRelation.WITH_MEAL -> "🍽️"
    MealRelation.AFTER_MEAL -> "⏩"
    MealRelation.EMPTY_STOMACH -> "🌅"
    MealRelation.IRRELEVANT -> "✨"
}

private fun getMealRelationTitle(relation: MealRelation): String = when (relation) {
    MealRelation.BEFORE_MEAL -> "Yemekten Önce"
    MealRelation.WITH_MEAL -> "Yemekle Birlikte"
    MealRelation.AFTER_MEAL -> "Yemekten Sonra"
    MealRelation.EMPTY_STOMACH -> "Aç Karnına"
    MealRelation.IRRELEVANT -> "Farketmez"
}

private fun getMealRelationSubtitle(relation: MealRelation): String = when (relation) {
    MealRelation.BEFORE_MEAL -> "30-60 dakika önce"
    MealRelation.WITH_MEAL -> "Yemek sırasında"
    MealRelation.AFTER_MEAL -> "30-60 dakika sonra"
    MealRelation.EMPTY_STOMACH -> "Yemekten 1-2 saat önce"
    MealRelation.IRRELEVANT -> "Herhangi bir zamanda"
}

private fun getImportanceIcon(importance: MedicationImportance): String = when (importance) {
    MedicationImportance.CRITICAL -> "🔴"
    MedicationImportance.REGULAR -> "🟡"
    MedicationImportance.OPTIONAL -> "🟢"
}

private fun getImportanceLabel(importance: MedicationImportance): String = when (importance) {
    MedicationImportance.CRITICAL -> "Kritik"
    MedicationImportance.REGULAR -> "Normal"
    MedicationImportance.OPTIONAL -> "Opsiyonel"
}

