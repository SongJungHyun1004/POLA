package com.jinjinjara.pola.domain.model

import com.jinjinjara.pola.presentation.ui.component.DisplayItem

data class FavoriteData(
    val fileId: Long,
    override val imageUrl: String,
    val type: String,
    val context: String,
    override val isFavorite: Boolean,
    override val tags: List<String> = emptyList()
) : DisplayItem {
    override val id: String
        get() = fileId.toString()
    override val imageRes: Int
        get() = 0
    override val description: String
        get() = context
}
