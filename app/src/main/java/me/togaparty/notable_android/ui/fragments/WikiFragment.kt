package me.togaparty.notable_android.ui.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.files.JsonParser


class WikiFragment : Fragment() {
    private var term : String? = null
    private var note : String? = null
    private var duration: String? = null

    private var title: String? = null
    private var definition: String? = null
    private val jsonParser by lazy {JsonParser.getInstance(requireContext())}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            term = requireArguments().getString("term")
            note = requireArguments().getString("note")
            duration = requireArguments().getString("duration")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val pair = term?.let {
            jsonParser.getNoteAndDefinition(it)
        }?: Pair("","")

        title = pair.first
        definition = pair.second
        return inflater.inflate(R.layout.fragment_glossary_definition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val definitionTextView: TextView = view.findViewById(R.id.definition_textview)
        view.findViewById<TextView>(R.id.term_textview).text = title
        definitionTextView.text = definition
        definitionTextView.movementMethod = ScrollingMovementMethod()
    }

    companion object
}
