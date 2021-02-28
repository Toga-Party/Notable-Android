package me.togaparty.notable_android.data.network

import android.content.Context
import android.net.Uri
import android.util.Log
import me.togaparty.notable_android.MainActivity
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedOutputStream
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.util.zip.ZipInputStream


class RetrofitWorker(val context: Context) {

    fun uploadFile(currentImage: GalleryImage): GalleryImage {
        val image = GalleryImage(
                imageUrl = currentImage.imageUrl,
                name = currentImage.name,
            )
            val serviceWorker = RetrofitBuilder.getRetrofit()
            val filename = image.name.toRequestBody("text/plain".toMediaTypeOrNull())
            var imageToSend: MultipartBody.Part?
            context.contentResolver.openInputStream(image.imageUrl).use { input ->
                val imageType = context.contentResolver.getType(image.imageUrl)
                imageToSend = input?.let {
                    MultipartBody.Part.createFormData(
                        "file",
                        image.name,
                        it.readBytes().toRequestBody(imageType!!.toMediaTypeOrNull()))
                }
            }

//            val callback = object : Callback<ResponseBody> {
//                override fun onResponse(
//                        call: Call<ResponseBody>,
//                        response: Response<ResponseBody>
//                ) {
//
//                }
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    Log.e(TAG, "Something went wrong: ${t.printStackTrace()}", t)
//                    throw Exception("Failed to get response from the server: ${t.printStackTrace()}")
//                }
//            }
            Log.d(TAG, "Retrofit: Uploading the image")
            val response = imageToSend?.let { sendImage -> serviceWorker.create(RetrofitService::class.java)
                .upload(sendImage, filename)
                .execute()
            }
            if (response != null) {
                if (response.isSuccessful) {
                    Log.v(TAG, "Retrofit: Success response received")
                    ZipInputStream(response.body()?.byteStream()).use { zip -> var entry = zip.nextEntry
                        while (entry != null) {
                            val outputDirectory = MainActivity.externalAppSpecificStorage(context)

                            val temp = when (File(entry.name).extension) {
                                "png", "jpg" ->
                                    File(
                                        outputDirectory,
                                            File(image.name).nameWithoutExtension + separator +"images")
                                            .apply{mkdirs()}
                                else ->
                                    File(
                                        outputDirectory,
                                        File(image.name).nameWithoutExtension +
                                                separator +
                                                File(entry.name).extension).apply{mkdirs()}
                            }

                            val send = File(temp,entry.name)

                            when (send.extension) {
                                "wav" -> image.addWAVFile(send.nameWithoutExtension, Uri.fromFile(send))
                                "png" -> image.addImageFile(send.nameWithoutExtension,Uri.fromFile(send))
                                "txt" -> image.addTextFile(send.nameWithoutExtension,Uri.fromFile(send))
                            }
                            image.processed = true
                            extractFile(zip, FileOutputStream(send))
                            entry = zip.nextEntry
                        }
                    }
                } else {
                    Log.v(TAG, "Retrofit: Upload failed")
                }
            }
        Log.v(TAG, "Retrofit: Returning to ImageProvider")
        //Thread.sleep(10000)
        return image
    }
    private fun extractFile(zipIn: ZipInputStream, fileOutputStream: FileOutputStream) {
        val bytesIn = ByteArray(4096)
        var read: Int
        BufferedOutputStream(fileOutputStream).use { bos ->
            while (zipIn.read(bytesIn).also { read = it } != -1) {
                bos.write(bytesIn, 0, read)
            }
        }
    }
}