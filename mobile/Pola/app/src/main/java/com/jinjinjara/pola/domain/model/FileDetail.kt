package com.jinjinjara.pola.domain.model

import java.time.LocalDateTime

/**
 * 파일 상세 정보 Domain Model
 */
data class FileDetail(
    val id: Long,
    val userId: Long,
    val categoryId: Long,
    val src: String,
    val type: String,
    val context: String?,
    val ocrText: String?,
    val vectorId: Long?,
    val fileSize: Long,
    val shareStatus: Boolean,
    val favorite: Boolean,
    val favoriteSort: Int,
    val favoritedAt: LocalDateTime?,
    val views: Int,
    val platform: String,
    val originUrl: String,
    val createdAt: LocalDateTime,
    val lastViewedAt: LocalDateTime?,
    val tags: List<Tag>
)

data class Tag(
    val id: Long,
    val name: String
)