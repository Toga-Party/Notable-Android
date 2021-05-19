package me.togaparty.notable_android.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.files.JsonParser
import me.togaparty.notable_android.databinding.FragmentGlossaryBinding
import me.togaparty.notable_android.ui.adapter.CategoryItemAdapter
import me.togaparty.notable_android.ui.adapter.MainRecyclerAdapter
import me.togaparty.notable_android.ui.items.AllCategory
import me.togaparty.notable_android.ui.items.CategoryItem
import org.json.JSONException


class GlossaryFragment :
    Fragment(R.layout.fragment_glossary),
    CategoryItemAdapter.OnItemClickListener {

    private val binding by viewBinding(FragmentGlossaryBinding::bind)

    internal var mainRecyclerAdapter: MainRecyclerAdapter? = null
    private lateinit var navController: NavController
    private lateinit var jsonParser: JsonParser


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        navController = this.findNavController()

        jsonParser = JsonParser.getInstance(requireContext())
        val keys = listOf("Lines", "Clefs", "Notes", "Rests", "Articulations", "Time Signatures", "Key Signatures")
        val categories: MutableList<List<CategoryItem>> = arrayListOf()
        try {
            for (item in keys.indices) {
                categories.add(jsonParser.getList(keys[item], item))
            }
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }
        val allCategory: MutableList<AllCategory> = arrayListOf()
        for (item in keys.indices) {
            allCategory.add(AllCategory(keys[item], categories[item]))
        }
        setMainCategoryRecycler(allCategory)


        binding.editTextGlossary.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mainRecyclerAdapter?.filter?.filter(s)

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }



    private fun setMainCategoryRecycler(allCategory: List<AllCategory>){
        val layoutManager:RecyclerView.LayoutManager = LinearLayoutManager(this.context)
        binding.mainRecycler.layoutManager = layoutManager
        mainRecyclerAdapter = this.context?.let { MainRecyclerAdapter(it, allCategory, this) }
        binding.mainRecycler.adapter = mainRecyclerAdapter
    }

    override fun onItemClick(position: Int, itemtext: String, itemdefinition: String) {
        val bundle = bundleOf("term" to itemtext, "definition" to itemdefinition)
        navController.navigate(R.id.action_glossaryFragment_to_glossaryDefinitionFragment, bundle)
    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_glossary, container, false)
    }

    companion object


}