package me.togaparty.notable_opencv.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_settings.*
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.utils.ExampleAdapter
import me.togaparty.notable_opencv.utils.ExampleItem

class SettingsFragment : Fragment(), ExampleAdapter.OnItemClickListener {

    private val exampleList = generateDummyList(5)
    private val adapter = ExampleAdapter(exampleList, this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generateDummyList(5)
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
    override fun onItemClick(position: Int) {
        Toast.makeText(this.context, "Setting $position clicked", Toast.LENGTH_SHORT).show()
    }
    companion object


}