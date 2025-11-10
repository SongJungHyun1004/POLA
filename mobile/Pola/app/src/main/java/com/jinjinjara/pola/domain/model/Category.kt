package com.jinjinjara.pola.domain.model

data class UserCategory(
    val id: Long,
    val categoryName: String,
    val categorySort: Int,
    val createdAt: String,
    val userEmail: String
)