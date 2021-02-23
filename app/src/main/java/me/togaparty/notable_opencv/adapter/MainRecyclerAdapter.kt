package me.togaparty.notable_opencv.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.adapter.CategoryItemAdapter.OnItemClickListener
import me.togaparty.notable_opencv.model.AllCategory
import me.togaparty.notable_opencv.model.CategoryItem

class MainRecyclerAdapter(private val context: Context,
                          private val allCategory: List<AllCategory>,
                          private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<MainRecyclerAdapter.MainViewHolder>() {

    class MainViewHolder(
            inflater: LayoutInflater,
            parent: ViewGroup,
    ):
            RecyclerView.ViewHolder(inflater.inflate(R.layout.main_recycler_row_item, parent, false)){
        var categoryTitle: TextView = itemView.findViewById(R.id.cat_title)
        var itemRecycler:RecyclerView = itemView.findViewById(R.id.cat_item_recycler)

        init {


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MainViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.categoryTitle.text = allCategory[position].categoryTitle
        setCatItemRecycler(holder.itemRecycler, allCategory[position].categoryItem)
    }

    override fun getItemCount(): Int {
        return allCategory.size
    }

    private fun setCatItemRecycler(recyclerView: RecyclerView, categoryItem: List<CategoryItem>){
        val itemRecyclerAdapter = CategoryItemAdapter(context, categoryItem, listener)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = itemRecyclerAdapter
    }

}