package com.jinjinjara.pola.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.jinjinjara.pola.data.remote.api.CategoryApi
import com.jinjinjara.pola.data.remote.dto.request.CategoryTagInitRequest
import com.jinjinjara.pola.data.remote.dto.request.CategoryWithTags
import com.jinjinjara.pola.data.mapper.toDomain
import com.jinjinjara.pola.data.remote.api.AuthApi
import com.jinjinjara.pola.data.remote.api.ContentApi
import com.jinjinjara.pola.data.remote.dto.request.FilesListRequest
import com.jinjinjara.pola.di.IoDispatcher
import com.jinjinjara.pola.domain.model.Category
import com.jinjinjara.pola.domain.model.CategoryRecommendation
import com.jinjinjara.pola.domain.model.FileDetail
import com.jinjinjara.pola.domain.model.FilesPage
import com.jinjinjara.pola.domain.model.UserCategory
import com.jinjinjara.pola.domain.repository.CategoryRepository
import com.jinjinjara.pola.domain.repository.ContentRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * CategoryRepository implementation
 */
class ContentRepositoryImpl @Inject constructor(
    private val contentApi: ContentApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ContentRepository {


    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getFileDetail(fileId: Long): Result<FileDetail> {
        return withContext(ioDispatcher) {
            try {
                Log.d("File:Repo", "Fetching file detail for fileId: $fileId")
                val response = contentApi.getFileDetail(fileId)

                Log.d("File:Repo", "Response code: ${response.code()}")
                Log.d("File:Repo", "Is successful: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    Log.d("File:Repo", "Successfully fetched file detail")
                    Log.d("File:Repo", "File ID: ${body.data.id}, Views: ${body.data.views}")

                    val fileDetail = body.data.toDomain()
                    Result.Success(fileDetail)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("File:Repo", "Failed to fetch file detail")
                    Log.e("File:Repo", "Error body: $errorBody")

                    Result.Error(
                        message = "파일 정보를 불러올 수 없습니다",
                        errorType = ErrorType.SERVER
                    )
                }
            } catch (e: Exception) {
                Log.e("File:Repo", "Exception while fetching file detail", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "알 수 없는 오류가 발생했습니다",
                    errorType = ErrorType.NETWORK
                )
            }
        }
    }


}