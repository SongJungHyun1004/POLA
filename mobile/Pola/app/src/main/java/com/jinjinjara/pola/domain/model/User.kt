package com.jinjinjara.pola.domain.model

/**
 * 사용자 도메인 모델
 */
data class User(
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)