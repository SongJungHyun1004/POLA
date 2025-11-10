package com.jinjinjara.pola.domain.usecase.favorite

import com.jinjinjara.pola.domain.repository.FavoriteRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(fileId: Long, isFavorite: Boolean): Result<Boolean> {
        return favoriteRepository.toggleFavorite(fileId, isFavorite)
    }
}
