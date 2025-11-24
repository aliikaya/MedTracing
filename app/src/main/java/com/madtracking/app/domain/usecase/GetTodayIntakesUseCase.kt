package com.madtracking.app.domain.usecase

import com.madtracking.app.domain.model.Intake
import com.madtracking.app.domain.repository.IntakeRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTodayIntakesUseCase @Inject constructor(
    private val intakeRepository: IntakeRepository
) {
    operator fun invoke(profileId: Long, date: LocalDate = LocalDate.now()): Flow<List<Intake>> {
        return intakeRepository.getIntakesForDate(profileId, date)
    }
}

