package me.togaparty.notable_android.utils


sealed class UploadResult<out T> {
    data class Success <out R>(val message: R): UploadResult<R>()
    data class Error(val message: String, val cause: Exception? = null) : UploadResult<Nothing>()
}
