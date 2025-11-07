package com.jinjinjara.pola.data.remote.dto.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryRecommendationDto(
    @Json(name = "categoryName")
    val categoryName: String,

    @Json(name = "tags")
    val tags: List<String>
)

@JsonClass(generateAdapter = true)
data class CategoryRecommendationsData(
    @Json(name = "recommendations")
    val recommendations: List<CategoryRecommendationDto>
)

// Using existing OAuthApiResponse wrapper
typealias CategoryRecommendationsResponse = OAuthApiResponse<CategoryRecommendationsData>