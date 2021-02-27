@file:Suppress("unused", "unused", "unused")

package me.togaparty.notable_android.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.togaparty.notable_android.R
import me.togaparty.notable_android.ui.items.CategoryItem

class CategoryItemAdapter(
    private var categoryItem: List<CategoryItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<CategoryItemAdapter.CategoryItemViewHolder>() {

    inner class CategoryItemViewHolder(inflater: LayoutInflater, parent: ViewGroup )
        : RecyclerView.ViewHolder(inflater.inflate(R.layout.cat_row_items, parent, false)), View.OnClickListener {
        var itemText:TextView = itemView.findViewById(R.id.item_text)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            val itemtext = categoryItem[position].itemText
            val itemdefinition = categoryItem[position].itemDefinition
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