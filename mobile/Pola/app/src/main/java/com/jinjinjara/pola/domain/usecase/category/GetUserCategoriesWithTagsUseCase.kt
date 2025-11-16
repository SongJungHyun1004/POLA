package com.jinjinjara.pola.domain.usecase.category

import com.jinjinjara.pola.domain.model.UserCategoryWithTags
import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class GetUserCategoriesWithTagsUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(): Result<List<UserCategoryWithTags>> {
        return categoryRepository.getUserCategoriesWithTags()
    }
}
