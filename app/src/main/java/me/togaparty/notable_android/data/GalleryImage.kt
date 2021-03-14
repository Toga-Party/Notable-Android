package me.togaparty.notable_android.data

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class GalleryImage (
        val imageUrl: Uri,
        val name: String,
        var processed: Boolean? = false,
        var textFiles: Map<String,Uri> = linkedMapOf(),
        var wavFiles: Map<String,Uri> = linkedMapOf(),
        var imageFiles: Map<String,Uri> = linkedMapOf(),
) : Parcelable {
    fun addTextFiles(map: Map<String, Uri>) {
        if (textFiles.isNullOrEmpty()) textFiles = linkedMapOf()
        textFiles += map.toMutableMap()
    }
    fun addWavFiles(map: Map<String, Uri>) {
        if (wavFiles.isNullOrEmpty()) wavFiles = linkedMapOf()
        wavFiles += map.toMutableMap()
    }
    fun addImageFiles(map: Map<String, Uri>) {
        if (imageFiles.isNullOrEmpty()) imageFiles = linkedMapOf()
        imageFiles += map.toMutableMap()
    }
}