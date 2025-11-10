package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.domain.model.RemindData
import com.jinjinjara.pola.util.Result

interface RemindRepository {
    suspend fun getReminders(): Result<List<RemindData>>
}
