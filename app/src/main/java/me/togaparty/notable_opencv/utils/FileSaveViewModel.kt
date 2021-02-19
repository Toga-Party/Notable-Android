package me.togaparty.notable_opencv.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class FileSaveViewModel: ViewModel() {

    suspend fun saveImage(
        context: Context,
        directory: String,
        fileName: String,
        fileUri: Uri
    ){
        val bitmap = getBitmap(File(fileUri.path!!))
        return withContext(Dispatchers.IO){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = contentValues(fileName).apply{
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + directory)
                    put(MediaStore.Images.Media.IS_PENDING, true)
                }
                // RELATIVE_PATH and IS_PENDING are introduced in API 29.

                val imageUri: Uri? =
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )

                if (imageUri != null) {
                    saveImageOutput(
                        bitmap,
                        context.contentResolver.openOutputStream(imageUri)
                    )
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(
                        imageUri,
                        values,
                        null, null)
                }
            } else {

                val saveDirectory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                            ,File.separator + directory)

                if (!saveDirectory.exists()) {
                    saveDirectory.mkdirs()
                }
                val saveFile = System.currentTimeMillis().toString() + ".png"
                val file = File(saveDirectory, saveFile)

                saveImageOutput(bitmap, FileOutputStream(file))

                val values = contentValues(fileName)
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)

                // .DATA is deprecated in API 29
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            }
        }

    }

    private fun contentValues(fileName: String) : ContentValues =
        ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/*")

        }
    private fun saveImageOutput(bitmap: Bitmap, outputStream: OutputStream?)  =
        outputStream?.let {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

}