package com.jinjinjara.pola.data.local.database.converter

import androidx.room.TypeConverter
import com.jinjinjara.pola.presentation.ui.screen.search.ImageData
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class ChatMessageConverters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val imageDataListType = Types.newParameterizedType(List::class.java, ImageData::class.java)
    private val imageDataListAdapter = moshi.adapter<List<ImageData>>(imageDataListType)

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { stringListAdapter.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { stringListAdapter.fromJson(it) }
    }

    @TypeConverter
    fun fromImageDataList(value: List<ImageData>?): String? {
        return value?.let { imageDataListAdapter.toJson(it) }
    }

    @TypeConverter
    fun toImageDataList(value: String?): List<ImageData>? {
        return value?.let { imageDataListAdapter.fromJson(it) }
    }
}
