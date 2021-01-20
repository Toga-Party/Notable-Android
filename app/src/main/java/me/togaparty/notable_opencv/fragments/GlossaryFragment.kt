package me.togaparty.notable_opencv.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_glossary.*
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.adapter.MainRecyclerAdapter
import me.togaparty.notable_opencv.model.AllCategory
import me.togaparty.notable_opencv.model.CategoryItem

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GlossaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GlossaryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var mainCategoryRecycler:RecyclerView? = null
    private var mainRecyclerAdapter:MainRecyclerAdapter?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryItemList: MutableList<CategoryItem> = ArrayList()
        categoryItemList.add(CategoryItem(1,"Melody"))
        categoryItemList.add(CategoryItem(1,"Harmony"))
        categoryItemList.add(CategoryItem(1,"Rhythm"))
        categoryItemList.add(CategoryItem(1,"Tempo"))
        categoryItemList.add(CategoryItem(1,"Dynamics"))

        val categoryItemList2: MutableList<CategoryItem> = ArrayList()
        categoryItemList2.add(CategoryItem(2,"Key"))
        categoryItemList2.add(CategoryItem(2,"Major"))
        categoryItemList2.add(CategoryItem(2,"Scale"))
        categoryItemList2.add(CategoryItem(2,"Half step"))
        categoryItemList2.add(CategoryItem(2,"Octave"))

        val categoryItemList3: MutableList<CategoryItem> = ArrayList()
        categoryItemList3.add(CategoryItem(3,"Score"))
        categoryItemList3.add(CategoryItem(3,"Staff"))
        categoryItemList3.add(CategoryItem(3,"Grand Staff"))
        categoryItemList3.add(CategoryItem(3,"Note"))
        categoryItemList3.add(CategoryItem(3,"Clef"))

        val categoryItemList4: MutableList<CategoryItem> = ArrayList()
        categoryItemList4.add(CategoryItem(4,"Largo"))
        categoryItemList4.add(CategoryItem(4,"Andante"))
        categoryItemList4.add(CategoryItem(4,"Allegreto"))
        categoryItemList4.add(CategoryItem(4,"Allegro"))
        categoryItemList4.add(CategoryItem(4,"Accelerando"))


        val allCategory: MutableList<AllCategory> = ArrayList()
        allCategory.add(AllCategory("General Terms", categoryItemList))
        allCategory.add(AllCategory("Key and Scales", categoryItemList2))
        allCategory.add(AllCategory("Music Notation", categoryItemList3))
        allCategory.add(AllCategory("Tempo Terms", categoryItemList4))

        setMainCategoryRecycler(allCategory)

    }

    private fun setMainCategoryRecycler(allCategory: List<AllCategory>){
        //mainCategoryRecycler = findViewById(R.id.main_recycler)
        val layoutManager:RecyclerView.LayoutManager = LinearLayoutManager(this.context)
        main_recycler!!.layoutManager = layoutManager
        mainRecyclerAdapter = this.context?.let { MainRecyclerAdapter(it,allCategory) }
        main_recycler!!.adapter = mainRecyclerAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_glossary, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment GlossaryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GlossaryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}