package com.adyen.android.assignment.domain.errorhandling

class SocketTimeOutException: BaseException() {
    override var message: String = "Something went wrong, please check your internet and try again later."
}