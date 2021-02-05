package me.togaparty.notable_opencv.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.utils.ExampleAdapter
import me.togaparty.notable_opencv.utils.ExampleItem
import kotlinx.android.synthetic.main.fragment_files.*

class FilesFragment : Fragment(), ExampleAdapter.OnItemClickListener{

    private val exampleList = generateDummyList(20)
    private val adapter = ExampleAdapter(exampleList, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val result = "FilesFragment"
        setFragmentResult("requestKey", bundleOf("filesFragment" to result))
        if (savedInstanceState == null) {
            if (!PermissionsFragment.allPermissionsGranted(requireContext())) {
                NavHostFragment.findNavController(this)
                    .navigate(FilesFragmentDirections.actionFilesFragmentToPermissionsFragment())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_files, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exampleList = generateDummyList(20)
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this.context)
        recycler_view.setHasFixedSize(true)
    }

    override fun onItemClick(position: Int) {
        Toast.makeText(this.context, "Item $position clicked", Toast.LENGTH_SHORT).show()
//        val clickedItem:ExampleItem = exampleList[position]
//        clickedItem.text1 = "Clicked"
    }

    private fun generateDummyList(size: Int): List<ExampleItem> {
        val list = ArrayList<ExampleItem>()
        for (i in 0 until size) {
            val item = ExampleItem("Item $i", "Line 2")
            list += item
        }
        return list
    }

    companion object {
    }
}