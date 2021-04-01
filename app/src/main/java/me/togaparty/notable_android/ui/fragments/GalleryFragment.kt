package me.togaparty.notable_android.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.data.ImageListProvider
import me.togaparty.notable_android.databinding.FragmentGalleryBinding
import me.togaparty.notable_android.helper.GlideApp
import me.togaparty.notable_android.ui.adapter.GalleryImageClickListener
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import me.togaparty.notable_android.utils.FILE_REQUIRED_PERMISSIONS
import me.togaparty.notable_android.utils.permissionsGranted

private const val COLUMN_COUNT = 2
class GalleryFragment:
    Fragment(R.layout.fragment_gallery),
    GalleryImageClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private val binding by viewBinding(FragmentGalleryBinding::bind)
    private lateinit var galleryAdapter: GalleryImageAdapter
    private lateinit var navController: NavController
    private lateinit  var model: ImageListProvider


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
        model.getList().observe(viewLifecycleOwner, {
            galleryAdapter.notifyDataSetChanged()
        })

        binding.swipeContainer.setOnRefreshListener(this)
        binding.swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        )
        // init recyclerview
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), COLUMN_COUNT)
        binding.recyclerView.adapter = galleryAdapter
        binding.importImage.setOnClickListener { openGallery()}
    }

    override fun onRefresh() {

        Log.d(TAG, "Gallery: Refreshing list")
        GlobalScope.launch(Dispatchers.Main) {
            model.refreshList()
            galleryAdapter.notifyDataSetChanged()
        }
        binding.swipeContainer.isRefreshing = false

    }
    override fun onClick(position: Int) {
        val bundle = Bundle()
            bundle.putInt("position", position)
        val fragmentTransaction = childFragmentManager.beginTransaction()
        val galleryFragment = GalleryFullscreenFragment()
        galleryFragment.arguments = bundle
        galleryFragment.show(fragmentTransaction, "gallery")
    }
    fun openGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "*/*"
        val mimeTypes = arrayOf("image/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, 102)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 102 && resultCode == RESULT_OK && data != null) {
            Log.d(TAG,"Getting data from gallery")
            lifecycleScope.launch(Dispatchers.IO) {
                model.copyImageToList(data)
            }
        }

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

        inner class ViewHolder(
            itemView: View
        ) : RecyclerView.ViewHolder(itemView) {
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
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(itemView.findViewById(R.id.ivGalleryImage))
                // adding click or tap handler for our image layout
                itemView.findViewById<View>(R.id.container).setOnClickListener {
                    listener?.onClick(adapterPosition)
                }
            }

        }
    }
}