package me.togaparty.notable_android.data.files


import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import me.togaparty.notable_android.BuildConfig
import me.togaparty.notable_android.MainActivity
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class FileWorker(val context: Context){


    fun query(
            collection: Uri =
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,

            projection: Array<String> = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
            ),
            selection: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.RELATIVE_PATH + " like ? "
            } else {
                MediaStore.Images.Media.DATA + " like ? "
            },
            selectionArgs: Array<String>? = arrayOf("%$TAG%")
    ): Cursor? {
        return context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                MediaStore.Images.ImageColumns.DATE_ADDED
        )

    }
    fun loadImages():  ArrayList<GalleryImage> {
        val imageList = arrayListOf<GalleryImage>()

        val outputDirectory = MainActivity.externalAppSpecificStorage(context)
        query()?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val tempImage = GalleryImage(contentUri, name, id)

                val checkDir = File(outputDirectory, File(name).nameWithoutExtension)
                if (checkDir.exists()) {
                    tempImage.processed = true
                    tempImage.addWavFiles(getOtherFiles(checkDir, "wav"))
                    tempImage.addTextFiles(getOtherFiles(checkDir, "txt"))
                    tempImage.addImageFiles(getOtherFiles(checkDir, "images"))
                }
                imageList.add(tempImage)
            }
        }

        return imageList
    }

    @Throws(SecurityException::class)
    fun deleteImage(fileUri: Uri) {
        fileUri.let {
            context.contentResolver
                .delete(it, null, null)
        }
    }

    fun copyImage(filename: String, uri:Uri) {

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/*")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.apply {
                put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + TAG
                )
                put(MediaStore.Images.Media.IS_PENDING, true)
            }

            // RELATIVE_PATH and IS_PENDING are introduced in API 29.
            val imageUri: Uri? = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
            )
            if (imageUri != null) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                context.contentResolver.openOutputStream(imageUri).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(
                        imageUri,
                        values,
                        null, null
                )
            } else {
                throw IOException("Failed to create new MediaStore record.")
            }
        } else {

            val saveDirectory = File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES
                    ), File.separator + TAG
            )

            if (!saveDirectory.exists()) {
                saveDirectory.mkdirs()
            }

            val file = File(saveDirectory, filename)

            context.contentResolver.openInputStream(uri).use {
                input ->
                if (input != null) {
                    file.outputStream().use {
                        output ->
                        input.copyTo(output, 4096)
                    }
                } else{
                    throw IOException("Failed to create output stream.")
                }
            }
            //saveImageOutput(uri, FileOutputStream(file))

            values.put(MediaStore.Images.Media.DATA, file.absolutePath)

            // .DATA is deprecated in API 29
            val imageUri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: throw IOException("Failed to insert new MediaStore record.")
        }

    }
    fun saveImage(
            filename: String,
            uri: Uri
    ) {

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/*")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.apply {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + TAG
                )
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
                        uri,
                        context.contentResolver.openOutputStream(imageUri)
                )
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(
                        imageUri,
                        values,
                        null, null
                )
            }
        } else {

            val saveDirectory = File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES
                    ), File.separator + TAG
            )

            if (!saveDirectory.exists()) {
                saveDirectory.mkdirs()
            }
            val saveFile = System.currentTimeMillis().toString() + ".png"
            val file = File(saveDirectory, saveFile)

            saveImageOutput(uri, FileOutputStream(file))

            values.put(MediaStore.Images.Media.DATA, file.absolutePath)

            // .DATA is deprecated in API 29
            val imageUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
            )

        }
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
    private fun getOtherFiles(directory: File, fileType: String) : Map<String, Uri> {
        val listOfFiles = File(directory, fileType).listFiles()
        val map = linkedMapOf<String, Uri>()
        listOfFiles?.forEach {
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "File read type: $fileType, name: ${it.nameWithoutExtension}")
            }
            map[it.nameWithoutExtension] = Uri.fromFile(it)
        }
        return map
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

