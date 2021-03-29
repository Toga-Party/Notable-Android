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
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.files.JsonParser
import me.togaparty.notable_android.databinding.FragmentGlossaryBinding
import me.togaparty.notable_android.ui.adapter.CategoryItemAdapter
import me.togaparty.notable_android.ui.adapter.MainRecyclerAdapter
import me.togaparty.notable_android.ui.items.AllCategory
import me.togaparty.notable_android.ui.items.CategoryItem
import me.togaparty.notable_android.utils.viewBindingWithBinder
import org.json.JSONException


class GlossaryFragment : Fragment(R.layout.fragment_glossary),
    CategoryItemAdapter.OnItemClickListener
{
    internal var mainRecyclerAdapter: MainRecyclerAdapter? = null
    private lateinit var navController: NavController
    private lateinit var jsonParser: JsonParser
    private val binding by viewBindingWithBinder(FragmentGlossaryBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        navController = this.findNavController()

        jsonParser = JsonParser.getInstance(requireContext())
        val categoryItemList: MutableList<CategoryItem> = ArrayList()
        val categoryItemList2: MutableList<CategoryItem> = ArrayList()
        val categoryItemList3: MutableList<CategoryItem> = ArrayList()
        val categoryItemList4: MutableList<CategoryItem> = ArrayList()
        val categoryItemList5: MutableList<CategoryItem> = ArrayList()
        val categoryItemList6: MutableList<CategoryItem> = ArrayList()
        val categoryItemList7: MutableList<CategoryItem> = ArrayList()

        try {
            categoryItemList.addAll(jsonParser.getList("Lines", 1))
            categoryItemList2.addAll(jsonParser.getList("Clefs", 2))
            categoryItemList3.addAll(jsonParser.getList("Notes", 3))
            categoryItemList4.addAll(jsonParser.getList("Rests", 4))
            categoryItemList5.addAll(jsonParser.getList("Articulations", 5))
            categoryItemList6.addAll(jsonParser.getList("Time Signatures", 6))
            categoryItemList7.addAll(jsonParser.getList("Key Signatures", 7))
        }
        catch (e: JSONException) {
            e.printStackTrace()
        }


        val allCategory: MutableList<AllCategory> = ArrayList()
        allCategory.add(AllCategory("Lines", categoryItemList))
        allCategory.add(AllCategory("Clefs", categoryItemList2))
        allCategory.add(AllCategory("Notes", categoryItemList3))
        allCategory.add(AllCategory("Rests", categoryItemList4))
        allCategory.add(AllCategory("Articulations", categoryItemList5))
        allCategory.add(AllCategory("Time Signatures", categoryItemList6))
        allCategory.add(AllCategory("Key Signatures", categoryItemList7))

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