package me.togaparty.notable_android.data.files

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.togaparty.notable_android.data.GalleryImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class FileWorker (private val coroutineScope: CoroutineScope, val context: Context){

    @SuppressLint("Recycle")
    fun loadImages():  ArrayList<GalleryImage> {
        val imageList = ArrayList<GalleryImage>()
        coroutineScope.launch(context = Dispatchers.IO) {
            query()?.use {  cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol)
                    val contentUri: Uri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                    )
                    imageList.add(GalleryImage(contentUri, name, false))
                }

            }
        }
        return imageList
    }

    fun deleteImage(fileUri: Uri) {
        Log.d("FileWorker", "$fileUri")
        fileUri.let {
            context.contentResolver
                .delete(it, null, null)
        }
    }

    fun saveImage(
            directory: String,
            filename: String,
            uri: Uri
    ): GalleryImage? {

        var image: GalleryImage? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = contentValues(filename).apply{
                    put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + File.separator + directory)
                    put(MediaStore.Images.Media.IS_PENDING, true)
                }
                // RELATIVE_PATH and IS_PENDING are introduced in API 29.

                val imageUri: Uri? =
                    context.contentResolver.insert(
                            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                            values
                    )

                if (imageUri != null) {
                    saveImageOutput(
                            uri,
                            context.contentResolver.openOutputStream(imageUri)
                    )
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(
                            imageUri,
                            values,
                            null, null)
                    image = GalleryImage(imageUri, filename, false)
                }
            } else {

                val saveDirectory = File(
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES
                        ),File.separator + directory)

                if (!saveDirectory.exists()) {
                    saveDirectory.mkdirs()
                }
                val saveFile = System.currentTimeMillis().toString() + ".png"
                val file = File(saveDirectory, saveFile)

                saveImageOutput(uri, FileOutputStream(file))

                val values = contentValues(filename)
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)

                // .DATA is deprecated in API 29
                val imageUri = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                )
                image = imageUri?.let { GalleryImage(it, filename, false) }
            }
        return image
    }

    private fun query(): Cursor? {
        val collection = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
        )
        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.RELATIVE_PATH + " like ? "
        } else {
            MediaStore.Images.Media.DATA + " like ? "
        }
        val selectionArgs = arrayOf("%Notable%")
        return context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                MediaStore.Images.ImageColumns.DATE_ADDED
        )

    }
    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true)
    }

    private fun contentValues(fileName: String) : ContentValues =
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/*")

            }
    private fun saveImageOutput(fileUri: Uri, outputStream: OutputStream?) {

        var bitmap = BitmapFactory.decodeFile(fileUri.path!!)
        var exif: ExifInterface? = null
        try {
            val pictureFile = File(fileUri.path!!)
            exif = ExifInterface(pictureFile.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var orientation = ExifInterface.ORIENTATION_NORMAL

        if (exif != null) {
            orientation =
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }

        bitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
            else -> bitmap
        }
        outputStream?.let {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}

