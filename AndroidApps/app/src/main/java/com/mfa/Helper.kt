package com.mfa

sealed class Helper <out R>private constructor(){
    data class Success<out T>(
        val data:T
    ):Helper<T>()

    data class Error<out E>(
        val eror:E
    ):Helper<E>()

    object Loading : Helper<Nothing>()
}
