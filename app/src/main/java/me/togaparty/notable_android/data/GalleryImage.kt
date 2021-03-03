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
        if (textFiles.isNullOrEmpty()) textFiles = linkedMapOf()
        textFiles += map.toMutableMap()
    }
    fun addTextFile(name: String, uri: Uri) {
        if (textFiles.isNullOrEmpty()) textFiles = linkedMapOf()
        textFiles += Pair(name, uri)
    }
    fun addWavFiles(map: Map<String, Uri>) {
        if (wavFiles.isNullOrEmpty()) wavFiles = linkedMapOf()
        wavFiles += map.toMutableMap()
    }
    fun addWAVFile(name: String, uri: Uri) {
        if (wavFiles.isNullOrEmpty()) wavFiles = linkedMapOf()
        wavFiles += Pair(name, uri)
    }
    fun addImageFiles(map: Map<String, Uri>) {
        if (imageFiles.isNullOrEmpty()) imageFiles = linkedMapOf()
        imageFiles += map.toMutableMap()
    }
    fun addImageFile(name: String, uri: Uri) {
        if (imageFiles.isNullOrEmpty()) imageFiles = linkedMapOf()
        imageFiles += Pair(name, uri)
    }
}