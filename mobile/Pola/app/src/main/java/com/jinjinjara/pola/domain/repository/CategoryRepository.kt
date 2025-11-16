package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.domain.model.Category
import com.jinjinjara.pola.domain.model.CategoryRecommendation
import com.jinjinjara.pola.domain.model.FileDetail
import com.jinjinjara.pola.domain.model.FilesPage
import com.jinjinjara.pola.domain.model.UserCategory
import com.jinjinjara.pola.domain.model.UserCategoryWithTags
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
     * Get files list by category
     */
    suspend fun getFilesByCategory(
        categoryId: Long,
        page: Int = 0,
        size: Int = 20,
        sortBy: String,
        direction: String
    ): Result<FilesPage>

    /**
     * Get user's categories
     */
    suspend fun getUserCategories(): Result<List<UserCategory>>

    /**
     * Get user's categories
     */
    suspend fun getCategories(): Result<List<Category>>

    /**
     * Get user's categories with tags
     */
    suspend fun getUserCategoriesWithTags(): Result<List<UserCategoryWithTags>>

    /**
     * Update category name
     */
    suspend fun updateCategory(categoryId: Long, name: String): Result<Unit>

}