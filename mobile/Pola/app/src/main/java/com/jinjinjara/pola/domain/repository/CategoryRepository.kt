package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.domain.model.Category
import com.jinjinjara.pola.domain.model.CategoryRecommendation
import com.jinjinjara.pola.util.Result

/**
 * Category related Repository interface
 */
interface CategoryRepository {

    /**
     * Get recommended categories with tags
     */
    suspend fun getCategoryRecommendations(): Result<List<CategoryRecommendation>>

    /**
     * Initialize user's categories and tags
     */
    suspend fun initCategoryTags(categoriesWithTags: Map<String, List<String>>): Result<Unit>

    /**
     * Get user's categories
     */
    suspend fun getCategories(): Result<List<Category>>
}