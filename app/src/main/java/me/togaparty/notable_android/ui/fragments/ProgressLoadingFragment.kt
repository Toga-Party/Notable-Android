package me.togaparty.notable_android.ui.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.kofigyan.stateprogressbar.StateProgressBar
import fr.tvbarthel.lib.blurdialogfragment.SupportBlurDialogFragment
import me.togaparty.notable_android.R
import me.togaparty.notable_android.databinding.FragmentProgressLoadingBinding

class ProgressLoadingFragment:  SupportBlurDialogFragment() {


    private lateinit var binding: FragmentProgressLoadingBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding = FragmentProgressLoadingBinding.bind(layoutInflater.inflate( R.layout.fragment_progress_loading,
            container))

        val descriptionData = arrayOf("Processing", "Unpacking", "Done")
        binding.stateProgressBar.setStateDescriptionData(descriptionData)
        binding.textView.text = "Processing your image. This might take a while but please don't leave the screen."
        return binding.root
    }
    fun editStateNumber(number: Int) {
        when(number) {
            2 -> {
                binding.stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO)
                binding.textView.text = "Unpacking the sent file for you to inspect."
            }
            3 ->{
                binding.stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE)
                binding.textView.text = "You can view your files now."
            }

            else -> Unit
        }
    }
    fun finishedAllStates(finished: Boolean) {
        binding.stateProgressBar.setAllStatesCompleted(finished)
        this.dismiss()
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.also { window ->
            window.attributes?.also { attributes ->
                window.attributes = attributes
            }
        }
    }
    override fun getDownScaleFactor(): Float {
        // Allow to customize the down scale factor.
        return 8.0.toFloat()
    }

    override fun getBlurRadius(): Int {
        // Allow to customize the blur radius factor.
        return 8
    }

    override fun isActionBarBlurred(): Boolean {
        // Enable or disable the blur effect on the action bar.
        // Disabled by default.
        return true
    }

    override fun isDimmingEnable(): Boolean {
        // Enable or disable the dimming effect.
        // Disabled by default.
        return true
    }

    override fun isRenderScriptEnable(): Boolean {
        // Enable or disable the use of RenderScript for blurring effect
        // Disabled by default.
        return true
    }

    override fun isDebugEnable(): Boolean {
        // Enable or disable debug mode.
        // False by default.
        return true
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