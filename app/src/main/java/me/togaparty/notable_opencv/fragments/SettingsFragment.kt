package me.togaparty.notable_opencv.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_files.*
import kotlinx.android.synthetic.main.fragment_files.recycler_view
import kotlinx.android.synthetic.main.fragment_settings.*
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.utils.ExampleAdapter
import me.togaparty.notable_opencv.utils.ExampleItem

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment(), ExampleAdapter.OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val exampleList = generateDummyList(5)
    private val adapter = ExampleAdapter(exampleList, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exampleList = generateDummyList(5)
        settings_recycler_view.adapter = adapter
        settings_recycler_view.layoutManager = LinearLayoutManager(this.context)
        settings_recycler_view.setHasFixedSize(true)
    }

    private fun generateDummyList(size: Int): List<ExampleItem> {
        val list = ArrayList<ExampleItem>()
        for (i in 0 until size) {
            val item = ExampleItem("Setting $i", "Line 2")
            list += item
        }
        return list
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this.context, "Setting $position clicked", Toast.LENGTH_SHORT).show()
    }
}