package com.jinjinjara.pola.domain.usecase.category

import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class AddTagsToCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(categoryId: Long, tagNames: List<String>): Result<List<Long>> {
        return categoryRepository.addTagsToCategory(categoryId, tagNames)
    }
}
