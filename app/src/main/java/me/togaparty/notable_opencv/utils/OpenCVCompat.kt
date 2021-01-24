package me.togaparty.notable_opencv.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.boundingRect
import org.opencv.imgproc.Imgproc.rectangle


fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    this.close()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}



fun Mat.threshold(bitmap: Bitmap, min: Double = 50.00, max: Double = 255.00,
                  type: Int = Imgproc.THRESH_OTSU, action: (Bitmap) -> Unit) {
    this.toGrayScale(bitmap)
    Imgproc.threshold(this, this, min, max, type)
    return action(this.toBitmap())
}


fun Mat.findContours(contours: MutableList<MatOfPoint>, hierarchy: Mat, retrieval: Int = Imgproc.RETR_EXTERNAL,
                     approx: Int = Imgproc.CHAIN_APPROX_SIMPLE) {
    Imgproc.findContours(this, contours, hierarchy, retrieval, approx)
}

fun Mat.blur() {
    Imgproc.blur(this, this, Size(3.0, 3.0))
}
fun Mat.erode() {
    Imgproc.erode(this, this, Mat())
}
fun Mat.canny(bitmap: Bitmap, min: Double = 50.00, max: Double = 255.00, aperture: Int = 3,
              l2Gradient: Boolean = true, action: (Bitmap) -> Unit){
    this.toGrayScale(bitmap)
    Imgproc.Canny(this, this, min, max, aperture, l2Gradient)
    return action(this.toBitmap())
}

fun Mat.drawRectangles(contours: MutableList<MatOfPoint>) {
    for (i in 0 until contours.size) {
        val rect: Rect = boundingRect(contours.get(i))
        if (rect.width > 10 || rect.height > 10) {
            rectangle(this , rect, Scalar(255.00), -1)
        }
    }
}

fun Mat.toGrayScale(bitmap: Bitmap, mat: Mat? = null) {
    Utils.bitmapToMat(bitmap, this)
    mat?.let{
        Imgproc.cvtColor(this, mat, Imgproc.COLOR_RGB2GRAY)
    }?: run {
        Imgproc.cvtColor(this, this, Imgproc.COLOR_RGB2GRAY)
    }
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