package me.togaparty.notable_android.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.ramotion.paperonboarding.PaperOnboardingEngine
import com.ramotion.paperonboarding.PaperOnboardingFragment
import com.ramotion.paperonboarding.PaperOnboardingPage
import com.ramotion.paperonboarding.listeners.PaperOnboardingOnChangeListener
import com.ramotion.paperonboarding.listeners.PaperOnboardingOnLeftOutListener
import com.ramotion.paperonboarding.listeners.PaperOnboardingOnRightOutListener
import me.togaparty.notable_android.MainActivity
import me.togaparty.notable_android.OnBoardingActivity
import me.togaparty.notable_android.R

private const val ELEMENTS_PARAM = "elements"
class CustomOnBoardingFragment : PaperOnboardingFragment() {

    private var mOnChangeListener: PaperOnboardingOnChangeListener? = null
    private var mOnRightOutListener: PaperOnboardingOnRightOutListener? = null
    private var mOnLeftOutListener: PaperOnboardingOnLeftOutListener? = null

    private var mElements: ArrayList<PaperOnboardingPage>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mElements = requireArguments()[ELEMENTS_PARAM] as ArrayList<PaperOnboardingPage>?
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_custom_on_boarding, container, false)

        // create engine for onboarding element
        val paperOnboardingEngine = PaperOnboardingEngine(
            view.findViewById(R.id.onboardingRootView),
            mElements,
            requireContext()
        )
        val textView: TextView = view.findViewById(R.id.skip)
        textView.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().apply {
                putBoolean(OnBoardingActivity.COMPLETED_ONBOARDING_PREF_NAME, true)
                apply()
            }
            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        // set listeners
        paperOnboardingEngine.setOnChangeListener(mOnChangeListener)
        paperOnboardingEngine.setOnLeftOutListener(mOnLeftOutListener)
        paperOnboardingEngine.setOnRightOutListener(mOnRightOutListener)

        return view
    }
    companion object {
        fun newInstance(elements: ArrayList<PaperOnboardingPage>): CustomOnBoardingFragment {
            val fragment = CustomOnBoardingFragment()
            val args = Bundle()
            args.putSerializable(ELEMENTS_PARAM, elements)
            fragment.arguments = args
            return fragment
        }
    }
}