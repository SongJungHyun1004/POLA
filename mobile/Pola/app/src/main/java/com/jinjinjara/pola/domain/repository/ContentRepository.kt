package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.data.remote.dto.response.FileTag
import com.jinjinjara.pola.domain.model.Category
import com.jinjinjara.pola.domain.model.CategoryRecommendation
import com.jinjinjara.pola.domain.model.FileDetail
import com.jinjinjara.pola.domain.model.FilesPage
import com.jinjinjara.pola.domain.model.ShareLink
import com.jinjinjara.pola.domain.model.UserCategory
import com.jinjinjara.pola.util.Result

interface ContentRepository {

    suspend fun getFileDetail(fileId: Long): Result<FileDetail>
    suspend fun deleteFile(fileId: Long): Result<Unit>
    suspend fun createShareLink(fileId: Long, expireHours: Int): Result<ShareLink>
    suspend fun getFileTags(fileId: Long): Result<List<FileTag>>
    suspend fun addFileTags(fileId: Long, tagNames: List<String>): Result<Unit>
    suspend fun removeFileTag(fileId: Long, tagId: Long): Result<Unit>
    suspend fun updateFileContext(fileId: Long, context: String): Result<Unit>
    suspend fun updateFileCategory(fileId: Long, categoryId: Long): Result<Unit>
}