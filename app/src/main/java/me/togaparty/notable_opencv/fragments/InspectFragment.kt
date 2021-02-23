package me.togaparty.notable_opencv.fragments

//import kotlinx.android.synthetic.main.fragment_glossary.*
//import me.togaparty.notable_opencv.adapter.MainRecyclerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.fragment_inspect.*
import me.togaparty.notable_opencv.R

class InspectFragment : Fragment() {
    //private lateinit var mainCategoryRecycler: RecyclerView
    //private var mainRecyclerAdapter: MainRecyclerAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

//    private fun setMainCategoryRecycler(allCategory: List<AllCategory>){
//        //mainCategoryRecycler = findViewById(R.id.main_recycler)
//        val layoutManager:RecyclerView.LayoutManager = LinearLayoutManager(this.context)
//        main_recycler!!.layoutManager = layoutManager
//        mainRecyclerAdapter = this.context?.let { MainRecyclerAdapter(it,allCategory) }
//        main_recycler!!.adapter = mainRecyclerAdapter
//    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inspect, container, false)
    }

    companion object
}