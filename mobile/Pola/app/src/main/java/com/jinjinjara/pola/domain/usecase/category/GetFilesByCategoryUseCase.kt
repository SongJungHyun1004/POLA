package com.jinjinjara.pola.domain.usecase.category

import com.jinjinjara.pola.domain.model.FilesPage
import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class GetFilesByCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(
        categoryId: Long,
        page: Int = 0,
        size: Int = 20
    ): Result<FilesPage> {
        return categoryRepository.getFilesByCategory(categoryId, page, size)
    }
}