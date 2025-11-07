package com.jinjinjara.pola.domain.usecase.category

import com.jinjinjara.pola.domain.model.CategoryRecommendation
import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

/**
 * Get category recommendations UseCase
 */
class GetCategoryRecommendationsUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(): Result<List<CategoryRecommendation>> {
        return categoryRepository.getCategoryRecommendations()
    }
}