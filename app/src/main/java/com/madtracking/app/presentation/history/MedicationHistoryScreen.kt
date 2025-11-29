package com.madtracking.app.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.madtracking.app.domain.model.IntakeStatus
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: MedicationHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kullanım Geçmişi")
                        if (uiState.medicationName.isNotEmpty()) {
                            Text(
                                text = uiState.medicationName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
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
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hata: ${uiState.errorMessage}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Tekrar Dene")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Tarih aralığı ve adherence özeti
                        item {
                            AdherenceSummaryCard(
                                fromDate = uiState.fromDate.format(dateFormatter),
                                toDate = uiState.toDate.format(dateFormatter),
                                adherenceResult = uiState.adherenceResult
                            )
                        }

                        // Günlük gruplar
                        if (uiState.dailyGroups.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Bu tarih aralığında kayıt bulunamadı.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(uiState.dailyGroups, key = { it.date.toString() }) { group ->
                                DailyGroupCard(
                                    group = group,
                                    dateFormatter = dateFormatter,
                                    timeFormatter = timeFormatter
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdherenceSummaryCard(
    fromDate: String,
    toDate: String,
    adherenceResult: AdherenceResultUi?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Tarih aralığı
            Text(
                text = "$fromDate - $toDate",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (adherenceResult != null) {
                // Uyum yüzdesi ve progress bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Uyum Oranı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = adherenceResult.adherencePercentText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress bar
                val progress = adherenceResult.adherencePercentText
                    .replace("%", "")
                    .toIntOrNull()
                    ?.div(100f) ?: 0f
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        progress >= 0.8f -> Color(0xFF4CAF50) // Yeşil
                        progress >= 0.5f -> Color(0xFFFFC107) // Sarı
                        else -> Color(0xFFF44336) // Kırmızı
                    },
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // İstatistikler
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "Planlanan",
                        value = "${adherenceResult.planned}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    StatItem(
                        label = "Alınan",
                        value = "${adherenceResult.taken}",
                        color = Color(0xFF4CAF50)
                    )
                    StatItem(
                        label = "Kaçırılan",
                        value = "${adherenceResult.missed}",
                        color = Color(0xFFF44336)
                    )
                }
            } else {
                Text(
                    text = "Uyum verisi hesaplanamadı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DailyGroupCard(
    group: DailyIntakeGroupUi,
    dateFormatter: DateTimeFormatter,
    timeFormatter: DateTimeFormatter
) {
    val dayName = group.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("tr"))
        .replaceFirstChar { it.uppercase() }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Tarih başlığı
            Text(
                text = "${group.date.format(dateFormatter)} $dayName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Intake listesi
            group.intakes.forEachIndexed { index, intake ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                IntakeRow(
                    intake = intake,
                    timeFormatter = timeFormatter
                )
            }
        }
    }
}

@Composable
private fun IntakeRow(
    intake: DailyIntakeUi,
    timeFormatter: DateTimeFormatter
) {
    val (backgroundColor, icon, iconTint) = when (intake.status) {
        IntakeStatus.TAKEN -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.1f),
            Icons.Default.Check,
            Color(0xFF4CAF50)
        )
        IntakeStatus.MISSED -> Triple(
            Color(0xFFF44336).copy(alpha = 0.1f),
            Icons.Default.Close,
            Color(0xFFF44336)
        )
        IntakeStatus.PLANNED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            Icons.Default.Info,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        IntakeStatus.SKIPPED -> Triple(
            Color(0xFFFFC107).copy(alpha = 0.1f),
            Icons.Default.Close,
            Color(0xFFFFC107)
        )
    }
    
    val statusText = when (intake.status) {
        IntakeStatus.TAKEN -> "Alındı"
        IntakeStatus.MISSED -> "Kaçırıldı"
        IntakeStatus.PLANNED -> "Bekliyor"
        IntakeStatus.SKIPPED -> "Atlandı"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Text(
                text = intake.time.format(timeFormatter),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = iconTint,
            fontWeight = FontWeight.Medium
        )
    }
}

