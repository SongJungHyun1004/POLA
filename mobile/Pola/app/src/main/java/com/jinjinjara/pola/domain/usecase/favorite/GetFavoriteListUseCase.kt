package com.jinjinjara.pola.domain.usecase.favorite

import com.jinjinjara.pola.domain.model.FavoriteData
import com.jinjinjara.pola.domain.repository.FavoriteRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class GetFavoriteListUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(): Result<List<FavoriteData>> {
        return favoriteRepository.getFavoriteList()
    }
}
