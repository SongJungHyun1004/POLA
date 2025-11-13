package com.jinjinjara.pola.data.mapper

import com.jinjinjara.pola.data.remote.dto.response.CategoryDto
import com.jinjinjara.pola.data.remote.dto.response.CategoryRecommendationDto
import com.jinjinjara.pola.data.remote.dto.response.TimelineData
import com.jinjinjara.pola.data.remote.dto.response.TimelineFileDto
import com.jinjinjara.pola.data.remote.dto.response.UserResponse
import com.jinjinjara.pola.domain.model.Category
import com.jinjinjara.pola.domain.model.CategoryRecommendation
import com.jinjinjara.pola.domain.model.TimelineFile
import com.jinjinjara.pola.domain.model.TimelinePage
import com.jinjinjara.pola.domain.model.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

/**
 * TimelineFileDto를 TimelineFile 도메인 모델로 변환
 */
fun TimelineFileDto.toDomain(): TimelineFile {
    val dateTime = try {
        LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME)
    } catch (e: Exception) {
        // 파싱 실패 시 현재 시간 사용
        LocalDateTime.now()
    }

    return TimelineFile(
        id = id,
        imageUrl = src,
        type = type,
        context = context,
        tags = tags,
        createdAt = dateTime
    )
}

/**
 * TimelineData를 TimelinePage 도메인 모델로 변환
 */
fun TimelineData.toDomain(): TimelinePage {
    return TimelinePage(
        files = content.map { it.toDomain() },
        currentPage = page,
        totalPages = totalPages,
        isLast = last
    )
}

/**
 * CategoryDto를 Category 도메인 모델로 변환
 */
fun CategoryDto.toDomain(): Category {
    return Category(
        id = id,
        name = categoryName,
        sort = fileCount
    )
}