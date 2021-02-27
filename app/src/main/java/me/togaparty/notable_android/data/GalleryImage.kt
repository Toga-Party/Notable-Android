package me.togaparty.notable_android.data

import android.net.Uri

data class GalleryImage (
    val imageUrl: Uri,
    val name: String,
    var processed: Boolean? = false,
    var textFiles: Map<String,Uri> = hashMapOf(),
    var wavFiles: Map<String,Uri> = hashMapOf(),
    var imageFiles: Map<String,Uri> = hashMapOf(),
) {
    fun addTextFiles(map: HashMap<String, Uri>) {
        (textFiles as HashMap).putAll(map.toMutableMap())
    }

    //TODO: I should probably use generics for this. Maybe when the app is working I can do some refactoring.
    fun addTextFile(name: String, uri: Uri) {
        (textFiles as HashMap)[name] = uri
    }
    fun addWavFiles(map: HashMap<String, Uri>) {
        (wavFiles as HashMap).putAll(map.toMutableMap())
    }
    fun addWAVFile(name: String, uri: Uri) {
        (wavFiles as HashMap)[name] = uri
    }
    fun addImageFiles(map: HashMap<String, Uri>) {
        (imageFiles as HashMap).putAll(map.toMutableMap())
    }
    fun addImageFile(name: String, uri: Uri) {
        (imageFiles as HashMap)[name] = uri
    }
}