package com.jinjinjara.pola.domain.usecase.remind

import com.jinjinjara.pola.domain.model.RemindData
import com.jinjinjara.pola.domain.repository.RemindRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val remindRepository: RemindRepository
) {
    suspend operator fun invoke(): Result<List<RemindData>> {
        return remindRepository.getReminders()
    }
}
