package com.adyen.android.assignment.domain.errorhandling

abstract class BaseException : RuntimeException(){
    override var message: String = "Its not you, its us! Please try again after a while. :)"
}