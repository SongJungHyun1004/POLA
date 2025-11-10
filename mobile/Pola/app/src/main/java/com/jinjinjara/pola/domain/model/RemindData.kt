package com.jinjinjara.pola.domain.model

data class RemindData(
    val id: Long,
    val imageUrl: String,
    val type: String,
    val context: String,
    val isFavorite: Boolean,
    val tags: List<String> = emptyList()
)
