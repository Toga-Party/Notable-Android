@file:Suppress("BlockingMethodInNonBlockingContext")

package me.togaparty.notable_android.data

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
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
    fun copyImageToList(intent: Intent): Status {
        val uri = intent.data!!
        lateinit var returnedName: String
        fun checkForDuplicates() : Boolean {
            fileWorker.query(collection = uri, selection = null, selectionArgs = null)?.use {
                cursor ->
                cursor.moveToFirst()
                returnedName = cursor.getString(1)
            }
            newList.forEach {
                Log.d(TAG, "${it.name} == $returnedName")

            }
            return !newList.any { galleryImage ->
                galleryImage.name == returnedName
            }
        }
        if(checkForDuplicates()) {
            fileWorker.copyImage(returnedName, uri)
        } else {
            return Status.CONFLICT
        }
        return Status.SUCCESSFUL
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
        withContext(Dispatchers.Main) {
            imageList.value = newList
        }


        var returnedImage: GalleryImage? = null
        var message = "Upload failed:"

        val value = viewModelScope.async(context = Dispatchers.IO) {
            when(val returnedValue = retrofitWorker.uploadFile(image)) {
                is RetrofitWorker.UploadResult.Success -> {
                    returnedImage = returnedValue.retrieved
                }
                is RetrofitWorker.UploadResult.Error -> {
                    message += returnedValue.message
                    Log.e(TAG, message, returnedValue.cause)
                    processingStatus = Status.FAILED
                }
            }
        }
        value.await()
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "Returned image wavfiles count: ${returnedImage?.wavFiles?.size}")
            Log.d(TAG, "Returned image textfiles count: ${returnedImage?.textFiles?.size}")
            Log.d(TAG, "Returned image imagefiles count: ${returnedImage?.imageFiles?.size}")
        }
        if(processingStatus != Status.FAILED) {
            returnedImage?.let { returned ->

                processingStatus = Status.EXTRACTING_DATA

                withContext(Dispatchers.Main) {
                    imageList.value = newList
                }

                newList[position] = returned.copy(
                    imageFiles = returned.imageFiles.toMutableMap(),
                    textFiles = returned.textFiles.toMutableMap(),
                    wavFiles = returned.wavFiles.toMutableMap(),
                )
                Thread.sleep(2000) //I am blocking the UI anyway, then just block everything all together.
                //Really hacky solution for flashing screen issue.
            }
            withContext(Dispatchers.Main) {
                processingStatus = Status.SUCCESSFUL
                imageList.value = newList
            }
        }else {
            withContext(Dispatchers.Main) {

                imageList.value = newList
            }
        }
    }

    fun getProcessingStatus() = processingStatus

    fun setProcessingStatus(status: Status) { processingStatus = status}

    fun getImageListSize(): Int = newList.size

    fun getGalleryImage(position: Int): GalleryImage = newList[position]

    fun getList() : LiveData<List<GalleryImage>> = imageList

    fun saveImageToStorage(filename: String, fileUri: Uri) {
        fileWorker.saveImage(filename, fileUri)
    }

    @Throws(SecurityException::class)
    fun deleteGalleryImage(position: Int, fileUri: Uri) {
        fileWorker.deleteImage(fileUri)
        newList.removeAt(position)
        imageList.value = newList
    }




}