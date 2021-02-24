package me.togaparty.notable_opencv.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.togaparty.notable_opencv.adapter.GalleryImage
import me.togaparty.notable_opencv.utils.toast
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetrofitWorker {

    fun uploadFile(image: GalleryImage, context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val serviceWorker = RetrofitBuilder.getRetrofit()
                val filename = image.name.toRequestBody("text/plain".toMediaTypeOrNull())

                var imageToSend: MultipartBody.Part?
                context.contentResolver.openInputStream(image.imageUrl).use { input ->
                    val imageType = context.contentResolver.getType(image.imageUrl)


                    Log.d("Retrofit", "Sending image type: $imageType")
                     imageToSend = input?.let {

                        MultipartBody.Part.createFormData(
                            "file",
                            image.name,
                            it.readBytes().toRequestBody(imageType!!.toMediaTypeOrNull()))

                    }
                }
                imageToSend?.let {
                    serviceWorker.create(RetrofitService::class.java).upload(it, filename)
                            .enqueue(object: Callback<ServerResponse> {
                                override fun onResponse(
                                        call: Call<ServerResponse>,
                                        response: Response<ServerResponse>
                                ) {
                                    Log.d("Response", response.code().toString())
                                    if (response.isSuccessful) {
                                        Log.v("Upload", "success ${response.body().toString()}")
                                    } else {
                                        Log.v("Upload", "Upload failed")
                                    }
                                }

                                override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                                    Log.e("Upload", "Something went wrong: ${t.printStackTrace()}", t)
                                }
                            } )
                } ?: context.toast("Upload failed")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}