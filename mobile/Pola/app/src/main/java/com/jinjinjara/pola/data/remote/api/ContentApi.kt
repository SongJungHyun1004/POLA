package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.request.ShareRequest
import com.jinjinjara.pola.data.remote.dto.response.FileDetailResponse
import com.jinjinjara.pola.data.remote.dto.response.ShareResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ContentApi {
    /**
     * 파일 단건의 상세 정보를 반환
     * 호출 시 해당 파일의 조회수가 1 증가하고, 마지막 열람 시각(lastViewedAt)이 갱신됨
     */
    @GET("files/{fileId}")
    suspend fun getFileDetail(
        @Path("fileId") fileId: Long
    ): Response<FileDetailResponse>

    @DELETE("files/{fileId}")
    suspend fun deleteFile(
        @Path("fileId") fileId: Long
    ): Response<Unit>

    /**
     * 파일 공유 링크 생성
     */
    @POST("files/{fileId}/share")
    suspend fun createShareLink(
        @Path("fileId") fileId: Long,
        @Body request: ShareRequest
    ): Response<ShareResponse>

}