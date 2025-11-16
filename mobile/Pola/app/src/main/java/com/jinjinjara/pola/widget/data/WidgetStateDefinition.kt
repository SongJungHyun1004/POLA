package com.jinjinjara.pola.widget.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.glance.state.GlanceStateDefinition
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Glance 위젯의 상태 정의
 * DataStore를 사용하여 위젯 상태를 영구적으로 저장
 */
object WidgetStateDefinition : GlanceStateDefinition<WidgetState> {

    private const val WIDGET_STATE_FILE = "widget_state.json"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(WidgetState::class.java)

    override suspend fun getDataStore(
        context: android.content.Context,
        fileKey: String
    ): androidx.datastore.core.DataStore<WidgetState> {
        return androidx.datastore.core.DataStoreFactory.create(
            serializer = WidgetStateSerializer,
            produceFile = {
                File(context.filesDir, "datastore/$fileKey/$WIDGET_STATE_FILE")
            }
        )
    }

    override fun getLocation(
        context: android.content.Context,
        fileKey: String
    ): File {
        return File(context.filesDir, "datastore/$fileKey/$WIDGET_STATE_FILE")
    }

    /**
     * WidgetState Serializer
     */
    private object WidgetStateSerializer : Serializer<WidgetState> {
        override val defaultValue: WidgetState
            get() = WidgetState()

        override suspend fun readFrom(input: InputStream): WidgetState {
            return try {
                withContext(Dispatchers.IO) {
                    val bytes = input.readBytes()
                    if (bytes.isEmpty()) {
                        defaultValue
                    } else {
                        adapter.fromJson(bytes.decodeToString()) ?: defaultValue
                    }
                }
            } catch (e: Exception) {
                throw CorruptionException("Cannot read widget state", e)
            }
        }

        override suspend fun writeTo(t: WidgetState, output: OutputStream) {
            withContext(Dispatchers.IO) {
                val json = adapter.toJson(t)
                output.write(json.encodeToByteArray())
            }
        }
    }
}
