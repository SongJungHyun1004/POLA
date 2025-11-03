package com.jinjinjara.pola.presentation.ui.screen.upload

import android.net.Uri

data class GalleryImage(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateAdded: Long,
    val isSelected: Boolean = false
)