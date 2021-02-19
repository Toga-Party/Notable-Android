package me.togaparty.notable_opencv.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import me.togaparty.notable_opencv.network.RetrofitService
import me.togaparty.notable_opencv.network.RetrofitWorker
import me.togaparty.notable_opencv.network.ServerResponse
import me.togaparty.notable_opencv.network.WebURL.Companion.url
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class FileUtils{
    companion object{
        @JvmStatic
        fun getBitmap(file: File): Bitmap
                = BitmapFactory.decodeFile(file.absolutePath)
        @JvmStatic
        fun uploadFile(file: File, fileUri: Uri) {
            Log.d("Files", "Upload File to $url")
            val serviceWorker = RetrofitWorker.getRetrofit()
            val requestFile = file
                .asRequestBody((MimeTypeMap.getFileExtensionFromUrl(fileUri.toString())
                    ?.run{ MimeTypeMap.getSingleton().getMimeTypeFromExtension(this)}
                    ?: "image/*"
                        ).toMediaTypeOrNull())
            Log.d("FileUtils", requestFile.contentType().toString())
            val fileName = file.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val image = MultipartBody.Part.createFormData("file", file.name, requestFile)
//                        MultipartBody.Builder()
//                                .addFormDataPart("file", file.name, requestFile)
//                                .build()
            serviceWorker.create(RetrofitService::class.java).upload(image, fileName)
                .enqueue(object: Callback<ServerResponse>{
                    override fun onResponse(
                        call: Call<ServerResponse>,
                        response: Response<ServerResponse>
                    ) {
                        if (response.isSuccessful) {
                            Log.v("Upload", "success ${response.body().toString()}")
                        } else {
                            Log.v("Upload", "Upload failed")
                        }
                    }
                    override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                        Log.e("Upload", t.message, t)
                    }
                } )
        }
    }
}

