package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.request.FileCompleteRequest
import com.jinjinjara.pola.data.remote.dto.response.FileCompleteResponse
import com.jinjinjara.pola.data.remote.dto.response.FilePostProcessResponse
import com.jinjinjara.pola.data.remote.dto.response.PresignedUrlResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface FileUploadApi {

    @GET("s3/presigned/upload")
    suspend fun getPresignedUrl(
        @Query("fileName") fileName: String
    ): Response<PresignedUrlResponse>

    @PUT
    suspend fun uploadToS3(
        @Url url: String,
        @Body file: RequestBody,
        @Header("Content-Type") contentType: String
    ): Response<Unit>

    @POST("files/complete")
    suspend fun completeUpload(
        @Body request: FileCompleteRequest
    ): Response<FileCompleteResponse>

    @POST("files/{fileId}/post-process")
    suspend fun postProcessFile(
        @Path("fileId") fileId: Long
    ): Response<FilePostProcessResponse>
}