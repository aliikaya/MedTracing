package com.madtracking.app.presentation.profiles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    onProfileClick: (Long) -> Unit,
    onAddProfile: () -> Unit,
    viewModel: ProfilesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profiller") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Profil Ekle")
            }
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
                    Text(
                        text = "Hata: ${uiState.error}",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                uiState.profiles.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Henüz profil yok",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "İlk profilinizi eklemek için + butonuna basın",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.profiles, key = { it.id }) { profile ->
                            Card(
                                onClick = { onProfileClick(profile.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = profile.avatarEmoji ?: "👤",
                                        style = MaterialTheme.typography.headlineLarge
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = profile.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        profile.relation?.let { relation ->
                                            Text(
                                                text = relation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Text(
                                        text = "→",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Profil ekleme dialogu
        if (uiState.showAddDialog) {
            AddProfileDialog(
                name = uiState.newProfileName,
                emoji = uiState.newProfileEmoji,
                relation = uiState.newProfileRelation,
                onNameChange = { viewModel.onNameChange(it) },
                onEmojiChange = { viewModel.onEmojiChange(it) },
                onRelationChange = { viewModel.onRelationChange(it) },
                onDismiss = { viewModel.hideAddDialog() },
                onSave = { viewModel.saveProfile() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProfileDialog(
    name: String,
    emoji: String,
    relation: String,
    onNameChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit,
    onRelationChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val emojiOptions = listOf("👤", "👨", "👩", "👦", "👧", "👴", "👵", "🧑", "👶", "🐱", "🐶")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Profil") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("İsim") },
                    placeholder = { Text("örn: Annem") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Emoji seçici
                Text(
                    text = "Avatar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    emojiOptions.take(6).forEach { emojiOption ->
                        FilterChip(
                            selected = emoji == emojiOption,
                            onClick = { onEmojiChange(emojiOption) },
                            label = { Text(emojiOption) }
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    emojiOptions.drop(6).forEach { emojiOption ->
                        FilterChip(
                            selected = emoji == emojiOption,
                            onClick = { onEmojiChange(emojiOption) },
                            label = { Text(emojiOption) }
                        )
                    }
                }

                OutlinedTextField(
                    value = relation,
                    onValueChange = onRelationChange,
                    label = { Text("İlişki (opsiyonel)") },
                    placeholder = { Text("örn: Anne, Baba, Çocuk") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = name.isNotBlank()
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
