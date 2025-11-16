package com.jinjinjara.pola.domain.model

data class TagSearchFile(
    val fileId: Long,
    val userId: Long,
    val categoryName: String,
    val tags: List<String>,
    val context: String,
    val ocrText: String,
    val imageUrl: String,
    val type: String,
    val createdAt: String
)
