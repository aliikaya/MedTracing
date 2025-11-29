package com.madtracking.app.presentation.today

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.madtracking.app.domain.model.IntakeStatus
import com.madtracking.app.domain.model.MealRelation
import com.madtracking.app.ui.components.*
import com.madtracking.app.ui.theme.StatusMissed
import com.madtracking.app.ui.theme.StatusTaken
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    profileId: Long,
    onNavigateToAddMedication: () -> Unit,
    onNavigateToMedicationHistory: (Long) -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val listState = rememberLazyListState()
    
    // FAB visibility based on scroll
    val showFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 || 
            listState.firstVisibleItemScrollOffset < 100
        }
    }
    
    LaunchedEffect(profileId) {
        viewModel.loadIntakes(profileId)
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
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                text = "Bugün",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.profileName.isNotEmpty()) {
                                Text(
                                    text = uiState.profileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hata: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Tekrar Dene")
                        }
                    }
                }
                uiState.items.isEmpty() -> {
                    EmptyState(
                        icon = "💊",
                        title = "Bugün için planlanmış ilaç yok",
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
                        // Özet kartı
                        item {
                            TodaySummaryCard(
                                total = uiState.items.size,
                                taken = uiState.items.count { it.status == IntakeStatus.TAKEN },
                                missed = uiState.items.count { it.status == IntakeStatus.MISSED }
                            )
                        }
                        
                        itemsIndexed(
                            items = uiState.items,
                            key = { _, item -> item.intakeId }
                        ) { index, item ->
                            AnimatedListItem(index = index) {
                                ModernIntakeCard(
                                    item = item,
                                    timeFormatter = timeFormatter,
                                    onMarkTaken = { viewModel.onMarkTaken(item.intakeId) },
                                    onMarkMissed = { viewModel.onMarkMissed(item.intakeId) },
                                    onNavigateToHistory = { onNavigateToMedicationHistory(item.medicationId) }
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
private fun TodaySummaryCard(
    total: Int,
    taken: Int,
    missed: Int
) {
    val remaining = total - taken - missed
    val progress = if (total > 0) taken.toFloat() / total else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bugünkü İlerleme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$taken / $total",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStatItem(
                    value = "$remaining",
                    label = "Bekliyor",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                SummaryStatItem(
                    value = "$taken",
                    label = "Alındı",
                    color = StatusTaken
                )
                SummaryStatItem(
                    value = "$missed",
                    label = "Kaçırıldı",
                    color = StatusMissed
                )
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernIntakeCard(
    item: TodayIntakeUi,
    timeFormatter: DateTimeFormatter,
    onMarkTaken: () -> Unit,
    onMarkMissed: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "card_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = tween(150),
        label = "card_elevation"
    )
    
    val cardColors = when (item.status) {
        IntakeStatus.TAKEN -> CardDefaults.cardColors(
            containerColor = StatusTaken.copy(alpha = 0.08f)
        )
        IntakeStatus.MISSED -> CardDefaults.cardColors(
            containerColor = StatusMissed.copy(alpha = 0.08f)
        )
        else -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    Card(
        onClick = onNavigateToHistory,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Saat badge
            TimeBadge(
                time = item.time.format(timeFormatter),
                isHighlighted = item.status == IntakeStatus.PLANNED
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // İlaç bilgileri
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.medicationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.dosageDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Kalan gün
                    item.getRemainingDaysDisplay()?.let { remainingDisplay ->
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = remainingDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.isExpired || item.remainingDays == 0)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Kullanım talimatı
                if (item.mealRelation != MealRelation.IRRELEVANT) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📋 ${item.mealRelation.toDisplayText()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Status veya aksiyonlar
            when (item.status) {
                IntakeStatus.PLANNED -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledIconButton(
                            onClick = onMarkMissed,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = StatusMissed.copy(alpha = 0.15f),
                                contentColor = StatusMissed
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Kaçırdım",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        FilledIconButton(
                            onClick = onMarkTaken,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = StatusTaken,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Aldım",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                else -> {
                    StatusChip(status = item.status)
                }
            }
        }
    }
}
