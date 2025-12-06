package com.medtracking.app.domain.usecase

import com.medtracking.app.domain.repository.IntakeRepository
import java.time.LocalDateTime
import javax.inject.Inject

class MarkIntakeTakenUseCase @Inject constructor(
    private val intakeRepository: IntakeRepository
) {
    suspend operator fun invoke(intakeId: Long, takenTime: LocalDateTime = LocalDateTime.now()) {
        intakeRepository.markIntakeTaken(intakeId, takenTime)
    }
}

