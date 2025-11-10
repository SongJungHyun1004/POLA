package com.jinjinjara.pola.data.repository

import com.jinjinjara.pola.data.remote.api.HomeApi
import com.jinjinjara.pola.domain.model.CategoryInfo
import com.jinjinjara.pola.domain.model.FileInfo
import com.jinjinjara.pola.domain.model.HomeScreenData
import com.jinjinjara.pola.domain.repository.HomeRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeApi: HomeApi
) : HomeRepository {

    override suspend fun getHomeData(): Result<HomeScreenData> {
        return try {
            val response = homeApi.getHomeData()

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                // DTO를 Domain Model로 변환
                val categories = body.data.categories.map { categoryData ->
                    CategoryInfo(
                        id = categoryData.categoryId,
                        name = categoryData.categoryName,
                        recentFiles = categoryData.files.take(3).map { fileData ->
                            FileInfo(
                                id = fileData.id,
                                imageUrl = fileData.src,
                                type = fileData.type,
                                isFavorite = fileData.favorite
                            )
                        }
                    )
                }

                val timeline = body.data.timeline.map { fileData ->
                    FileInfo(
                        id = fileData.id,
                        imageUrl = fileData.src,
                        type = fileData.type,
                        isFavorite = fileData.favorite
                    )
                }

                Result.Success(
                    HomeScreenData(
                        categories = categories,
                        timeline = timeline
                    )
                )
            } else {
                Result.Error(
                    message = "홈 데이터를 불러올 수 없습니다",
                    errorType = ErrorType.SERVER
                )
            }
        } catch (e: Exception) {
            Result.Error(
                exception = e,
                message = e.message ?: "알 수 없는 오류가 발생했습니다",
                errorType = ErrorType.NETWORK
            )
        }
    }
}