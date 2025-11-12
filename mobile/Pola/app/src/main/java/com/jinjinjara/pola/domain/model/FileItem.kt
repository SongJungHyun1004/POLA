package com.jinjinjara.pola.domain.model

import com.jinjinjara.pola.presentation.ui.component.DisplayItem

data class FileItem(
    val fileId: Long,
    val src: String,
    override val type: String,
    val context: String,
    val favorite: Boolean,
    override val tags: List<String>,
    val createdAt: String
) : DisplayItem {
    override val id: String get() = fileId.toString()
    override val imageRes: Int = 0
    override val imageUrl: String = src
    override val description: String = context
    override val isFavorite: Boolean = favorite
}

data class FilesPage(
    val fileName: String,
    val content: List<FileItem>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val last: Boolean
)