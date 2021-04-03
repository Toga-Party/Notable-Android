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
import me.togaparty.notable_android.data.files.FileWorker
import me.togaparty.notable_android.data.network.RetrofitWorker
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import me.togaparty.notable_android.utils.Status
import me.togaparty.notable_android.utils.UploadResult


class ImageListProvider(app: Application) : AndroidViewModel(app) {

    private val fileWorker = FileWorker(getApplication())

    private val retrofitWorker = RetrofitWorker(getApplication())

    private val _states = MutableLiveData<Pair<Status, UploadResult<String>?>>()
    val states: LiveData<Pair<Status, UploadResult<String>?>> = _states

    private val _imageList: MutableLiveData<List<GalleryImage>> by lazy {
        val data = MutableLiveData<List<GalleryImage>>().apply { //Could've created a separate logic for this. This might do for now.
            value = mutableListOf()
            viewModelScope.launch {
                imageList += fileWorker.loadImages()
            }
            value = imageList
        }
        data
    }
    val imageList = arrayListOf<GalleryImage>()

    fun copyImageToList(intent: Intent): Status {
        val uri = intent.data!!
        lateinit var returnedName: String
        fun checkForDuplicates() : Boolean {
            fileWorker.query(collection = uri, selection = null, selectionArgs = null)?.use {
                cursor ->
                cursor.moveToFirst()
                returnedName = cursor.getString(1)
            }
            imageList.forEach {
                Log.d(TAG, "${it.name} == $returnedName")

            }
            return !imageList.any { galleryImage ->
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
        imageList.clear()
        _imageList.value = imageList
        viewModelScope.launch {
            imageList += fileWorker.loadImages()
        }
        _imageList.value = imageList
    }

    suspend fun uploadImage(image: GalleryImage, position: Int) {
        val value = viewModelScope.async(Dispatchers.IO) {
            coroutineScope {
                launch(Dispatchers.IO) {
                    try {
                        _states.postValue(Pair(Status.PROCESSING, null))
                        retrofitWorker.uploadFile(image).let { returned ->

                            _states.postValue(Pair(Status.EXTRACTING_DATA, null))
                            delay(1800)

                            imageList[position] = returned.copy(
                                    imageFiles = returned.imageFiles.toMutableMap(),
                                    textFiles = returned.textFiles.toMutableMap(),
                                    wavFiles = returned.wavFiles.toMutableMap(),
                            )
                            _states.postValue(Pair(Status.SUCCESSFUL, UploadResult.Success(message = "Upload successful")))
                            delay(1800)
                            withContext(Dispatchers.Main) {
                                _imageList.value = imageList
                            }
                        }
                    } catch (ex: Exception) {
                        _states.postValue(Pair(Status.FAILED, UploadResult.Error(message = "Something went wrong: ${ex.message}", ex)))
                    }
                }
            }
        }
        value.await()
    }
    fun resetStatus() = _states.postValue(Pair(Status.NIL,null))
    fun getImageListSize(): Int = imageList.size
    fun getGalleryImage(position: Int): GalleryImage = imageList[position]
    fun getList() : LiveData<List<GalleryImage>> = _imageList

    fun saveImageToStorage(filename: String, fileUri: Uri) {
        fileWorker.saveImage(filename, fileUri)
    }

    @Throws(SecurityException::class)
    fun deleteGalleryImage(position: Int, fileUri: Uri) {
        fileWorker.deleteImage(fileUri)
        imageList.removeAt(position)
        _imageList.value = imageList
    }




}