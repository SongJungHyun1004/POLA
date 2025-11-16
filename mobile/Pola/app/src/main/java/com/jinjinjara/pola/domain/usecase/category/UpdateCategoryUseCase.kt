package com.jinjinjara.pola.domain.usecase.category

import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long, name: String): Result<Unit> {
        return categoryRepository.updateCategory(categoryId, name)
    }
}
