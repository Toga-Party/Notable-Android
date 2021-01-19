package me.togaparty.notable_opencv.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    this.close()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}


fun Mat.otsuThreshold(bitmap: Bitmap, thresh: Double = 50.00, max: Double = 255.00,
                  type:Int = Imgproc.THRESH_OTSU, action: (Bitmap) -> Unit) {
    this.toGrayScale(bitmap)
    Imgproc.threshold(this, this, thresh, max, type)
    return action(this.toBitmap())
}

fun Mat.toGrayScale(bitmap: Bitmap) {
    Utils.bitmapToMat(bitmap, this)
    Imgproc.cvtColor(this, this, Imgproc.COLOR_RGB2GRAY)
}

fun Bitmap.toMat() : Mat {
    val mat = Mat()
    Utils.bitmapToMat(this, mat)
    return mat
}

fun Mat.toBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
    val bitmap = Bitmap.createBitmap(this.cols(), this.rows(), config)
    Utils.matToBitmap(this, bitmap)
    return bitmap
}