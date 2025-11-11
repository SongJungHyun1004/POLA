package com.jinjinjara.pola.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.jinjinjara.pola.data.remote.api.FileUploadApi
import com.jinjinjara.pola.data.remote.dto.request.FileCompleteRequest
import com.jinjinjara.pola.domain.repository.FileUploadRepository
import com.jinjinjara.pola.util.ErrorType
import com.jinjinjara.pola.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class FileUploadRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileUploadApi: FileUploadApi
) : FileUploadRepository {

    override suspend fun uploadFile(uri: Uri, fileName: String, contentType: String): Result<String> {
        return try {
            Log.d("FileUpload", "=== Starting File Upload ===")
            Log.d("FileUpload", "FileName: $fileName, ContentType: $contentType")

            // 1. Presigned URL 요청
            val presignedResponse = fileUploadApi.getPresignedUrl(fileName)
            if (!presignedResponse.isSuccessful || presignedResponse.body() == null) {
                Log.e("FileUpload", "Failed to get presigned URL: ${presignedResponse.code()}")
                return Result.Error(
                    message = "Presigned URL 생성 실패",
                    errorType = when (presignedResponse.code()) {
                        401 -> ErrorType.UNAUTHORIZED
                        in 400..499 -> ErrorType.BAD_REQUEST
                        in 500..599 -> ErrorType.SERVER
                        else -> ErrorType.UNKNOWN
                    }
                )
            }

            val presignedData = presignedResponse.body()!!.data
            Log.d("FileUpload", "Presigned URL received: ${presignedData.url.take(50)}...")
            Log.d("FileUpload", "Key: ${presignedData.key}")

            // 2. 파일을 RequestBody로 변환
            val fileBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (fileBytes == null) {
                Log.e("FileUpload", "Failed to read file from URI")
                return Result.Error(
                    message = "파일을 읽을 수 없습니다",
                    errorType = ErrorType.BAD_REQUEST
                )
            }

            val fileSize = fileBytes.size.toLong()
            Log.d("FileUpload", "File size: $fileSize bytes")

            val requestBody = fileBytes.toRequestBody(contentType.toMediaTypeOrNull())

            // 3. S3에 업로드
            Log.d("FileUpload", "Uploading to S3...")
            val uploadResponse = fileUploadApi.uploadToS3(
                url = presignedData.url,
                file = requestBody,
                contentType = contentType
            )

            if (!uploadResponse.isSuccessful) {
                Log.e("FileUpload", "S3 upload failed: ${uploadResponse.code()}")
                return Result.Error(
                    message = "S3 업로드 실패",
                    errorType = ErrorType.SERVER
                )
            }

            Log.d("FileUpload", "S3 upload successful")

            // 4. 업로드 완료 처리
            val originUrl = presignedData.url.substringBefore("?")
            Log.d("FileUpload", "Origin URL: $originUrl")

            val platform = "APP"
            val completeRequest = FileCompleteRequest(
                key = presignedData.key,
                type = contentType,
                fileSize = fileSize,
                originUrl = originUrl,
                platform = platform,
            )

            val completeResponse = fileUploadApi.completeUpload(completeRequest)
            if (!completeResponse.isSuccessful || completeResponse.body() == null) {
                Log.e("FileUpload", "Complete upload failed: ${completeResponse.code()}")
                return Result.Error(
                    message = "업로드 완료 처리 실패",
                    errorType = when (completeResponse.code()) {
                        401 -> ErrorType.UNAUTHORIZED
                        in 400..499 -> ErrorType.BAD_REQUEST
                        in 500..599 -> ErrorType.SERVER
                        else -> ErrorType.UNKNOWN
                    }
                )
            }

            val fileId = completeResponse.body()!!.data.id
            Log.d("FileUpload", "=== Upload Complete SUCCESS ===")
            Log.d("FileUpload", "File ID: $fileId")

            // 파일 후처리 (자동 분류)를 별도 코루틴으로 실행 - UI는 기다리지 않음
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("FileUpload", "Starting automatic classification in background...")
                val postProcessResult = postProcessFile(fileId)
                when (postProcessResult) {
                    is Result.Success -> {
                        Log.d("FileUpload", "Auto classification SUCCESS")
                    }
                    is Result.Error -> {
                        Log.w("FileUpload", "Auto classification failed: ${postProcessResult.message}")
                    }
                    is Result.Loading -> {}
                }
            }

            Result.Success(completeResponse.body()!!.message)

        } catch (e: UnknownHostException) {
            Log.e("FileUpload", "Network error: no internet connection", e)
            Result.Error(
                exception = e,
                message = "인터넷 연결을 확인해주세요",
                errorType = ErrorType.NETWORK
            )
        } catch (e: SocketTimeoutException) {
            Log.e("FileUpload", "Timeout error", e)
            Result.Error(
                exception = e,
                message = "요청 시간이 초과되었습니다",
                errorType = ErrorType.TIMEOUT
            )
        } catch (e: Exception) {
            Log.e("FileUpload", "Upload failed with exception", e)
            Result.Error(
                exception = e,
                message = e.message ?: "업로드 실패",
                errorType = ErrorType.UNKNOWN
            )
        }
    }

    override suspend fun uploadText(text: String, fileName: String): Result<String> {
        return try {
            Log.d("FileUpload", "=== Starting Text Upload ===")
            Log.d("FileUpload", "FileName: $fileName")

            // 1. Presigned URL 요청
            val presignedResponse = fileUploadApi.getPresignedUrl(fileName)
            if (!presignedResponse.isSuccessful || presignedResponse.body() == null) {
                return Result.Error(
                    message = "Presigned URL 생성 실패",
                    errorType = ErrorType.SERVER
                )
            }

            val presignedData = presignedResponse.body()!!.data
            Log.d("FileUpload", "Presigned URL received")

            // 2. 텍스트를 RequestBody로 변환
            val textBytes = text.toByteArray(Charsets.UTF_8)
            val fileSize = textBytes.size.toLong()
            val requestBody = textBytes.toRequestBody("text/plain; charset=utf-8".toMediaTypeOrNull())

            // 3. S3에 업로드
            Log.d("FileUpload", "Uploading text to S3...")
            val uploadResponse = fileUploadApi.uploadToS3(
                url = presignedData.url,
                file = requestBody,
                contentType = "text/plain; charset=utf-8"
            )

            if (!uploadResponse.isSuccessful) {
                return Result.Error(
                    message = "S3 업로드 실패",
                    errorType = ErrorType.SERVER
                )
            }

            Log.d("FileUpload", "S3 upload successful")

            // 4. 업로드 완료 처리
            val platform = "APP"
            val originUrl = presignedData.url.substringBefore("?")
            val completeRequest = FileCompleteRequest(
                key = presignedData.key,
                type = "text/plain",
                fileSize = fileSize,
                originUrl = originUrl,
                platform = platform,
            )

            val completeResponse = fileUploadApi.completeUpload(completeRequest)
            if (!completeResponse.isSuccessful || completeResponse.body() == null) {
                return Result.Error(
                    message = "업로드 완료 처리 실패",
                    errorType = ErrorType.SERVER
                )
            }

            Log.d("FileUpload", "=== Upload Complete SUCCESS ===")

            Result.Success(completeResponse.body()!!.message)

        } catch (e: UnknownHostException) {
            Result.Error(
                exception = e,
                message = "인터넷 연결을 확인해주세요",
                errorType = ErrorType.NETWORK
            )
        } catch (e: SocketTimeoutException) {
            Result.Error(
                exception = e,
                message = "요청 시간이 초과되었습니다",
                errorType = ErrorType.TIMEOUT
            )
        } catch (e: Exception) {
            Log.e("FileUpload", "Text upload failed", e)
            Result.Error(
                exception = e,
                message = e.message ?: "업로드 실패",
                errorType = ErrorType.UNKNOWN
            )
        }
    }

    override suspend fun postProcessFile(fileId: Long): Result<String> {
        return try {
            Log.d("FileUpload", "=== Starting Post Process ===")
            Log.d("FileUpload", "FileId: $fileId")

            val response = fileUploadApi.postProcessFile(fileId)

            if (!response.isSuccessful || response.body() == null) {
                Log.e("FileUpload", "Post process failed: ${response.code()}")
                return Result.Error(
                    message = "파일 분석 실패",
                    errorType = when (response.code()) {
                        401 -> ErrorType.UNAUTHORIZED
                        404 -> ErrorType.BAD_REQUEST
                        in 400..499 -> ErrorType.BAD_REQUEST
                        in 500..599 -> ErrorType.SERVER
                        else -> ErrorType.UNKNOWN
                    }
                )
            }

            Log.d("FileUpload", "=== Post Process SUCCESS ===")
            Log.d("FileUpload", "Category ID: ${response.body()!!.data.categoryId}")
            Log.d("FileUpload", "OCR Text: ${response.body()!!.data.ocrText?.take(50)}...")

            Result.Success(response.body()!!.message)

        } catch (e: UnknownHostException) {
            Log.e("FileUpload", "Network error during post process", e)
            Result.Error(
                exception = e,
                message = "인터넷 연결을 확인해주세요",
                errorType = ErrorType.NETWORK
            )
        } catch (e: SocketTimeoutException) {
            Log.e("FileUpload", "Timeout during post process", e)
            Result.Error(
                exception = e,
                message = "요청 시간이 초과되었습니다",
                errorType = ErrorType.TIMEOUT
            )
        } catch (e: Exception) {
            Log.e("FileUpload", "Post process failed with exception", e)
            Result.Error(
                exception = e,
                message = e.message ?: "파일 분석 실패",
                errorType = ErrorType.UNKNOWN
            )
        }
    }
}