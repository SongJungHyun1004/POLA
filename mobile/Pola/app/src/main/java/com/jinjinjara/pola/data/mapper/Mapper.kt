package com.jinjinjara.pola.data.mapper

import com.jinjinjara.pola.data.remote.dto.response.UserResponse
import com.jinjinjara.pola.domain.model.User

/**
 * UserResponse를 User 도메인 모델로 변환
 */
fun UserResponse.toUser(): User {
    return User(
        id = id,
        email = email,
        displayName = displayName,
        profileImageUrl = profileImageUrl,
        createdAt = createdAt
    )
}