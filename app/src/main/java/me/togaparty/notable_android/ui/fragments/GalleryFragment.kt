package me.togaparty.notable_android.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.item_gallery_image.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.data.ImageListProvider
import me.togaparty.notable_android.helper.GlideApp
import me.togaparty.notable_android.ui.adapter.GalleryImageClickListener
import me.togaparty.notable_android.utils.*
import me.togaparty.notable_android.utils.Constants.Companion.TAG


class GalleryFragment : Fragment(),
        GalleryImageClickListener, SwipeRefreshLayout.OnRefreshListener {
    // Gallery Column Count
    private val spanCount = 2

    private lateinit var galleryAdapter: GalleryImageAdapter
    private lateinit var navController: NavController
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit  var model: ImageListProvider// by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = this.findNavController()
        if(!permissionsGranted(requireContext(), FILE_REQUIRED_PERMISSIONS)) {
            navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToDashboardFragment())
        }
        // init adapter
        model = ViewModelProvider(requireActivity()).get(ImageListProvider::class.java)


        galleryAdapter = GalleryImageAdapter(model.getList().value as MutableList<GalleryImage>)
        galleryAdapter.listener = this
        swipeRefreshLayout = view.findViewById(R.id.swipeContainer)

        model.getList().observe(viewLifecycleOwner, {
            Log.d(TAG, "Gallery: Something changed")

            activity?.let {
                when (model.getProcessingStatus()) {
                    Status.FAILED -> {
                        showFailedDialog("Upload failed", "The upload you sent failed.")
                        model.setProcessingStatus(Status.AVAILABLE)
                    }
                    Status.SUCCESSFUL -> {
                        showSuccessDialog(
                            "Processing finished",
                            "We have received the response from the server. " +
                                    "Please check the image you processed earlier to inspect it."
                        ) {}
                        model.setProcessingStatus(Status.AVAILABLE)
                    }

                    else -> Unit
                }
            }
            swipeRefreshLayout.setOnRefreshListener(this)
            swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
            )

            galleryAdapter.notifyDataSetChanged()
        })
        // init recyclerview
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        recyclerView.adapter = galleryAdapter
    }

    override fun onRefresh() {

        Log.d(TAG, "Gallery: Refreshing list")
        GlobalScope.launch(Dispatchers.Main) {
            model.refreshList()
            galleryAdapter.notifyDataSetChanged()
        }
        swipeRefreshLayout.isRefreshing = false

    }
    override fun onClick(position: Int) {
        val bundle = Bundle()
            bundle.putInt("position", position)
        val fragmentTransaction = childFragmentManager.beginTransaction()
        val galleryFragment = GalleryFullscreenFragment()
        galleryFragment.arguments = bundle
        galleryFragment.show(fragmentTransaction, "gallery")
    }

    inner class GalleryImageAdapter(private val itemList: MutableList<GalleryImage>) :
        RecyclerView.Adapter<GalleryImageAdapter.ViewHolder>() {

        internal var context: Context? = null
        var listener: GalleryImageClickListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            context = parent.context
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_gallery_image, parent,
                false
            )
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return itemList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind() {
                val image = itemList[adapterPosition]

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
}