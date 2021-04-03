package me.togaparty.notable_android.data.network

import android.content.Context
import android.net.Uri
import android.util.Log
import me.togaparty.notable_android.BuildConfig
import me.togaparty.notable_android.MainActivity
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.BufferedOutputStream
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream


class RetrofitWorker(val context: Context) {

    @Throws(Exception::class)
    fun uploadFile(currentImage: GalleryImage): GalleryImage {

        val image = GalleryImage(
            imageUrl = currentImage.imageUrl,
            name = currentImage.name,
        )

        val filename = image.name.toRequestBody("text/plain".toMediaTypeOrNull())

        var imageToSend: MultipartBody.Part?
        context.contentResolver.openInputStream(image.imageUrl).use { input ->
            val imageType = context.contentResolver.getType(image.imageUrl)
            imageToSend = input?.let {
                MultipartBody.Part.createFormData(
                    "file",
                    image.name,
                    it.readBytes().toRequestBody(imageType!!.toMediaTypeOrNull())
                )
            }
        }

        Log.d(TAG, "Retrofit: Uploading the image")
        val retrofit: RetrofitService = RetrofitBuilder.retrofitInstance

        val response: Response<ResponseBody>? = try {
            imageToSend?.let { sendImage ->
                Log.v(TAG, "Retrofit: Waiting for response")
                retrofit
                    .upload(sendImage, filename)
                    .execute()
            }
        } catch (exec: IOException) {
            exec.printStackTrace()
            throw IOException("Failed to upload the file to the server.")
        } catch (exec: RuntimeException) {
            exec.printStackTrace()
            throw RuntimeException("Failed to decode the response from the server.")
        }

        if (response != null) {
            if (response.isSuccessful) {
                Log.v(TAG, "Retrofit: Success response received ${response.body()!!.contentLength()}")

                if(response.body()!!.contentLength() <= 100) {
                    throw  RuntimeException("Processing Failed: ${response.body()?.string()} " +
                            "Please contact support for more inquiries.")
                }
                ZipInputStream(response.body()?.byteStream()).use { zip ->
                    var entry = zip.nextEntry

                    while (entry != null) {
                        val outputDirectory =
                                MainActivity.externalAppSpecificStorage(context)

                        val temp = when (File(entry.name).extension) {
                            "png", "jpg", "jpeg" ->
                                File(
                                    outputDirectory,
                                    File(image.name).nameWithoutExtension
                                            + separator + "images"
                                )
                                    .apply { mkdirs() }
                            else ->
                                File(
                                    outputDirectory,
                                    File(image.name).nameWithoutExtension +
                                            separator +
                                            File(entry.name).extension
                                ).apply { mkdirs() }
                        }

                        val send = File(temp, entry.name)
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "Retrofit: Extracting zip")
                        }
                        try {
                            extractFile(zip, FileOutputStream(send))
                        } catch (exec: IOException) {
                            exec.printStackTrace()
                            throw IOException("Failed to extract the response received from the server.")
                        }

                        when (send.extension) {
                            "wav" -> image.addWavFiles(
                                mapOf(Pair(
                                    send.nameWithoutExtension,
                                    Uri.fromFile(send)
                                ))
                            )

                            "png", "jpg", "jpeg" -> image.addImageFiles(
                                mapOf(
                                    Pair(
                                        send.nameWithoutExtension,
                                        Uri.fromFile(send)
                                    )
                                )
                            )

                            "txt" -> image.addTextFiles(
                                mapOf(
                                    Pair(
                                        send.nameWithoutExtension,
                                        Uri.fromFile(send)
                                    )
                                )
                            )
                        }
                        entry = zip.nextEntry
                    }
                }
            } else {
                throw IOException("Upload failed. Server might be down.")
            }
        }
        return image.apply { processed = true }
    }
    @Throws(IOException::class)
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