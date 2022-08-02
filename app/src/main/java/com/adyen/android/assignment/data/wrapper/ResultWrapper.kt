package com.adyen.android.assignment.data.wrapper

import com.adyen.android.assignment.domain.errorhandling.BaseException

sealed class ResultWrapper<out T> {
    data class Success<out T>(val data: T?) : ResultWrapper<T>()
    data class Failure(val exception: BaseException) : ResultWrapper<Nothing>()
}