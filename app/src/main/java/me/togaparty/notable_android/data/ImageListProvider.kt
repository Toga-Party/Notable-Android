package me.togaparty.notable_android.data

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import me.togaparty.notable_android.data.files.FileWorker
import me.togaparty.notable_android.data.network.RetrofitWorker
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import java.io.IOException
import java.lang.Exception


class ImageListProvider(app: Application) : AndroidViewModel(app) {

    private val fileWorker = FileWorker(getApplication())
    private val retrofitWorker = RetrofitWorker(getApplication())
    private val newList = arrayListOf<GalleryImage>()
    private val imageList = MutableLiveData<List<GalleryImage>>().apply {
        value = ArrayList()
        viewModelScope.launch { newList.addAll(fileWorker.loadImages()) }
        value = newList
    }

    suspend fun uploadImage(image: GalleryImage, position: Int)  {
        var returnedImage: GalleryImage? = null
        val value = GlobalScope.async {
            returnedImage = retrofitWorker.uploadFile(image)
        }
        value.await()
        returnedImage?.let {
            returned ->
            newList[position] = returned.copy(
                    imageFiles = returned.imageFiles.toMutableMap(),
                    textFiles =  returned.textFiles.toMutableMap(),
                    wavFiles =  returned.wavFiles.toMutableMap(),
            )
            withContext(Dispatchers.Main) {
                imageList.value = newList
            }
        }?: throw IOException("Upload failed")

    }
    fun saveImageToStorage(directory: String, filename: String, fileUri: Uri): GalleryImage? {
        return fileWorker.saveImage(directory, filename, fileUri)
    }
    fun getList() : LiveData<List<GalleryImage>> = imageList

    fun addToList(image: GalleryImage) {
        newList.add(image)
        imageList.value = newList
    }
    fun deleteGalleryImage(position: Int, fileUri: Uri) {
        newList.removeAt(position)
        fileWorker.deleteImage(fileUri)
        imageList.value = newList
    }
    fun getImageListSize(): Int {
        return newList.size
    }
    fun getGalleryImage(position: Int): GalleryImage {
        return newList[position]
    }

}