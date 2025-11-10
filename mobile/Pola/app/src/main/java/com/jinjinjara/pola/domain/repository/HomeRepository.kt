package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.domain.model.HomeScreenData
import com.jinjinjara.pola.util.Result

interface HomeRepository {
    suspend fun getHomeData(): Result<HomeScreenData>
}