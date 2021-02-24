package me.togaparty.notable_opencv.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.model.Inspect_Prediction


class PredictionsAdapter(private val predictions: List<Inspect_Prediction>,private val listener : OnItemClickListener) : RecyclerView.Adapter<PredictionsAdapter.ViewHolder>() {

    interface OnItemClickListener{
        fun onItemClick(position: Int, view: TextView)
    }
    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView), View.OnClickListener {
        val symbolTextView: TextView = itemView.findViewById<TextView>(R.id.symbol_name)
        val toGalleryButton: Button = itemView.findViewById<Button>(R.id.toGlossary_button)
        init {
            toGalleryButton.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                listener.onItemClick(position, symbolTextView)
            }

        }
    }
    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredictionsAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val contactView = inflater.inflate(R.layout.fragment_inspect_recycleritem, parent, false)
        // Return a new holder instance
        return ViewHolder(contactView)
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(viewHolder: PredictionsAdapter.ViewHolder, position: Int) {
        // Get the data model based on position
        val row: Inspect_Prediction = predictions.get(position)
        // Set item views based on your views and data model
        val textView = viewHolder.symbolTextView
        textView.setText(row.symbol)
        val button = viewHolder.toGalleryButton
        button.text = if (row.in_Glossary) "Review" else "Missing"
        button.isEnabled = row.in_Glossary
    }

    // Returns the total count of items in the list
    override fun getItemCount(): Int {
        return predictions.size
    }
}