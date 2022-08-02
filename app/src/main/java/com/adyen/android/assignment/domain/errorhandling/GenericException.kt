package com.adyen.android.assignment.domain.errorhandling

class GenericException: BaseException() {
    override var message: String = "Its not you, its us! Please try again after a while. :)"
}