package com.jinjinjara.pola.domain.model

data class UserCategory(
    val id: Long = -1,
    val categoryName: String = "전체",
    val fileCount: Int = 0,
    val createdAt: String = "",
    val userEmail: String = ""
)
data class Category(
    val id: Long,
    val name: String,
    val sort: Int
)
