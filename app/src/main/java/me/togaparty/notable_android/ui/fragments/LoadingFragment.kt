package me.togaparty.notable_android.ui.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import me.togaparty.notable_android.R

class LoadingFragment: DialogFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return requireActivity().layoutInflater.inflate(R.layout.fragment_loading, container)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.also { window ->
            window.attributes?.also { attributes ->
                attributes.dimAmount = 0.1f
                window.attributes = attributes
            }
        }
    }
    companion object {
        private const val FRAGMENT_TAG = "busy"

        private fun newInstance() = LoadingFragment()

        fun show(supportFragmentManager: FragmentManager): LoadingFragment {
            val dialog = newInstance()
            // prevent dismiss by user click
            dialog.isCancelable = false
            dialog.show(supportFragmentManager, FRAGMENT_TAG)
            return dialog
        }
    }
}