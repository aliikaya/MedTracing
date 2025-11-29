package com.madtracking.app.domain.usecase

import com.madtracking.app.domain.repository.IntakeRepository
import javax.inject.Inject

class MarkIntakeMissedUseCase @Inject constructor(
    private val intakeRepository: IntakeRepository
) {
    suspend operator fun invoke(intakeId: Long) {
        intakeRepository.markIntakeMissed(intakeId)
    }
}

