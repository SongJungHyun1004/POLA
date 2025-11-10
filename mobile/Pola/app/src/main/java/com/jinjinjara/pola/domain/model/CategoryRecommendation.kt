package com.jinjinjara.pola.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Category recommendation domain model
 */
data class CategoryRecommendation(
    val categoryName: String,
    val tags: List<String>,
    val icon: ImageVector? = null // Will be mapped in presentation layer
)