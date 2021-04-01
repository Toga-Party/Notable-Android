package me.togaparty.notable_android.data

import android.app.Application
import android.content.Intent
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
import me.togaparty.notable_android.BuildConfig
import me.togaparty.notable_android.data.files.FileWorker
import me.togaparty.notable_android.data.network.RetrofitWorker
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import me.togaparty.notable_android.utils.Status


class ImageListProvider(app: Application) : AndroidViewModel(app) {

    private val fileWorker = FileWorker(getApplication())

    private val retrofitWorker = RetrofitWorker(getApplication())

    private val newList = arrayListOf<GalleryImage>()

    private var processingStatus = Status.AVAILABLE

    private val imageList: MutableLiveData<List<GalleryImage>> by lazy {
        val data = MutableLiveData<List<GalleryImage>>().apply {
            value = mutableListOf()
            viewModelScope.launch {
                newList += fileWorker.loadImages()
            }
            value = newList
        }
        data
    }
    suspend fun copyImageToList(intent: Intent) {
        val uri = intent.data!!
        var returnedID: Long = -1
        lateinit var returnedName: String

        fun checkForDuplicates() : Boolean {
            fileWorker.query(collection = uri, selection = null, selectionArgs = null)?.use {
                cursor ->
                cursor.moveToFirst()
                returnedID = cursor.getLong(0)
                returnedName = cursor.getString(1)
            }
            return !newList.any { galleryImage ->
                galleryImage.name == returnedName && galleryImage.id != (-1).toLong() && galleryImage.id == returnedID
            }
        }
        if(checkForDuplicates()) {
            val returnedImage = fileWorker.copyImage(returnedName, uri)
            if(returnedImage != null) {
                withContext(Dispatchers.Main) {
                    addToList(returnedImage)
                }
            }
        }
    }
    fun refreshList() {
        newList.clear()
        imageList.value = newList
        viewModelScope.launch {
            newList += fileWorker.loadImages()
        }
        imageList.value = newList
    }

    suspend fun uploadImage(image: GalleryImage, position: Int)  {

        processingStatus = Status.PROCESSING
        var returnedImage: GalleryImage? = null
        var message = "Upload failed:"

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
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "Returned image wavfiles count: ${returnedImage?.wavFiles?.size}")
            Log.d(TAG, "Returned image textfiles count: ${returnedImage?.textFiles?.size}")
            Log.d(TAG, "Returned image imagefiles count: ${returnedImage?.imageFiles?.size}")
        }
        value.await()
        returnedImage?.let { returned ->
            newList[position] = returned.copy(
                imageFiles = returned.imageFiles.toMutableMap(),
                textFiles = returned.textFiles.toMutableMap(),
                wavFiles = returned.wavFiles.toMutableMap(),
            )
        }
        withContext(Dispatchers.Main) {
            imageList.value = newList
        }
    }

    fun getProcessingStatus() = processingStatus

    fun setProcessingStatus(status: Status) { processingStatus = status}

    fun getImageListSize(): Int = newList.size

    fun getGalleryImage(position: Int): GalleryImage = newList[position]

    fun getList() : LiveData<List<GalleryImage>> = imageList

    fun saveImageToStorage(filename: String, fileUri: Uri): GalleryImage? {
        return fileWorker.saveImage(filename, fileUri)
    }

    fun addToList(image: GalleryImage) {
        newList.add(image)
        imageList.value = newList
    }

    @Throws(SecurityException::class)
    fun deleteGalleryImage(position: Int, fileUri: Uri) {
        newList.removeAt(position)
        fileWorker.deleteImage(fileUri)
        imageList.value = newList
    }




}