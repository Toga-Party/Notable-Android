package me.togaparty.notable_opencv.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.model.AllCategory
import me.togaparty.notable_opencv.model.CategoryItem

class CategoryItemAdapter(private val context:Context,
                          private var categoryItem:List<CategoryItem>,
                          private val listener : OnItemClickListener
                          ) : RecyclerView.Adapter<CategoryItemAdapter.CategoryItemViewHolder>() {

    inner class CategoryItemViewHolder(inflater: LayoutInflater, parent: ViewGroup )
        : RecyclerView.ViewHolder(inflater.inflate(R.layout.cat_row_items, parent, false)), View.OnClickListener {
        var itemText:TextView = itemView.findViewById(R.id.item_text)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            var itemtext = categoryItem[position].itemText
            var itemdefinition = categoryItem[position].itemDefinition
            if(position != RecyclerView.NO_POSITION){
                listener.onItemClick(position, itemtext, itemdefinition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
       return CategoryItemViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: CategoryItemViewHolder, position: Int) {
        holder.itemText.text = categoryItem[position].itemText
    }

    override fun getItemCount(): Int {
        return categoryItem.size
    }


    interface OnItemClickListener{
        fun onItemClick(position: Int, itemtext: String, itemdefinition: String)
    }

}