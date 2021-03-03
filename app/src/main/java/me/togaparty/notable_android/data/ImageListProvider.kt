package me.togaparty.notable_android.data

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.togaparty.notable_android.data.files.FileWorker
import me.togaparty.notable_android.data.network.RetrofitWorker
import me.togaparty.notable_android.ui.items.Status
import me.togaparty.notable_android.utils.Constants.Companion.TAG



class ImageListProvider(app: Application) : AndroidViewModel(app) {

    private val fileWorker = FileWorker(getApplication())
    private val retrofitWorker = RetrofitWorker(getApplication())
    private val newList = arrayListOf<GalleryImage>()

    //private var processing = false
    //private var result: Result? = null
   // private var processingStatus =
    private var processingTag: Uri? = null
    private var processingStatus = Status.AVAILABLE
    private val imageList = MutableLiveData<List<GalleryImage>>().apply {
        value = ArrayList()
        viewModelScope.launch { newList.addAll(fileWorker.loadImages()) }
        value = newList
    }

    suspend fun uploadImage(image: GalleryImage, position: Int)  {
        var returnedImage: GalleryImage? = null
        processingStatus = Status.PROCESSING
        processingTag = image.imageUrl
        var message = "Upload failed: "
        val value = viewModelScope.async(context = Dispatchers.IO) {
            when(val returnedValue = retrofitWorker.uploadFile(image)) {
                is RetrofitWorker.UploadResult.Success -> {
                    returnedImage = returnedValue.retrieved
                    processingStatus = Status.SUCCESSFUL
                }
                is RetrofitWorker.UploadResult.Error -> {
                    message += returnedValue.message
                    Log.e(TAG, message, returnedValue.cause)
                    processingStatus = Status.FAILED
                }
            }
        }
        value.await()
        returnedImage?.let {
            returned ->
            newList[position] = returned.copy(
                    imageFiles = returned.imageFiles.toMutableMap(),
                    textFiles =  returned.textFiles.toMutableMap(),
                    wavFiles =  returned.wavFiles.toMutableMap(),
            )
        }
        withContext(Dispatchers.Main) {
            imageList.value = newList
        }
        processingTag = null
    }


    fun getProcessingStatus() = processingStatus
    fun setProcessingStatus(status: Status) { processingStatus = status}
    fun getProcessingTag() = processingTag
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