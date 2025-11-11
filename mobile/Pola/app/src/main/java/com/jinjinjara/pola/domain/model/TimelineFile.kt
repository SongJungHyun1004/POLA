package com.jinjinjara.pola.domain.model

import java.time.LocalDateTime

data class TimelineFile(
    val id: Long,
    val imageUrl: String,
    val type: String,
    val context: String?,
    val tags: List<String>,
    val createdAt: LocalDateTime
)

data class TimelinePage(
    val files: List<TimelineFile>,
    val currentPage: Int,
    val totalPages: Int,
    val isLast: Boolean
)
