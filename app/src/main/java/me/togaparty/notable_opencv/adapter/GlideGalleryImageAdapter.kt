package me.togaparty.notable_opencv.adapter

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_gallery_image.view.*
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.helper.GlideApp
import java.io.File

class GlideGalleryImageAdapter(private val itemList: List<GlideImage>) : RecyclerView.Adapter<GlideGalleryImageAdapter.ViewHolder>() {
    private var context: Context? = null
    var listener: GlideGalleryImageClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlideGalleryImageAdapter.ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_gallery_image, parent,
            false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int {
        return itemList.size
    }
    override fun onBindViewHolder(holder: GlideGalleryImageAdapter.ViewHolder, position: Int) {
        holder.bind()
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            val image = itemList.get(adapterPosition)

            val circularProgressDrawable = context?.let { CircularProgressDrawable(it) }
            if (circularProgressDrawable != null) {
                circularProgressDrawable.strokeWidth = 5f
            }
            if (circularProgressDrawable != null) {
                circularProgressDrawable.centerRadius = 30f
            }
            if (circularProgressDrawable != null) {
                circularProgressDrawable.start()
            }


            // load image
            GlideApp.with(context!!)
                .load(image.imageUrl)
                .placeholder(circularProgressDrawable)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(itemView.ivGalleryImage)
            // adding click or tap handler for our image layout
            itemView.container.setOnClickListener {
                listener?.onClick(adapterPosition)
            }
        }
    }
}