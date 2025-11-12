package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.domain.model.TimelinePage
import com.jinjinjara.pola.util.Result

interface TimelineRepository {
    suspend fun getTimeline(
        page: Int,
        size: Int,
        sortBy: String,
        direction: String,
        filterType: String?,
        filterId: Long?
    ): Result<TimelinePage>
}
