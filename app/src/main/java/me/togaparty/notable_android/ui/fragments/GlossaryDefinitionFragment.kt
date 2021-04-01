package me.togaparty.notable_android.ui.fragments

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.fragment.app.Fragment
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import me.togaparty.notable_android.R
import me.togaparty.notable_android.databinding.FragmentGlossaryDefinitionBinding


class GlossaryDefinitionFragment : Fragment(R.layout.fragment_glossary_definition) {
    private val binding by viewBinding(FragmentGlossaryDefinitionBinding::bind)
    private var term : String? = null
    private var definition : String? = null
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
