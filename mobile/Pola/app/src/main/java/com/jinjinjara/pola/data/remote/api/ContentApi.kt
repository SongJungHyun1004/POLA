package com.jinjinjara.pola.data.remote.api

import com.jinjinjara.pola.data.remote.dto.response.FileDetailResponse
import retrofit2.Response
import retrofit2.http.GET
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

}