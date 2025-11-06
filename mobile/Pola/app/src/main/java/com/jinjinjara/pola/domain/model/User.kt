package com.jinjinjara.pola.domain.model

/**
 * 사용자 도메인 모델
 */
data class User(
    val id: Long,
    val email: String,
    val displayName: String,
    val profileImageUrl: String? = null,
    val createdAt: String
)