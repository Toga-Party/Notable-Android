package me.togaparty.notable_opencv.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment

fun Context.toast(text: String?) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}
fun Fragment.toast(text: String?) {
    Toast.makeText(this.requireContext(), text, Toast.LENGTH_LONG).show()
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