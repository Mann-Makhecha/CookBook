package com.example.cookbook.util

/**
 * A sealed class representing the result of an operation that can either succeed or fail.
 * This is used throughout the app for handling async operations and API responses.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
    object Idle : Result<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    val isLoading: Boolean
        get() = this is Loading

    val isIdle: Boolean
        get() = this is Idle
}

/**
 * Maps the success value of a Result to a new value using the provided transform function.
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(exception)
        is Result.Loading -> Result.Loading
        is Result.Idle -> Result.Idle
    }
}

/**
 * Returns the success data or null if the Result is not Success.
 */
fun <T> Result<T>.getOrNull(): T? {
    return when (this) {
        is Result.Success -> data
        else -> null
    }
}
