package com.jinjinjara.pola.domain.usecase.category

import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class RemoveTagFromCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long, tagId: Long): Result<Unit> {
        return categoryRepository.removeTagFromCategory(categoryId, tagId)
    }
}
