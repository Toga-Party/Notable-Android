package me.togaparty.notable_android.data

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import me.togaparty.notable_android.data.files.FileWorker
import me.togaparty.notable_android.data.network.RetrofitWorker
import java.lang.Exception


class ImageListProvider(app: Application) : AndroidViewModel(app) {

    private val fileWorker = FileWorker(viewModelScope, getApplication())
    private val retrofitWorker = RetrofitWorker(getApplication())
    private val newList = arrayListOf<GalleryImage>()
    private val imageList = MutableLiveData<List<GalleryImage>>().apply {
        value = ArrayList()
        viewModelScope.launch { newList.addAll(fileWorker.loadImages()) }
        value = newList
    }

    fun uploadImage(image: GalleryImage, position: Int) {

        viewModelScope.launch(context = Dispatchers.IO) {
            var returnedImage: GalleryImage? = null
            val job = async {
                returnedImage = retrofitWorker.uploadFile(image)
            }
            Log.d("Upload", "Waiting for upload to finish")
            job.await()
            Log.d("Upload", "Upload finished")

            returnedImage?.let {
                if(it.processed == true) {
                    newList[position] = it.copy(processed = true)
                    /*newList[position] =
                        image.copy(
                                pngFiles = image.pngFiles?.toMutableMap(),
                                wavFiles = image.wavFiles?.toMutableMap(),
                                textFiles = image.textFiles?.toMutableMap(),
                        )
                    */
                    withContext(Dispatchers.Main) {
                        imageList.value = newList
                    }
                }
            }?:throw Exception("Something went wrong")
        }
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