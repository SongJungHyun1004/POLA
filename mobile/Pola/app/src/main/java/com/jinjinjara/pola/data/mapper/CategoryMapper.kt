package com.jinjinjara.pola.data.mapper

import com.jinjinjara.pola.data.remote.dto.response.UserCategoryDto
import com.jinjinjara.pola.domain.model.UserCategory

fun UserCategoryDto.toDomain(): UserCategory {
    return UserCategory(
        id = id,
        categoryName = categoryName,
        fileCount = fileCount,
        createdAt = createdAt,
        userEmail = userEmail
    )
}