package com.jinjinjara.pola.widget.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 위젯 상태 데이터 클래스
 */
@JsonClass(generateAdapter = true)
data class WidgetState(
    @Json(name = "currentIndex")
    val currentIndex: Int = 0,

    @Json(name = "remindItems")
    val remindItems: List<WidgetRemindItem> = emptyList(),

    @Json(name = "lastUpdated")
    val lastUpdated: Long = System.currentTimeMillis(),

    @Json(name = "isLoading")
    val isLoading: Boolean = false,

    @Json(name = "errorMessage")
    val errorMessage: String? = null
)

/**
 * 위젯에 표시될 리마인드 아이템
 */
@JsonClass(generateAdapter = true)
data class WidgetRemindItem(
    @Json(name = "fileId")
    val fileId: Long,

    @Json(name = "imageUrl")
    val imageUrl: String,

    @Json(name = "isFavorite")
    val isFavorite: Boolean,

    @Json(name = "localImagePath")
    val localImagePath: String? = null,

    @Json(name = "tags")
    val tags: List<String> = emptyList(),

    @Json(name = "type")
    val type: String = "image",

    @Json(name = "context")
    val context: String = ""
)
