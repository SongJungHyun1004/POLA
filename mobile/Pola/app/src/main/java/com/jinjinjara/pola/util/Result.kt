package com.jinjinjara.pola.util

/**
 * API 호출 결과를 래핑하는 sealed class
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(
        val exception: Exception,
        val message: String? = null
    ) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

/**
 * Result 확장 함수들
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

fun <T> Result<T>.isError(): Boolean = this is Result.Error

fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

fun <T> Result<T>.getOrNull(): T? {
    return when (this) {
        is Result.Success -> data
        else -> null
    }
}

fun <T> Result<T>.getErrorOrNull(): String? {
    return when (this) {
        is Result.Error -> message ?: exception.message
        else -> null
    }
}

suspend fun <T> Result<T>.onSuccess(action: suspend (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

suspend fun <T> Result<T>.onError(action: suspend (String?) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(message)
    }
    return this
}