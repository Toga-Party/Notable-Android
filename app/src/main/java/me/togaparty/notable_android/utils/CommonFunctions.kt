package me.togaparty.notable_android.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

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

