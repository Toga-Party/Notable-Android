package me.togaparty.notable_android.data

import android.net.Uri

data class GalleryImage (
        val imageUrl: Uri,
        val name: String,
        var processed: Boolean? = false,
        var textFiles: Map<String,Uri> = linkedMapOf(),
        var wavFiles: Map<String,Uri> = linkedMapOf(),
        var imageFiles: Map<String,Uri> = linkedMapOf(),
) {
    //TODO: I should probably use generics for this. Maybe when the app is working I can do some refactoring.
    fun addTextFiles(map: Map<String, Uri>) {
        (textFiles as LinkedHashMap).putAll(map.toMutableMap())
    }
    fun addTextFile(name: String, uri: Uri) {
        (textFiles as LinkedHashMap)[name] = uri
    }
    fun addWavFiles(map: Map<String, Uri>) {
        (wavFiles as LinkedHashMap).putAll(map.toMutableMap())
    }
    fun addWAVFile(name: String, uri: Uri) {
        (wavFiles as LinkedHashMap)[name] = uri
    }
    fun addImageFiles(map: Map<String, Uri>) {
        (imageFiles as LinkedHashMap).putAll(map.toMutableMap())
    }
    fun addImageFile(name: String, uri: Uri) {
        (imageFiles as LinkedHashMap)[name] = uri
    }
}