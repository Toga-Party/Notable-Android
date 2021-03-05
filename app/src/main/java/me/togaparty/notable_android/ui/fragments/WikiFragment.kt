package me.togaparty.notable_android.ui.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.files.JsonParser
import me.togaparty.notable_android.utils.Constants


class WikiFragment : Fragment() {
    private var term : String? = null
    private var note : String? = null
    private var duration: String? = null

    private var title: String? = null
    private var definition: String? = null

    private var body: String? = null
    private var foot: String? = null
    private val jsonParser by lazy {JsonParser.getInstance(requireContext())}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            term = requireArguments().getString("first")
            note = requireArguments().getString("second")
            duration = requireArguments().getString("third")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val list = term?.let {
            when(it) {
                "keySignature", "timeSignature", "rest", "multirest" -> jsonParser.getNoteNameDuration(it, duration?:"")
                "barline", "tie" -> jsonParser.getNameAndDefinition(it)
                else -> jsonParser.getNoteNameDuration(it, note?:"")
            }
        }?: listOf("","","","")

        title = list[0] as String
        definition = list[1] as String
        //body = list[2] as String
        foot = list[3] as String
        body = " $note - $duration "
        foot += "\n $duration"
        Log.d(Constants.TAG, "$term $note $duration")
        return inflater.inflate(R.layout.fragment_wiki, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.term_textview).text = title

        val definitionTextView: TextView = view.findViewById(R.id.definition_textview)
        definitionTextView.text = definition
        definitionTextView.movementMethod = ScrollingMovementMethod()

        val notesTextView: TextView = view.findViewById(R.id.notes_textview)
        notesTextView.text = body
        notesTextView.movementMethod = ScrollingMovementMethod()

        val durationTextView: TextView = view.findViewById(R.id.duration_textview)
        durationTextView.text = foot
        durationTextView.movementMethod = ScrollingMovementMethod()
    }

    companion object
}
