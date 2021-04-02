package me.togaparty.notable_android.ui.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.kofigyan.stateprogressbar.StateProgressBar
import me.togaparty.notable_android.R

class ProgressLoadingFragment: DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_progress_loading, container)
        val descriptionData = arrayOf("Processing", "Received Data", "Unpacking", "Done")
        val progressBar: StateProgressBar = view.findViewById(R.id.state_progress_bar)
        progressBar.setStateDescriptionData(descriptionData)

        return view
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

        private fun newInstance() = ProgressLoadingFragment()

        fun show(supportFragmentManager: FragmentManager): ProgressLoadingFragment {
            val dialog = newInstance()
            // prevent dismiss by user click
            dialog.isCancelable = false
            dialog.show(supportFragmentManager, FRAGMENT_TAG)
            return dialog
        }
    }
}