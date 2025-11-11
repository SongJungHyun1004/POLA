package com.jinjinjara.pola.domain.usecase.timeline

import com.jinjinjara.pola.domain.model.TimelinePage
import com.jinjinjara.pola.domain.repository.TimelineRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class GetTimelineUseCase @Inject constructor(
    private val timelineRepository: TimelineRepository
) {
    suspend operator fun invoke(
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "createdAt",
        direction: String = "DESC",
        filterType: String? = null,
        filterId: Long? = null
    ): Result<TimelinePage> {
        return timelineRepository.getTimeline(
            page = page,
            size = size,
            sortBy = sortBy,
            direction = direction,
            filterType = filterType,
            filterId = filterId
        )
    }
}
