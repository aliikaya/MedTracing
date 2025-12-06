package com.medtracking.app.presentation.medications

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medtracking.app.domain.model.Medication
import com.medtracking.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(
    profileId: Long,
    onNavigateToAddMedication: () -> Unit,
    onNavigateToMedicationHistory: (Long) -> Unit,
    viewModel: MedicationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    // FAB visibility based on scroll
    val showFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 || 
            listState.firstVisibleItemScrollOffset < 100
        }
    }
    
    LaunchedEffect(profileId) {
        viewModel.loadMedications(profileId)
    }

    Scaffold(
        topBar = {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            text = "İlaçlarım",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }
        },
        floatingActionButton = {
            AnimatedFAB(
                onClick = onNavigateToAddMedication,
                visible = showFab,
                icon = Icons.Default.Add,
                contentDescription = "İlaç Ekle"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.medications.isEmpty() -> {
                    EmptyState(
                        icon = "💊",
                        title = "Henüz ilaç eklenmemiş",
                        subtitle = "İlaç eklemek için aşağıdaki butona tıklayın",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = uiState.medications,
                            key = { _, medication -> medication.id }
                        ) { index, medication ->
                            AnimatedListItem(index = index) {
                                MedicationCard(
                                    medication = medication,
                                    onClick = { onNavigateToMedicationHistory(medication.id) }
                                )
                            }
                        }
                        
                        // FAB için boşluk
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    onClick: () -> Unit
) {
    ClickableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${medication.dosage.toDisplayString()} • ${medication.form.toDisplayName()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Durum badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (medication.isActive) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (medication.isActive) "Aktif" else "Pasif",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (medication.isActive) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Alt bilgiler
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Saatler
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏰",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = medication.schedule.toDisplayString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Kalan gün
                medication.remainingDays()?.let { days ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📅",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                days == 0 -> "Son gün"
                                days == 1 -> "1 gün kaldı"
                                else -> "$days gün kaldı"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (days <= 3) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun com.medtracking.app.domain.model.MedicationForm.toDisplayName(): String = when (this) {
    com.medtracking.app.domain.model.MedicationForm.TABLET -> "Tablet"
    com.medtracking.app.domain.model.MedicationForm.CAPSULE -> "Kapsül"
    com.medtracking.app.domain.model.MedicationForm.SYRUP -> "Şurup"
    com.medtracking.app.domain.model.MedicationForm.DROP -> "Damla"
    com.medtracking.app.domain.model.MedicationForm.INJECTION -> "Enjeksiyon"
    com.medtracking.app.domain.model.MedicationForm.CREAM -> "Krem"
    com.medtracking.app.domain.model.MedicationForm.SPRAY -> "Sprey"
    com.medtracking.app.domain.model.MedicationForm.POWDER -> "Toz"
    com.medtracking.app.domain.model.MedicationForm.OTHER -> "Diğer"
}

