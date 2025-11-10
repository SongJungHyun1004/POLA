package com.jinjinjara.pola.domain.repository

import com.jinjinjara.pola.util.Result
import android.net.Uri

interface FileUploadRepository {
    suspend fun uploadFile(uri: Uri, fileName: String, contentType: String): Result<String>
    suspend fun uploadText(text: String, fileName: String): Result<String>
    suspend fun postProcessFile(fileId: Long): Result<String>
}