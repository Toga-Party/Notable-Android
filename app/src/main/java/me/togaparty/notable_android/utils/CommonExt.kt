package me.togaparty.notable_android.utils

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

fun <T : ViewBinding> viewBindingWithBinder(
    binder: (View) -> T
) = FragmentAutoClearedValueBinding(binder)

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

fun Fragment.showDialog(
    title: String,
    body: String,
    callback: () -> Unit
) {
    AlertDialog.Builder(requireContext()).also {
        it.setTitle(title)
        it.setMessage(body)
        it.setPositiveButton("Yes") { _, _ ->callback()}
        it.setNegativeButton("No") {_,_ ->}
    }.create().show()

}
fun Fragment.showFailedDialog(
    title: String,
    body: String)
{
    AlertDialog.Builder(requireContext()).also{
        it.setTitle(title)
        it.setMessage(body)
        it.setPositiveButton("Ok"){_,_->}
    }.create().show()
}
fun Fragment.showSuccessDialog(
    title: String,
    body: String,
    callback: () -> Unit
) {
    AlertDialog.Builder(requireContext()).also {
        it.setTitle(title)
        it.setMessage(body)
        it.setPositiveButton("Yes") { _, _ ->callback()}
        it.setNegativeButton("No") {_,_ ->}
    }.create().show()
}
