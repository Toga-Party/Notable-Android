package me.togaparty.notable_android.data

import android.net.Uri

data class GalleryImage (
    val imageUrl: Uri,
    val name: String,
    var processed: Boolean? = null,
    var textFiles: Map<String,Uri>? = null,
    var wavFiles: Map<String,Uri>? = null,
    var pngFiles: Map<String,Uri>? = null,
) {
    private fun initTextFileMap(){textFiles = hashMapOf()}
    private fun initWavFileMap(){wavFiles = hashMapOf()}
    private fun initPngFileMap(){pngFiles = hashMapOf()}

    fun addTextFile(textname: String, textUri: Uri) {
        if(textFiles == null){
            initTextFileMap()
        }
        (textFiles as HashMap)[textname] = textUri
    }

    fun addWAVFile(wavname: String, wavUri: Uri) {
        if(wavFiles == null){
            initWavFileMap()
        }
        (wavFiles as HashMap)[wavname] = wavUri
    }
    fun addPNGFile(pngname: String, pngUri: Uri) {
        if(pngFiles == null){
            initPngFileMap()
        }
        (pngFiles as HashMap)[pngname] = pngUri
    }
}