package me.togaparty.notable_android.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import me.togaparty.notable_android.BuildConfig
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.files.JsonParser
import me.togaparty.notable_android.databinding.FragmentWikiBinding
import me.togaparty.notable_android.utils.Constants
import me.togaparty.notable_android.utils.viewBindingWithBinder


class WikiFragment : Fragment(R.layout.fragment_wiki) {
    private var term: String? = null
    private var note: String? = null
    private var duration: String? = null

    private var title: String? = null
    private var definition: String? = null
    private var body: String? = null

    private lateinit var coloredFoot: SpannableStringBuilder
    private val jsonParser by lazy { JsonParser.getInstance(requireContext()) }
    private val binding by viewBindingWithBinder(FragmentWikiBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            term = requireArguments().getString("first")
            note = requireArguments().getString("second")
            duration = requireArguments().getString("third")
        }
    }

    private fun setFormattedText(list: List<Any>){
        coloredFoot = SpannableStringBuilder()
        title = list[0] as String
        definition = list[1] as String
        body = if (note.isNullOrBlank() && duration.isNullOrBlank()) {
                    ""
                } else if (duration.isNullOrBlank()) {
                    "Attribute: $note"
                } else if (note.isNullOrBlank()) {
                    "Attribute: $duration "
                } else {
                    "$note : $duration"
                }

        if (!body.isNullOrBlank()) {
            val part1 = "Additional Information:\n"
            var part2 = "• " + list[3] as String + "\n"
            if (duration in durationRef) {
                val dur = durationRef[duration]
                part2 += "• $duration has a duration of ${dur}s\n"
                coloredText(Color.BLACK, part1)
                coloredText(Color.DKGRAY, part2)
            } else {
                coloredText(Color.BLACK, part1)
                coloredText(Color.DKGRAY, part2)
            }
        }
    }
    private fun coloredText(color: Int, string: String) {
        val str = SpannableString(string)
        str.setSpan(ForegroundColorSpan(color), 0, str.length, 0)
        coloredFoot.append(str)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = term?.let {

            when (it) {
                "keySignature", "timeSignature", "rest" ->
                    jsonParser.getNoteNameDuration(it, duration?: "")
                "barline", "tie", "multirest" ->
                    jsonParser.getNameAndDefinition(it)
                else ->
                    jsonParser.getNoteNameDuration(it, note ?: "")
            }
        } ?: listOf("", "", "", "")

        setFormattedText(list)

        if(BuildConfig.DEBUG) {
            Log.d(Constants.TAG, "$term $note $duration")
        }
        binding.termTextview.text = title

        binding.definitionTextview.text = definition
        binding.definitionTextview.movementMethod = ScrollingMovementMethod()

        binding.notesTextview.text = body
        binding.notesTextview.movementMethod = ScrollingMovementMethod()

        binding.durationTextview.setText(coloredFoot, TextView.BufferType.SPANNABLE)
        binding.durationTextview.movementMethod = ScrollingMovementMethod()
    }

    companion object {
        val durationRef: Map<String, Double> by lazy {
            mapOf(
                "double" to 4.0,
                "double." to 6.0,
                "double.." to 7.0,
                "whole" to 2.0,
                "whole." to 3.0,
                "whole.." to 3.5,
                "half" to 1.0,
                "half." to 1.5,
                "half.." to 1.75,
                "quarter" to .50,
                "quarter." to .75,
                "quarter.." to .875,
                "eighth" to .25,
                "eighth." to .375,
                "eighth.." to .437,
                "sixteenth" to .06,
                "sixteenth." to .09,
                "sixteenth.." to .105,
                "thirty_second" to .03,
                "thirty_second." to .045,
                "thirty_second.." to .052,
                "sixty_fourth" to .02,
                "sixty_fourth." to .030,
                "sixty_fourth.." to .035,
                "hundred_twenty_eighth" to .01,
                "hundred_twenty_eighth." to .015,
                "hundred_twenty_eighth.." to .017
            )
        }
    }
}
