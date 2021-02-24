package me.togaparty.notable_opencv.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import me.togaparty.notable_opencv.adapter.GalleryImage

class ImageListProvider (application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    val context: Context = getApplication<Application>().applicationContext
    private val fileWorker = FileWorker()
    val imageList = MutableLiveData<ArrayList<GalleryImage>>().apply {
        value = ArrayList()
        value?.addAll(fileWorker.loadImages(context))
        Log.d("ImageProvider", "INITIALIZING LIST")
    }

    fun addtoList(image: GalleryImage) {
        imageList.value?.add(image)
    }
    fun getImageListSize(): Int? {
        return imageList.value?.size
    }
    fun getGalleryImage(position: Int): GalleryImage? {
        return imageList.value?.get(position)
    }

    fun deleteGalleryImage(position: Int) {
        imageList.value?.removeAt(position)
        imageList.value = imageList.value
    }
}