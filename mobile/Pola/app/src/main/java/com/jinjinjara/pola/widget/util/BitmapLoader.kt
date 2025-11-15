package com.jinjinjara.pola.widget.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File

/**
 * Bitmap 로더 유틸리티
 */
object BitmapLoader {
    private const val TAG = "BitmapLoader"
    private const val MAX_SIZE = 512 // 위젯용 최대 크기

    /**
     * 파일 경로에서 Bitmap 로드
     */
    fun loadBitmapFromPath(path: String?): Bitmap? {
        if (path == null) {
            Log.w(TAG, "[Widget] Path is null")
            return null
        }

        return try {
            val file = File(path)
            if (!file.exists()) {
                Log.w(TAG, "[Widget] File does not exist: $path")
                return null
            }

            if (file.length() == 0L) {
                Log.w(TAG, "[Widget] File is empty (0 bytes): $path")
                return null
            }

            Log.d(TAG, "[Widget] Loading bitmap from: $path (size: ${file.length()} bytes)")

            // 먼저 이미지 크기만 확인
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            Log.d(TAG, "[Widget] Image dimensions: ${options.outWidth}x${options.outHeight}")

            // 샘플링 계산
            options.inSampleSize = calculateInSampleSize(options, MAX_SIZE, MAX_SIZE)
            options.inJustDecodeBounds = false

            Log.d(TAG, "[Widget] Using inSampleSize: ${options.inSampleSize}")

            // 실제 Bitmap 로드
            val bitmap = BitmapFactory.decodeFile(path, options)

            if (bitmap == null) {
                Log.e(TAG, "[Widget] BitmapFactory.decodeFile returned null for: $path")
                return null
            }

            Log.d(TAG, "[Widget] Bitmap loaded successfully from $path: ${bitmap.width}x${bitmap.height}")
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "[Widget] Error loading bitmap from $path", e)
            null
        }
    }

    /**
     * 샘플링 비율 계산
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
