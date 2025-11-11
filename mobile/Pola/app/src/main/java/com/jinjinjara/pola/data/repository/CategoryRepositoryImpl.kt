package com.jinjinjara.pola.data.repository

import android.util.Log
import com.jinjinjara.pola.data.remote.api.CategoryApi
import com.jinjinjara.pola.data.remote.dto.request.CategoryTagInitRequest
import com.jinjinjara.pola.data.remote.dto.request.CategoryWithTags
import com.jinjinjara.pola.data.mapper.toDomain
import com.jinjinjara.pola.di.IoDispatcher
import com.jinjinjara.pola.domain.model.Category
import com.jinjinjara.pola.domain.model.CategoryRecommendation
import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * CategoryRepository implementation
 */
class CategoryRepositoryImpl @Inject constructor(
    private val categoryApi: CategoryApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CategoryRepository {

    override suspend fun getCategoryRecommendations(): Result<List<CategoryRecommendation>> {
        return withContext(ioDispatcher) {
            try {
                Log.d("Category:Repo", "Fetching category recommendations")
                val response = categoryApi.getCategoryRecommendations()

                Log.d("Category:Repo", "Response code: ${response.code()}")
                Log.d("Category:Repo", "Response message: ${response.message()}")
                Log.d("Category:Repo", "Is successful: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    Log.d("Category:Repo", "Response body: $apiResponse")

                    if (apiResponse.data != null) {
                        val categories = apiResponse.data.recommendations.map { it.toDomain() }
                        Log.d("Category:Repo", "Successfully fetched ${categories.size} categories")
                        Result.Success(categories)
                    } else {
                        Log.e("Category:Repo", "Response data is null")
                        Log.e("Category:Repo", "Response status: ${apiResponse.status}")
                        Log.e("Category:Repo", "Response message: ${apiResponse.message}")
                        Result.Error(
                            exception = Exception("Data is null"),
                            message = "카테고리 정보를 가져올 수 없습니다."
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("Category:Repo", "Failed to fetch categories")
                    Log.e("Category:Repo", "Response code: ${response.code()}")
                    Log.e("Category:Repo", "Response message: ${response.message()}")
                    Log.e("Category:Repo", "Error body: $errorBody")

                    Result.Error(
                        exception = Exception(response.message()),
                        message = "카테고리 정보를 가져올 수 없습니다. (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                Log.e("Category:Repo", "Exception while fetching categories", e)
                Log.e("Category:Repo", "Exception type: ${e.javaClass.simpleName}")
                Log.e("Category:Repo", "Exception message: ${e.message}")
                e.printStackTrace()

                Result.Error(
                    exception = e,
                    message = e.message ?: "네트워크 오류가 발생했습니다."
                )
            }
        }
    }

    override suspend fun initCategoryTags(categoriesWithTags: Map<String, List<String>>): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                Log.d("Category:Repo", "Initializing category tags")
                Log.d("Category:Repo", "Categories: ${categoriesWithTags.keys}")

                val request = CategoryTagInitRequest(
                    categories = categoriesWithTags.map { (categoryName, tags) ->
                        CategoryWithTags(
                            categoryName = categoryName,
                            tags = tags
                        )
                    }
                )

                Log.d("Category:Repo", "Request: $request")
                val response = categoryApi.initCategoryTags(request)

                Log.d("Category:Repo", "Response code: ${response.code()}")
                Log.d("Category:Repo", "Response message: ${response.message()}")

                if (response.isSuccessful) {
                    Log.d("Category:Repo", "Successfully initialized category tags")
                    Result.Success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("Category:Repo", "Failed to initialize category tags")
                    Log.e("Category:Repo", "Error body: $errorBody")

                    Result.Error(
                        exception = Exception(response.message()),
                        message = "카테고리 초기화에 실패했습니다. (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                Log.e("Category:Repo", "Exception while initializing categories", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "네트워크 오류가 발생했습니다."
                )
            }
        }
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return withContext(ioDispatcher) {
            try {
                Log.d("Category:Repo", "Fetching user categories")
                val response = categoryApi.getCategories()

                Log.d("Category:Repo", "Response code: ${response.code()}")
                Log.d("Category:Repo", "Is successful: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d("Category:Repo", "Successfully fetched ${body.data.size} categories")

                    val categories = body.data
                        .map { it.toDomain() }
                        .sortedBy { it.sort }

                    Result.Success(categories)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("Category:Repo", "Failed to fetch categories")
                    Log.e("Category:Repo", "Error body: $errorBody")

                    Result.Error(
                        message = "카테고리 목록을 불러올 수 없습니다",
                        errorType = ErrorType.SERVER
                    )
                }
            } catch (e: Exception) {
                Log.e("Category:Repo", "Exception while fetching categories", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "알 수 없는 오류가 발생했습니다",
                    errorType = ErrorType.NETWORK
                )
            }
        }
    }
}