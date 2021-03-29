package me.togaparty.notable_android.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.leinardi.android.speeddial.SpeedDialActionItem
import kotlinx.coroutines.*
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.data.ImageListProvider
import me.togaparty.notable_android.databinding.FragmentGalleryFullscreenBinding
import me.togaparty.notable_android.helper.GlideApp
import me.togaparty.notable_android.helper.GlideZoomOutPageTransformer
import me.togaparty.notable_android.utils.*
import me.togaparty.notable_android.utils.Constants.Companion.TAG


class GalleryFullscreenFragment : DialogFragment(R.layout.fragment_gallery_fullscreen) {

    private val binding by viewBindingWithBinder(FragmentGalleryFullscreenBinding::bind)
    private lateinit var viewPager: ViewPager
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter

    private lateinit var currentImage: GalleryImage
    private lateinit var navController: NavController

    private var fileUri: Uri? = null
    private var selectedPosition: Int = 0
    internal lateinit var model: ImageListProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = this.findNavController()
        galleryPagerAdapter = GalleryPagerAdapter()

        model = ViewModelProvider(requireActivity()).get(ImageListProvider::class.java)

        viewPager = binding.viewPager
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())

        setCurrentItem(requireArguments().getInt("position"))
        generateFloatingActionButton()

        model.getList().observe(viewLifecycleOwner, {
            Log.d(TAG, "Fullscreen: Something changed")
            viewPager.adapter?.notifyDataSetChanged()

            if (model.getImageListSize() == 0) dismiss() else setCurrentItem(selectedPosition)

            editFloatingActionButton()
            activity?.let {
                when (model.getProcessingStatus()) {
                    Status.FAILED -> {
                        showFailedDialog("Upload failed",
                            "The upload you sent failed.")
                        model.setProcessingStatus(Status.AVAILABLE)

                    }
                    Status.SUCCESSFUL -> {
                        showSuccessDialog(
                            "Processing finished",
                            "We have received the response from the server want to " +
                                    "inspect it?"
                        ) {navigateToInspect()}
                        model.setProcessingStatus(Status.AVAILABLE)
                    }
                    else -> Unit
                }
            }
        })
    }

    private fun navigateToInspect() {
        dismiss()
        val bundle = bundleOf("currentImage" to currentImage)
        navController.navigate(
                R.id.action_galleryFragment_to_inspectFragment,
                bundle
        )
    }
    private fun editFloatingActionButton() {
        val floatingActionButton = binding.speedDial

        if (currentImage.processed == true) {
            floatingActionButton.removeActionItem(1)
            floatingActionButton.addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_inspect, R.drawable.search_icon)
                    .setLabel(getString(R.string.inspect))
                    .setTheme(R.style.Theme_Notable_OPENCV)
                    .setLabelClickable(false)
                    .create()
            )
        } else {
            floatingActionButton.removeActionItem(1)
            floatingActionButton.addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_process, R.drawable.sync)
                    .setLabel(getString(R.string.process_music))
                    .setTheme(R.style.Theme_Notable_OPENCV)
                    .setLabelClickable(false)
                    .create()
            )
        }

    }

    private fun generateFloatingActionButton() {
        val floatingActionButton = binding.speedDial
        floatingActionButton.addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_delete, R.drawable.ic_delete_black)
                        .setLabel(getString(R.string.delete))
                        .setTheme(R.style.Theme_Notable_OPENCV)
                        .setLabelClickable(false)
                        .create()
        )
        floatingActionButton.addActionItem(when (currentImage.processed == true) {

            true -> SpeedDialActionItem.Builder(R.id.fab_inspect, R.drawable.search_icon)
                    .setLabel(getString(R.string.inspect))
                    .setTheme(R.style.Theme_Notable_OPENCV)
                    .setLabelClickable(false)
                    .create()

            else -> SpeedDialActionItem.Builder(R.id.fab_process, R.drawable.sync)
                    .setLabel(getString(R.string.process_music))
                    .setTheme(R.style.Theme_Notable_OPENCV)
                    .setLabelClickable(false)
                    .create()
        })

        floatingActionButton.setOnActionSelectedListener { actionItem ->

            when (actionItem.id) {
                R.id.fab_delete -> {
                    if (model.getProcessingTag() != null &&
                            model.getProcessingTag() == currentImage.imageUrl) {
                        toast("Can't delete something that's being processed")
                        Log.d(TAG, "Full screen: delete failed")

                    } else {
                        GlobalScope.launch(Dispatchers.Main) {

                            Log.d(TAG, "Full screen: deleting")
                            if (model.getImageListSize() != 0) {

                                model.deleteGalleryImage(
                                        selectedPosition,
                                        currentImage.imageUrl
                                )
                            }

                        }
                        Log.d(TAG, "Full screen: deleted")
                    }
                }
                R.id.fab_inspect -> navigateToInspect()
                R.id.fab_process -> {
                    if (model.getProcessingStatus() != Status.PROCESSING) {
                        Log.d(TAG, "Status: ${model.getProcessingStatus().name}")
                        processImage()

                    } else {
                        toast("Something is processing. Please wait for it to finish")
                    }
                }
            }
            true
        }
    }

    private fun processImage() {

        if (ConnectionDetector(requireContext()).connected) {
            toast("Processing image")
            val loadingFragment = LoadingFragment.show(childFragmentManager)
            lifecycleScope.launch {
                val deferred = GlobalScope.async(Dispatchers.IO + NonCancellable) {
                    model.uploadImage(currentImage, selectedPosition)
                }
                deferred.await()
                withContext(Dispatchers.Main) {loadingFragment.dismiss()}
            }

        } else {
            toast("Please connect to the internet")
        }
    }


    internal fun setCurrentItem(position: Int) {
        viewPager.setCurrentItem(position, false)
        currentImage = model.getGalleryImage(position)
        selectedPosition = position
        fileUri = currentImage.imageUrl
        editFloatingActionButton()
    }


    // viewpager page change listener
    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
            object : ViewPager.OnPageChangeListener {
                override fun onPageSelected(position: Int) {
                    setCurrentItem(position)
                }

                override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
                }

                override fun onPageScrollStateChanged(arg0: Int) {
                }
            }

    //Gallery adapter
    inner class GalleryPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                    as LayoutInflater

            val view = layoutInflater.inflate(
                    R.layout.gallery_image_fullscreen,
                    container,
                    false
            )

            val image = model.getGalleryImage(position)

            view.findViewById<ImageView>(R.id.ivFullscreenImage).tag = image.imageUrl

            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            // load image
            GlideApp.with(context!!)
                    .load(image.imageUrl)
                    .placeholder(circularProgressDrawable)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view.findViewById(R.id.ivFullscreenImage))

            container.addView(view)
            return view
        }

        override fun getItemPosition(`object`: Any): Int {
            val imageView = `object` as ImageView
            val tag = imageView.tag

            var flag = false
            model.getList().value?.forEach {
                if (it.imageUrl == tag) {
                    flag = true
                    return@forEach
                }
            }
            return if (flag) super.getItemPosition(`object`) else POSITION_NONE
        }

        override fun getCount(): Int {
            return model.getImageListSize()
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj as View
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }
    }

}