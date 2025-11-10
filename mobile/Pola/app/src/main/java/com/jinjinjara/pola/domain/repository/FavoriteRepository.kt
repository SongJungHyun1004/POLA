package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.domain.model.FavoriteData
import com.jinjinjara.pola.util.Result

interface FavoriteRepository {
    suspend fun getFavoriteList(): Result<List<FavoriteData>>
    suspend fun toggleFavorite(fileId: Long, isFavorite: Boolean): Result<Boolean>
}
