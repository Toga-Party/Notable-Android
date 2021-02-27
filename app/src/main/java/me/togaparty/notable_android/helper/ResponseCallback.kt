package me.togaparty.notable_android.helper

interface ResponseCallback<T> {
    fun onProcessFinished (output: T): T
}