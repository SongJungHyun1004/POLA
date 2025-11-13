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
import com.jinjinjara.pola.data.remote.dto.request.AddTagsRequest
import com.jinjinjara.pola.data.remote.dto.request.FilesListRequest
import com.jinjinjara.pola.data.remote.dto.request.ShareRequest
import com.jinjinjara.pola.data.remote.dto.request.UpdateContextRequest
import com.jinjinjara.pola.data.remote.dto.response.FileTag
import com.jinjinjara.pola.di.IoDispatcher
import com.jinjinjara.pola.domain.model.Category
import com.jinjinjara.pola.domain.model.CategoryRecommendation
import com.jinjinjara.pola.domain.model.FileDetail
import com.jinjinjara.pola.domain.model.FilesPage
import com.jinjinjara.pola.domain.model.ShareLink
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

    override suspend fun deleteFile(fileId: Long): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                Log.d("File:Repo", "Deleting file with fileId: $fileId")
                val response = contentApi.deleteFile(fileId)

                Log.d("File:Repo", "Response code: ${response.code()}")
                Log.d("File:Repo", "Is successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    Log.d("File:Repo", "File successfully deleted")
                    Result.Success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("File:Repo", "Failed to delete file")
                    Log.e("File:Repo", "Error body: $errorBody")

                    Result.Error(
                        message = "파일 삭제에 실패했습니다",
                        errorType = ErrorType.SERVER
                    )
                }
            } catch (e: Exception) {
                Log.e("File:Repo", "Exception while deleting file", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "알 수 없는 오류가 발생했습니다",
                    errorType = ErrorType.NETWORK
                )
            }
        }
    }


    override suspend fun createShareLink(fileId: Long, expireHours: Int): Result<ShareLink> {
        return withContext(ioDispatcher) {
            try {
                Log.d("File:Repo", "Creating share link for fileId: $fileId (expireHours: $expireHours)")
                val response = contentApi.createShareLink(
                    fileId = fileId,
                    request = ShareRequest(expireHours)
                )

                Log.d("File:Repo", "Response code: ${response.code()}")
                Log.d("File:Repo", "Is successful: ${response.isSuccessful}")

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.data
                    Log.d("File:Repo", "Share link successfully created: ${data.shareUrl}")

                    Result.Success(
                        ShareLink(
                            shareUrl = data.shareUrl,
                            expiredAt = data.expiredAt
                        )
                    )
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("File:Repo", "Failed to create share link")
                    Log.e("File:Repo", "Error body: $errorBody")

                    Result.Error(
                        message = "공유 링크 생성에 실패했습니다",
                        errorType = ErrorType.SERVER
                    )
                }
            } catch (e: Exception) {
                Log.e("File:Repo", "Exception while creating share link", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "알 수 없는 오류가 발생했습니다",
                    errorType = ErrorType.NETWORK
                )
            }
        }
    }

    // createShareLink 메서드 아래에 추가

    override suspend fun getFileTags(fileId: Long): Result<List<FileTag>> {
        return withContext(ioDispatcher) {
            try {
                Log.d("File:Repo", "Fetching tags for fileId: $fileId")
                val response = contentApi.getFileTags(fileId)

                if (response.isSuccessful && response.body() != null) {
                    val tags = response.body()!!.data
                    Log.d("File:Repo", "Successfully fetched ${tags.size} tags")
                    Result.Success(tags)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("File:Repo", "Failed to fetch tags: $errorBody")
                    Result.Error(
                        message = "태그를 불러올 수 없습니다",
                        errorType = ErrorType.SERVER
                    )
                }
            } catch (e: Exception) {
                Log.e("File:Repo", "Exception while fetching tags", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "알 수 없는 오류가 발생했습니다",
                    errorType = ErrorType.NETWORK
                )
            }
        }
    }

    override suspend fun addFileTags(fileId: Long, tagNames: List<String>): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                Log.d("File:Repo", "Adding tags to fileId: $fileId, tags: $tagNames")
                val response = contentApi.addFileTags(
                    fileId = fileId,
                    request = AddTagsRequest(tagNames)
                )

                if (response.isSuccessful) {
                    Log.d("File:Repo", "Tags successfully added")
                    Result.Success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("File:Repo", "Failed to add tags: $errorBody")
                    Result.Error(
                        message = "태그 추가에 실패했습니다",
                        errorType = ErrorType.SERVER
                    )
                }
            } catch (e: Exception) {
                Log.e("File:Repo", "Exception while adding tags", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "알 수 없는 오류가 발생했습니다",
                    errorType = ErrorType.NETWORK
                )
            }
        }
    }

    override suspend fun removeFileTag(fileId: Long, tagId: Long): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                Log.d("File:Repo", "Removing tag $tagId from fileId: $fileId")
                val response = contentApi.removeFileTag(fileId, tagId)

                if (response.isSuccessful) {
                    Log.d("File:Repo", "Tag successfully removed")
                    Result.Success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("File:Repo", "Failed to remove tag: $errorBody")
                    Result.Error(
                        message = "태그 제거에 실패했습니다",
                        errorType = ErrorType.SERVER
                    )
                }
            } catch (e: Exception) {
                Log.e("File:Repo", "Exception while removing tag", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "알 수 없는 오류가 발생했습니다",
                    errorType = ErrorType.NETWORK
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateFileContext(fileId: Long, context: String): Result<Unit> {
        return withContext(ioDispatcher) {
            try {
                Log.d("File:Repo", "Updating context for fileId: $fileId")
                val response = contentApi.updateFileContext(
                    fileId = fileId,
                    request = UpdateContextRequest(context)
                )

                if (response.isSuccessful && response.body() != null) {
                    val fileDetail = response.body()!!.data.toDomain()
                    Log.d("File:Repo", "Context successfully updated")
                    Result.Success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("File:Repo", "Failed to update context: $errorBody")
                    Result.Error(
                        message = "내용 수정에 실패했습니다",
                        errorType = ErrorType.SERVER
                    )
                }
            } catch (e: Exception) {
                Log.e("File:Repo", "Exception while updating context", e)
                Result.Error(
                    exception = e,
                    message = e.message ?: "알 수 없는 오류가 발생했습니다",
                    errorType = ErrorType.NETWORK
                )
            }
        }
    }


}