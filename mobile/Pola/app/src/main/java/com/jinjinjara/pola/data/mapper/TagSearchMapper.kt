package com.jinjinjara.pola.data.mapper

import com.jinjinjara.pola.data.remote.dto.response.TagSearchResult
import com.jinjinjara.pola.domain.model.TagSearchFile

fun TagSearchResult.toDomain(): TagSearchFile {
    return TagSearchFile(
        fileId = fileId,
        userId = userId,
        categoryName = categoryName,
        tags = tags.split(",").map { it.trim() },
        context = context,
        ocrText = ocrText,
        imageUrl = imageUrl,
        createdAt = createdAt
    )
}
