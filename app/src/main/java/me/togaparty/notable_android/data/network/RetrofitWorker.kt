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
import java.io.IOException
import java.net.ConnectException
import java.util.zip.ZipInputStream


class RetrofitWorker(val context: Context) {

    fun uploadFile(currentImage: GalleryImage): UploadResult = try {

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
        val response = imageToSend?.let { sendImage ->
            Log.v(TAG, "Retrofit: Waiting for response")
            retrofit
                .upload(sendImage, filename)
                .execute()
        }

        if (response != null) {
            if (response.isSuccessful) {
                Log.v(TAG, "Retrofit: Success response received")
                ZipInputStream(response.body()?.byteStream()).use { zip ->
                    var entry = zip.nextEntry

                    while (entry != null) {
                        val outputDirectory =
                                MainActivity.externalAppSpecificStorage(context)

                        val temp = when (File(entry.name).extension) {
                            "png", "jpg" ->
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

                        image.processed = true
                        when (send.extension) {
                            "wav" -> image.addWAVFile(
                                    send.nameWithoutExtension,
                                    Uri.fromFile(send))

                            "png" -> image.addImageFile(
                                    send.nameWithoutExtension,
                                    Uri.fromFile(send))

                            "txt" -> image.addTextFile(
                                    send.nameWithoutExtension,
                                    Uri.fromFile(send))
                        }

                        Log.d(TAG, "Retrofit: Extracting zip")
                        extractFile(zip, FileOutputStream(send))

                        entry = zip.nextEntry
                    }
                }
            } else {
                throw IOException("Response is empty. Upload failed.")
            }
        }
        UploadResult.Success(retrieved = image)
    } catch (exe: IOException) {
        UploadResult.Error("Failed to process the response", IOException())
    } catch (exe: ConnectException) {
        UploadResult.Error("Failed to connect to server", ConnectException())
    } catch (exe: Exception) {
        UploadResult.Error("Failed to upload the image", Exception())
    }
    @Throws(Exception::class)
    private fun extractFile(zipIn: ZipInputStream, fileOutputStream: FileOutputStream) {
        val bytesIn = ByteArray(4096)
        var read: Int
        BufferedOutputStream(fileOutputStream).use { bos ->
            while (zipIn.read(bytesIn).also { read = it } != -1) {
                bos.write(bytesIn, 0, read)
            }
        }
    }
    sealed class UploadResult {
        data class Success(val retrieved: GalleryImage) : UploadResult()
        data class Error(val message: String, val cause: Exception? = null) : UploadResult()
    }
}