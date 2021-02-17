package me.togaparty.notable_opencv.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import me.togaparty.notable_opencv.network.RetrofitBuilder
import me.togaparty.notable_opencv.network.RetrofitService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
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

            val service = RetrofitBuilder.getRetrofit()
            val serviceInterface = service.create(RetrofitService::class.java)
            val requestFile = file
                .asRequestBody((MimeTypeMap.getFileExtensionFromUrl(fileUri.toString())
                    ?.run{ MimeTypeMap.getSingleton().getMimeTypeFromExtension(this)}
                    ?: "image/*"
                        ).toMediaTypeOrNull())
            Log.d("FileUtils", requestFile.contentType().toString())
            //val fileName = RequestBody.create(MediaType.parse("text/plain"), file.name)
            val image = MultipartBody.Part.createFormData("file", file.name, requestFile)

            serviceInterface.upload(image)
                .enqueue(object: Callback<ResponseBody>{
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        Log.v("Upload", "success")
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("Upload", t.message, t)
                    }
                } )
        }
    }
}

