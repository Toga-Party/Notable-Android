package me.togaparty.notable_android.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_glossary.*
import me.togaparty.notable_android.R
import me.togaparty.notable_android.ui.adapter.CategoryItemAdapter
import me.togaparty.notable_android.ui.adapter.MainRecyclerAdapter
import me.togaparty.notable_android.ui.items.AllCategory
import me.togaparty.notable_android.ui.items.CategoryItem
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset


class GlossaryFragment : Fragment(), CategoryItemAdapter.OnItemClickListener{
    private var mainRecyclerAdapter: MainRecyclerAdapter? = null
    private lateinit var navController: NavController
    private lateinit var editText: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editText = view.findViewById(R.id.edit_text_glossary)
//        mainRecyclerAdapter = view.findViewById(R.id.main_recycler)

        navController = this.findNavController()

        val categoryItemList: MutableList<CategoryItem> = ArrayList()
        val categoryItemList2: MutableList<CategoryItem> = ArrayList()
        val categoryItemList3: MutableList<CategoryItem> = ArrayList()
        val categoryItemList4: MutableList<CategoryItem> = ArrayList()
        val categoryItemList5: MutableList<CategoryItem> = ArrayList()
        val categoryItemList6: MutableList<CategoryItem> = ArrayList()
        val categoryItemList7: MutableList<CategoryItem> = ArrayList()

        try {
            val obj = JSONObject(loadJSONFromAsset())

            val userArray = obj.getJSONArray("Lines")
            for (i in 0 until userArray.length()) {
                val termdefinition = userArray.getJSONObject(i)
                categoryItemList.add(
                        CategoryItem(
                                1, termdefinition.getString("name"), termdefinition.getString(
                                "definition"
                        )
                        )
                )
            }

            val userArray2 = obj.getJSONArray("Clefs")
            for (i in 0 until userArray2.length()) {
                val termdefinition = userArray2.getJSONObject(i)
                categoryItemList2.add(
                        CategoryItem(
                                2, termdefinition.getString("name"), termdefinition.getString(
                                "definition"
                        )
                        )
                )
            }

            val userArray3 = obj.getJSONArray("Notes")
            for (i in 0 until userArray3.length()) {
                val termdefinition = userArray3.getJSONObject(i)
                categoryItemList3.add(
                        CategoryItem(
                                3, termdefinition.getString("name"), termdefinition.getString(
                                "definition"
                        )
                        )
                )
            }

            val userArray4 = obj.getJSONArray("Rests")
            for (i in 0 until userArray4.length()) {
                val termdefinition = userArray4.getJSONObject(i)
                categoryItemList4.add(
                        CategoryItem(
                                4, termdefinition.getString("name"), termdefinition.getString(
                                "definition"
                        )
                        )
                )
            }

            val userArray5 = obj.getJSONArray("Articulations")
            for (i in 0 until userArray5.length()) {
                val termdefinition = userArray5.getJSONObject(i)
                categoryItemList5.add(
                        CategoryItem(
                                5, termdefinition.getString("name"), termdefinition.getString(
                                "definition"
                        )
                        )
                )
            }

            val userArray6 = obj.getJSONArray("Time Signatures")
            for (i in 0 until userArray6.length()) {
                val termdefinition = userArray6.getJSONObject(i)
                categoryItemList6.add(
                        CategoryItem(
                                6, termdefinition.getString("name"), termdefinition.getString(
                                "definition"
                        )
                        )
                )
            }

            val userArray7 = obj.getJSONArray("Key Signatures")
            for (i in 0 until userArray7.length()) {
                val termdefinition = userArray7.getJSONObject(i)
                categoryItemList7.add(
                        CategoryItem(
                                7, termdefinition.getString("name"), termdefinition.getString(
                                "definition"
                        )
                        )
                )
            }
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

        editText.addTextChangedListener(object:TextWatcher{
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
        main_recycler!!.layoutManager = layoutManager
        mainRecyclerAdapter = this.context?.let { MainRecyclerAdapter(it, allCategory, this) }
        main_recycler!!.adapter = mainRecyclerAdapter
    }

    private fun loadJSONFromAsset(): String {
        val json: String?
        try {
            val inputStream = context?.assets?.open("glossary.json")
            val size = inputStream?.available()
            val buffer = size?.let { ByteArray(it) }
            val charset: Charset = Charsets.UTF_8
            inputStream?.read(buffer)
            inputStream?.close()
            json = buffer?.let { String(it, charset) }
        }
        catch (ex: IOException) {
            ex.printStackTrace()
            return ""
        }
        return json!!
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