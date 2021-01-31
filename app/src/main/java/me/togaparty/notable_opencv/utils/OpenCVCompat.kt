package me.togaparty.notable_opencv.utils

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc.*
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

fun Mat.toGrayScale(mat: Mat? = null) {
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
        edges: Mat = Mat(), low: Double = 50.0, high: Double = 255.0, aperture: Int = 3,
        l2Gradient: Boolean = true
){
    Canny(this, edges, low, high, aperture, l2Gradient)
}

fun cornerHarris(
        src: Mat,
        dst: Mat,
        blockSize: Int = 7,
        kSize: Int = 3,
        sigma: Double = 0.04
) {
    cornerHarris(src, dst, blockSize,kSize, sigma)
}

fun zeroes(src: Mat): Mat {
    return Mat.zeros(src.rows(), src.cols(), src.type())
}
fun linspace(start: Double, stop: Double, num: Int)
    = Array(num) { start + it * ((stop - start) / (num - 1)) }

fun implement(filename: String){
    var src: Mat = Imgcodecs.imread(filename)
    var gray = Mat()

    src.toGrayScale(gray)
    var edges = Mat()
    src.canny(edges, high = 150.0, aperture = 2)

    var lines = MatOfInt4()
    var slopes = MatOfFloat()


    HoughLinesP(edges, lines, PI/180, 100.0, 50, 10.0)

    if (lines.empty()) {
        return
    }

    for (i in 0..lines.cols()) {
        var vec = lines.get(0, i)

        slopes.push_back(
            when (vec[0] - vec[2] < 0.0000001) {
                true -> Mat(0, 1000000)
                false -> Mat(0, (vec[1] - vec[3]) / (vec[0] - vec[2]).toFloat())
            }
        )
    }

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


