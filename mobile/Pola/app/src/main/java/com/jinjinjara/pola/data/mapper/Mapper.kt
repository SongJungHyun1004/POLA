package com.jinjinjara.pola.data.mapper

import com.jinjinjara.pola.data.remote.dto.response.CategoryRecommendationDto
import com.jinjinjara.pola.data.remote.dto.response.UserResponse
import com.jinjinjara.pola.domain.model.CategoryRecommendation
import com.jinjinjara.pola.domain.model.User

/**
 * UserResponse를 User 도메인 모델로 변환
 * @param onboardingCompleted 온보딩 완료 여부 (oauth/token 상태코드 기반)
 */
fun UserResponse.toUser(onboardingCompleted: Boolean = false): User {
    return User(
        id = id,
        email = email,
        displayName = displayName,
        profileImageUrl = profileImageUrl,
        createdAt = createdAt,
        onboardingCompleted = onboardingCompleted
    )
}

/**
 * CategoryRecommendationDto를 CategoryRecommendation 도메인 모델로 변환
 */
fun CategoryRecommendationDto.toDomain(): CategoryRecommendation {
    return CategoryRecommendation(
        categoryName = categoryName,
        tags = tags,
        icon = null // Icon will be assigned in presentation layer
    )
}