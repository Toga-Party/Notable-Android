package me.togaparty.notable_opencv.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class SharedViewModel(): ViewModel() {
    var imageCount = MutableLiveData<Int>()

    fun setImageCount(count: Int) {
        imageCount.value = count
    }

}