package com.jinjinjara.pola.domain.usecase.category

import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

/**
 * Initialize user's categories and tags UseCase
 */
class InitCategoryTagsUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoriesWithTags: Map<String, List<String>>): Result<Unit> {
        return categoryRepository.initCategoryTags(categoriesWithTags)
    }
}