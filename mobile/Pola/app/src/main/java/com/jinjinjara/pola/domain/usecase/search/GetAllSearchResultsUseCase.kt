package com.jinjinjara.pola.domain.usecase.search

import com.jinjinjara.pola.domain.model.TagSearchFile
import com.jinjinjara.pola.domain.repository.SearchRepository
import com.jinjinjara.pola.domain.usecase.BaseUseCase
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class GetAllSearchResultsUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) : BaseUseCase<String, Result<List<TagSearchFile>>> {

    override suspend fun invoke(params: String): Result<List<TagSearchFile>> {
        return searchRepository.searchAll(params)
    }
}