package com.madtracking.app.presentation.addmedication

import com.madtracking.app.domain.model.DosageUnit
import com.madtracking.app.domain.model.MedicationForm
import com.madtracking.app.domain.model.MedicationImportance

data class AddMedicationUiState(
    val name: String = "",
    val form: MedicationForm = MedicationForm.TABLET,
    val dosageAmount: String = "1",
    val dosageUnit: DosageUnit = DosageUnit.TABLET,
    val timesInput: String = "08:00,20:00", // Virgülle ayrılmış saatler
    val importance: MedicationImportance = MedicationImportance.REGULAR,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() && 
                dosageAmount.toDoubleOrNull() != null &&
                dosageAmount.toDoubleOrNull()!! > 0 &&
                timesInput.isNotBlank()
}

