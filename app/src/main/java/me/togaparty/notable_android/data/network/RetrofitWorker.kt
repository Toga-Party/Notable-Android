package me.togaparty.notable_android.data.network

import android.content.Context
import me.togaparty.notable_android.data.GalleryImage
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream


class RetrofitWorker(val context: Context) {

    fun uploadFile(currentImage: GalleryImage) : GalleryImage {
        //    val serviceWorker = RetrofitBuilder.getRetrofit()
//    val filename = image.name.toRequestBody("text/plain".toMediaTypeOrNull())
//
//    var imageToSend: MultipartBody.Part?
//    context.contentResolver.openInputStream(image.imageUrl).use { input ->
//        val imageType = context.contentResolver.getType(image.imageUrl)
//
//        //Log.d("Retrofit", "Sending image type: $imageType")
//         imageToSend = input?.let {
//
//            MultipartBody.Part.createFormData(
//                    "file",
//                    image.name,
//                    it.readBytes().toRequestBody(imageType!!.toMediaTypeOrNull()))
//
//        }
//    }
//    imageToSend?.let { multiBody_Part ->
//        serviceWorker.create(RetrofitService::class.java).upload(multiBody_Part, filename)
//            .enqueue(object : Callback<ResponseBody> {
//                override fun onResponse(
//                        call: Call<ResponseBody>,
//                        response: Response<ResponseBody>
//                ) {
//
//                    Log.d("Response", response.code().toString())
//
//
//                    if (response.isSuccessful) {
//                        Log.v("Upload", "success response received")
//
//                        ZipInputStream(response.body()?.byteStream()).use { zip ->
//                            var entry = zip.nextEntry
//                            while (entry != null) {
//                                val outputDirectory = MainActivity.externalAppSpecificStorage(context)
//                                    Log.d("Response", "File name: $entry")
//                                    Log.d("Response", "File size: ${entry.size}")
//                                    Log.d("Response", "Is directory: ${entry.isDirectory}")
//
//                                val tempfile =
//                                    File(outputDirectory,
//                                            File(image.name).nameWithoutExtension +
//                                                    separator +
//                                                    File(entry.name).extension
//                                    ).apply{mkdirs()}
//
//                                val send = File(tempfile,entry.name)
//                                when (send.extension) {
//                                    "wav" -> image.addWAVFile(send.nameWithoutExtension,Uri.fromFile(send))
//                                    "png" -> image.addPNGFile(send.nameWithoutExtension,Uri.fromFile(send))
//                                    "txt" -> image.addTextFile(send.nameWithoutExtension,Uri.fromFile(send))
//                                }
//                                extractFile(zip, FileOutputStream(send))
//
//                                entry = zip.nextEntry
//                            }
//
//                        }
//                        context.toast("Upload successful")
//                    } else {
//                        Log.v("Upload", "Upload failed")
//                        context.toast("Upload failed")
//                    }
//                }
//
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    Log.e("Upload", "Something went wrong: ${t.printStackTrace()}", t)
//                    context.toast("Failed to get response from the server")
//                }
//            })
//        } ?: context.toast("Upload failed")
        return GalleryImage(
                processed = true,
                imageUrl = currentImage.imageUrl,
                name = currentImage.name,
        )
    }

    internal fun extractFile(zipIn: ZipInputStream, fileOutputStream: FileOutputStream) {
        val bytesIn = ByteArray(4096)
        var read: Int
        BufferedOutputStream(fileOutputStream).use { bos ->
            while (zipIn.read(bytesIn).also { read = it } != -1) {
                bos.write(bytesIn, 0, read)
            }
        }
    }
}