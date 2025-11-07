package com.jinjinjara.pola.domain.usecase.home

import com.jinjinjara.pola.domain.model.HomeScreenData
import com.jinjinjara.pola.domain.repository.HomeRepository
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(): Result<HomeScreenData> {
        return homeRepository.getHomeData()
    }
}