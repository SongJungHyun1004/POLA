package com.jinjinjara.pola.domain.model

data class HomeScreenData(
    val categories: List<CategoryInfo>,
    val timeline: List<FileInfo>
)

data class CategoryInfo(
    val id: Long,
    val name: String,
    val recentFiles: List<FileInfo>  // 최근 3개만
)

data class FileInfo(
    val id: Long,
    val imageUrl: String,
    val type: String,
    val isFavorite: Boolean
)