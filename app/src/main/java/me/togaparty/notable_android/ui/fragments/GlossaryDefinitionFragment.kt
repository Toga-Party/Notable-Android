package me.togaparty.notable_android.ui.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.fragment.app.Fragment
import me.togaparty.notable_android.R
import me.togaparty.notable_android.databinding.FragmentGlossaryDefinitionBinding
import me.togaparty.notable_android.utils.viewBindingWithBinder


class GlossaryDefinitionFragment : Fragment(R.layout.fragment_glossary_definition) {
    private var term : String? = null
    private var definition : String? = null
    private val binding by viewBindingWithBinder(FragmentGlossaryDefinitionBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            term = requireArguments().getString("term")
            definition = requireArguments().getString("definition")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.termTextview.text = term
        binding.definitionTextview.text = definition
        binding.definitionTextview.movementMethod = ScrollingMovementMethod()
    }

    companion object
}
