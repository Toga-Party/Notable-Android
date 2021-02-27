package me.togaparty.notable_android.data

import android.net.Uri

data class GalleryImage (
    val imageUrl: Uri,
    val name: String,
    var processed: Boolean? = null,
    var textFiles: Map<String,Uri>? = null,
    var wavFiles: Map<String,Uri>? = null,
    var imageFiles: Map<String,Uri>? = null,
) {
    private fun initTextFileMap(){textFiles = hashMapOf()}
    private fun initWavFileMap(){wavFiles = hashMapOf()}
    private fun initPngFileMap(){imageFiles = hashMapOf()}

    fun addTextFiles(map: HashMap<String, Uri>) {
        if(textFiles == null){
            initTextFileMap()
        }
        (textFiles as HashMap).putAll(map.toMutableMap())
    }

    //TODO: I should probably use generics for this. Maybe when the app is working I can do some refactoring.
    fun addTextFile(name: String, uri: Uri) {
        if(textFiles == null){
            initTextFileMap()
        }
        (textFiles as HashMap)[name] = uri
    }
    fun addWavFiles(map: HashMap<String, Uri>) {
        if(wavFiles == null){
            initWavFileMap()
        }
        (wavFiles as HashMap).putAll(map.toMutableMap())
    }
    fun addWAVFile(name: String, uri: Uri) {
        if(wavFiles == null){
            initWavFileMap()
        }
        (wavFiles as HashMap)[name] = uri
    }
    fun addImageFiles(map: HashMap<String, Uri>) {
        if(imageFiles == null){
            initWavFileMap()
        }
        (imageFiles as HashMap).putAll(map.toMutableMap())
    }
    fun addImageFile(name: String, uri: Uri) {
        if(imageFiles == null){
            initPngFileMap()
        }
        (imageFiles as HashMap)[name] = uri
    }
}