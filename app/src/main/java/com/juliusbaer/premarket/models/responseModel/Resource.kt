package com.juliusbaer.premarket.models.responseModel

sealed class Resource<T> {
    //data class Progress<T>(var loading: Boolean) : Result<T>()
    data class Success<T>(var data: T) : Resource<T>()
    data class Failure<T>(private val throwable: Throwable, val data: T? = null) : Resource<T>() {
        var hasBeenHandled = false
            private set // Allow external read but not write

        /**
         * Returns the content and prevents its use again.
         */
        fun e(): Throwable? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                throwable
            }
        }
    }

    companion object {
        //fun <T> loading(isLoading: Boolean): Result<T> = Progress(isLoading)
        fun <T> success(data: T): Resource<T> = Success(data)
        fun <T> failure(e: Throwable, data: T? = null): Resource<T> = Failure(e, data)
    }
}