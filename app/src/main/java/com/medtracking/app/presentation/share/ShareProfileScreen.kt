package com.medtracking.app.presentation.share

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.medtracking.app.domain.model.MemberRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareProfileBottomSheet(
    profileId: Long,
    profileName: String,
    onDismiss: () -> Unit,
    viewModel: ShareProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Show Android share sheet when invitation URL is ready
    LaunchedEffect(uiState.showShareSheet, uiState.invitationUrl) {
        if (uiState.showShareSheet && uiState.invitationUrl != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "$profileName profilini paylaş")
                putExtra(Intent.EXTRA_TEXT, """
                    $profileName profilini sizinle paylaşıyorum!
                    
                    Aşağıdaki linke tıklayarak profili kabul edebilirsiniz:
                    ${uiState.invitationUrl}
                """.trimIndent())
            }
            context.startActivity(Intent.createChooser(shareIntent, "Profili paylaş"))
            viewModel.onShareSheetDismissed()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "\"$profileName\" profilini paylaş",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Davet edilen kişinin yetkisini seçin:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Role selection cards
            RoleSelectionCard(
                role = MemberRole.VIEWER,
                title = "İzleyici",
                description = "Sadece görüntüleyebilir, hiçbir değişiklik yapamaz",
                isSelected = uiState.selectedRole == MemberRole.VIEWER,
                onSelect = { viewModel.onRoleSelected(MemberRole.VIEWER) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RoleSelectionCard(
                role = MemberRole.PATIENT_MARK_ONLY,
                title = "Hasta",
                description = "İlaçları \"Aldım/Kaçırdım\" olarak işaretleyebilir",
                isSelected = uiState.selectedRole == MemberRole.PATIENT_MARK_ONLY,
                onSelect = { viewModel.onRoleSelected(MemberRole.PATIENT_MARK_ONLY) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            RoleSelectionCard(
                role = MemberRole.CAREGIVER_EDITOR,
                title = "Düzenleyebilir",
                description = "İlaç ekleyebilir, düzenleyebilir ve işaretleyebilir",
                isSelected = uiState.selectedRole == MemberRole.CAREGIVER_EDITOR,
                onSelect = { viewModel.onRoleSelected(MemberRole.CAREGIVER_EDITOR) }
            )

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onCreateInvitationClick(profileId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.selectedRole != null
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Link Oluştur ve Paylaş")
                }
            }
        }
    }
}

@Composable
private fun RoleSelectionCard(
    role: MemberRole,
    title: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null // Click handled by Card
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

