package me.togaparty.notable_opencv.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File

fun permissionsGranted(context: Context, permissions: List<String>) = permissions.all {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}
val ALL_REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE).also {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        it.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
    } else {
        it.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}

val FILE_REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.READ_EXTERNAL_STORAGE).also {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            it.add(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }  else {
            it.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
}

fun Fragment.toast(text: String?) {
    Toast.makeText(this.requireContext(), text, Toast.LENGTH_LONG).show()
}
fun Context.toast(text: String?) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}
fun Context.showDeniedDialog(title: String,
                             body: String) {
    AlertDialog.Builder(this).also{
        it.setTitle(title)
        it.setMessage(body)
    }.create().show()
}

fun Context.showPermissionRequestDialog(
    title: String,
    body: String,
    callback: () -> Unit
) {
    AlertDialog.Builder(this).also {
        it.setTitle(title)
        it.setMessage(body)
        it.setPositiveButton("Retry") { _, _ ->
            callback()
        }
        it.setNegativeButton("No") {_,_ ->
            this.showDeniedDialog(
            "Access denied",
            "You can accept the permissions needed in the Setting page")
        }
    }.create().show()

}

fun Fragment.showDialog(
        title: String,
        body: String,
        callback: () -> Unit
) {
    AlertDialog.Builder(requireContext()).also {
        it.setTitle(title)
        it.setMessage(body)
        it.setPositiveButton("Yes") { _, _ ->
            callback()
        }
        it.setNegativeButton("No") {_,_ ->

        }
    }.create().show()

}