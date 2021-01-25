package me.togaparty.notable_opencv.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc.*
import java.util.ArrayList
import kotlin.math.min
import kotlin.math.max

/*
fun Mat.threshold(
        bitmap: Bitmap, thresh: Double = 100.0, max: Double = 255.0,
        type: Int = THRESH_OTSU, action: (Bitmap) -> Unit
) {
    this.toGrayScale(bitmap)
    threshold(this, this, thresh, max, type)
    return action(this.toBitmap())
}
*/


fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    this.close()
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

fun Mat.toGrayScale(bitmap: Bitmap, mat: Mat? = null) {
    Utils.bitmapToMat(bitmap, this)
    mat?.let{
        cvtColor(this, mat, COLOR_RGB2GRAY)
    }?: run {
        cvtColor(this, this, COLOR_RGB2GRAY)
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

fun Mat.findContours(
        contours: MutableList<MatOfPoint>,
        hierarchy: Mat = Mat(),
        retrieval: Int = RETR_EXTERNAL,
        approx: Int = CHAIN_APPROX_SIMPLE
) : MutableList<MatOfPoint> {
    findContours(this, contours, hierarchy, retrieval, approx, Point(0.0, 0.0))
    return contours
}

fun Mat.blur() {
    blur(this, this, Size(3.0, 3.0))
}
fun Mat.erode() {
    erode(this, this, Mat())
}
fun Mat.canny(
        bitmap: Bitmap, thresh: Double = 50.0, max: Double = 255.0, aperture: Int = 3,
        l2Gradient: Boolean = true
){
    this.toGrayScale(bitmap)
    Canny(this, this, thresh, max, aperture, l2Gradient)
}
fun Mat.prepareContours(contours: MutableList<MatOfPoint>) {
    for (i in 0 until contours.size) {
        val rect: Rect = boundingRect(contours[i])
        if (rect.width > 10 || rect.height > 10) {
            rectangle(this, rect, Scalar(255.00), -1)
        }
    }
}
fun Mat.drawContours(contours: MutableList<MatOfPoint>, gray: Mat) {
    var big = Rect()
    for (i in 0 until contours.size) {
        if (contourArea(contours[i]) > gray.cols() * gray.rows() / 8) {
            val rect = boundingRect(contours[i])
            rectangle(gray, rect, Scalar(255.0, 0.0, 0.0), 2)
        } else {
            if (big.height < 1) {
                big = boundingRect(contours[i])
            }
            big = union(big, boundingRect(contours[i]))
        }
    }
    rectangle(this, big, Scalar (0.0, 255.0, 0.0), 2)
}

fun zeroes(src: Mat): Mat {
    return Mat.zeros(src.rows(), src.cols(), src.type())
}

fun implement(src: Mat, bitmap: Bitmap) : Mat {
    var gray =  Mat()
    src.toGrayScale(bitmap, gray)

    gray.blur()
    gray.erode()
    gray.canny(bitmap)
    val contours =
            gray.findContours(ArrayList<MatOfPoint>())

    gray = zeroes(gray)
    gray.prepareContours(contours)
    gray.erode()

    gray.findContours(contours)
    src.drawContours(contours, gray)
    return src
}
fun union(a: Rect, b: Rect) : Rect {
    if (a.empty()) {
        a.x = b.x
        a.y = b.y
        a.height = b.height
        a.width = b.width
    } else if (!b.empty()) {

        val x = min(a.x, b.x)
        val y = min(a.y, b.y)
        a.width =  max(a.x + a.width, b.x + b.width) - x
        a.height = max(a.y + a.height, b.y + b.height) - y
        a.x = x
        a.y = y

    }
    return a
}


