package com.jinjinjara.pola.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.jinjinjara.pola.data.remote.dto.response.FileDetailData
import com.jinjinjara.pola.data.remote.dto.response.FileTag
import com.jinjinjara.pola.domain.model.FileDetail
import com.jinjinjara.pola.domain.model.Tag
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * FileDetailData를 FileDetail로 변환
 */
@RequiresApi(Build.VERSION_CODES.O)
fun FileDetailData.toDomain(): FileDetail {
    return FileDetail(
        id = id,
        userId = userId,
        categoryId = categoryId,
        src = src,
        type = type,
        context = context,
        ocrText = ocrText,
        vectorId = vectorId,
        fileSize = fileSize,
        shareStatus = shareStatus,
        favorite = favorite,
        favoriteSort = favoriteSort,
        favoritedAt = favoritedAt?.let { parseDateTime(it) },
        views = views,
        platform = platform,
        originUrl = originUrl,
        createdAt = parseDateTime(createdAt),
        lastViewedAt = lastViewedAt?.let { parseDateTime(it) },
        tags = tags?.map { it.toDomain() } ?: emptyList()

    )
}

/**
 * FileTag를 Tag로 변환
 */
fun FileTag.toDomain(): Tag {
    return Tag(
        id = id,
        name = tagName
    )
}

/**
 * 다양한 날짜 문자열을 LocalDateTime으로 변환 (오류 없는 안전 버전)
 */
@RequiresApi(Build.VERSION_CODES.O)
private fun parseDateTime(dateString: String): LocalDateTime {
    return try {
        OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime()
    } catch (_: Exception) {
        try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (_: Exception) {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                LocalDateTime.parse(dateString, formatter)
            } catch (_: Exception) {
                LocalDateTime.now()
            }
        }
    }
}
