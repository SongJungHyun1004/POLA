package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.util.Result

interface FavoriteRepository {
    suspend fun toggleFavorite(fileId: Long, isFavorite: Boolean): Result<Boolean>
}
