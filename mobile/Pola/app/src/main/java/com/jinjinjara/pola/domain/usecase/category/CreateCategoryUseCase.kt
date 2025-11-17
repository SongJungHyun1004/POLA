package com.jinjinjara.pola.domain.usecase.category

import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(name: String): Result<Long> {
        return categoryRepository.createCategory(name)
    }
}
