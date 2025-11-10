package com.jinjinjara.pola.data.mapper

import com.jinjinjara.pola.data.remote.dto.response.FileItemDto
import com.jinjinjara.pola.data.remote.dto.response.FilesPageData
import com.jinjinjara.pola.domain.model.FileItem
import com.jinjinjara.pola.domain.model.FilesPage

fun FileItemDto.toDomain(): FileItem {
    return FileItem(
        fileId = id,
        src = src,
        type = type,
        context = context,
        favorite = favorite,
        tags = tags,
        createdAt = createdAt
    )
}

fun FilesPageData.toDomain(): FilesPage {
    return FilesPage(
        content = content.map { it.toDomain() },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
        last = last
    )
}