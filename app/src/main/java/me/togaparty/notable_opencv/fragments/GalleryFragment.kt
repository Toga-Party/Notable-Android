package me.togaparty.notable_opencv.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.item_gallery_image.view.*
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.adapter.GalleryImage
import me.togaparty.notable_opencv.adapter.GalleryImageClickListener
import me.togaparty.notable_opencv.helper.GlideApp
import me.togaparty.notable_opencv.helper.OnDismissListener
import me.togaparty.notable_opencv.utils.FILE_REQUIRED_PERMISSIONS
import me.togaparty.notable_opencv.utils.FileWorker
import me.togaparty.notable_opencv.utils.ImageListProvider
import me.togaparty.notable_opencv.utils.permissionsGranted

class GalleryFragment : Fragment(),
        GalleryImageClickListener, OnDismissListener {
    // Gallery Column Count
    private val spanCount = 2

    private lateinit var galleryAdapter: GalleryImageAdapter
    private lateinit var navController: NavController

    private val model: ImageListProvider by activityViewModels()

    private lateinit var fileWorker: FileWorker

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Gallery", "onViewCreatedCalled")
        navController = this.findNavController()
        if(!permissionsGranted(requireContext(), FILE_REQUIRED_PERMISSIONS)) {
            navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToDashboardFragment())
        }
        // init adapter
        galleryAdapter = GalleryImageAdapter(model.imageList.value)
        galleryAdapter.listener = this
        fileWorker = FileWorker()
        model.imageList.observe(viewLifecycleOwner, {
            Log.d("GalleryFragment", "Something changed")
            galleryAdapter.notifyDataSetChanged()
        })
        // init recyclerview
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        recyclerView.adapter = galleryAdapter
    }


    override fun onClick(position: Int) {
        val bundle = Bundle()
            bundle.putInt("position", position)
        val fragmentTransaction = childFragmentManager.beginTransaction()
        val galleryFragment = GalleryFullscreenFragment()
        galleryFragment.arguments = bundle
        galleryFragment.show(fragmentTransaction, "gallery")
    }
//    override fun onDialogDismiss() {
//        galleryAdapter.notifyDataSetChanged()
//    }
    inner class GalleryImageAdapter(private val itemList: ArrayList<GalleryImage>?) : RecyclerView.Adapter<GalleryImageAdapter.ViewHolder>() {
        private var context: Context? = null
        var listener: GalleryImageClickListener? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryImageAdapter.ViewHolder {
            context = parent.context
            val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.item_gallery_image, parent,
                    false)
            return ViewHolder(view)
        }
        override fun getItemCount(): Int {
            return itemList?.size ?: 0
        }
        override fun onBindViewHolder(holder: GalleryImageAdapter.ViewHolder, position: Int) {
            holder.bind()
        }
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind() {
                val image = itemList!![adapterPosition]

                val circularProgressDrawable = context?.let { CircularProgressDrawable(it) }
                if (circularProgressDrawable != null) {
                    circularProgressDrawable.strokeWidth = 5f
                }
                if (circularProgressDrawable != null) {
                    circularProgressDrawable.centerRadius = 30f
                }
                circularProgressDrawable?.start()

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
    override fun onDialogDismiss() {

    }
    companion object

    override




}