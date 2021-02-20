package me.togaparty.notable_opencv.network

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RetrofitUploader : ViewModel() {

    suspend fun uploadFile(file: File, fileUri: Uri) {
        Log.d("Files", "Upload File to ${WebURL.url}")
        val serviceWorker = RetrofitWorker.getRetrofit()
        val requestFile = file
            .asRequestBody((MimeTypeMap.getFileExtensionFromUrl(fileUri.toString())
                ?.run{ MimeTypeMap.getSingleton().getMimeTypeFromExtension(this)}
                ?: "image/*"
                    ).toMediaTypeOrNull())
        Log.d("FileUtils", requestFile.contentType().toString())
        val fileName = file.name.toRequestBody("text/plain".toMediaTypeOrNull())
        val image = MultipartBody.Part.createFormData("file", file.name, requestFile)

        withContext(Dispatchers.IO) {
            serviceWorker.create(RetrofitService::class.java).upload(image, fileName)
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
                        Log.e("Upload", t.message, t)
                    }
                } )
        }

    }
}