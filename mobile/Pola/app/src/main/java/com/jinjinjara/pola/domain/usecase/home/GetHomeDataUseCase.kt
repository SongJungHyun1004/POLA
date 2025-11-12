package com.jinjinjara.pola.domain.usecase.home

import com.jinjinjara.pola.domain.model.HomeScreenData
import com.jinjinjara.pola.domain.repository.HomeRepository
import com.jinjinjara.pola.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    operator fun invoke(): Flow<Result<HomeScreenData>> = flow {
        emit(Result.Loading)
        try {
            val result = homeRepository.getHomeData()
            emit(result)
        } catch (e: Exception) {
            emit(Result.Error(exception = e, message = e.message))
        }
    }
}