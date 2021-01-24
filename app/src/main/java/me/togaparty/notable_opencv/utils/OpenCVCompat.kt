package me.togaparty.notable_opencv.utils


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*
import java.util.*
import kotlin.collections.ArrayList


fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    this.close()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}



fun Mat.threshold(
    bitmap: Bitmap, min: Double = 50.00, max: Double = 255.00,
    type: Int = THRESH_OTSU, action: (Bitmap) -> Unit
) {
    this.toGrayScale(bitmap)
    threshold(this, this, min, max, type)
    return action(this.toBitmap())
}


fun Mat.findContours(
    contours: MutableList<MatOfPoint>,
    hierarchy: Mat = Mat(),
    retrieval: Int = Imgproc.RETR_EXTERNAL,
    approx: Int = Imgproc.CHAIN_APPROX_SIMPLE
) {
    findContours(this, contours, hierarchy, retrieval, approx)
}

fun Mat.blur() {
    blur(this, this, Size(3.0, 3.0))
}
fun Mat.erode() {
    erode(this, this, Mat())
}
fun Mat.canny(
    bitmap: Bitmap, min: Double = 50.0, max: Double = 255.0, aperture: Int = 3,
    l2Gradient: Boolean = true
){
    this.toGrayScale(bitmap)
    Canny(this, this, min, max, aperture, l2Gradient)
}
fun zeroes(src: Mat): Mat {
    return Mat.zeros(src.rows(), src.cols(), src.type())
}

fun Mat.drawRectangles(contours: MutableList<MatOfPoint>, src: Mat) {
    var rect: Rect
    var big: MutableList<Point> = ArrayList()
    for (i in 0 until contours.size) {
        rect = boundingRect(contours.get(i))
        if (contourArea(contours[i]) > src.cols() * src.rows() / 8) {

            rectangle(src, rect, Scalar(255.0, 0.0, 0.0), 2)
        } else {
            big.add(Point(rect.x.toDouble(), rect.y.toDouble()))
            big.add(Point(
                (rect.x + rect.width).toDouble(),
                (rect.y + rect.height).toDouble())
            )
        }
    }
    var source = MatOfPoint2f()
    source.fromList(big)
    rect = boundingRect(source)
    rectangle(src, rect, Scalar(0.0, 255.0, 0.0), 2)

}
fun Mat.prepareRectangles(contours: MutableList<MatOfPoint>) {
    for (i in 0 until contours.size) {
        val rect: Rect = boundingRect(contours.get(i))
        if (rect.width > 10 || rect.height > 10) {
            rectangle(this, rect, Scalar(255.00), -1)
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