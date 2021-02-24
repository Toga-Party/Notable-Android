package me.togaparty.notable_opencv.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.gallery_image_fullscreen.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.adapter.GalleryImage
import me.togaparty.notable_opencv.helper.GlideApp
import me.togaparty.notable_opencv.helper.GlideZoomOutPageTransformer
import me.togaparty.notable_opencv.network.RetrofitUploader
import me.togaparty.notable_opencv.utils.FileWorker
import me.togaparty.notable_opencv.utils.ImageListProvider
import me.togaparty.notable_opencv.utils.toast
import java.io.File


class GalleryFullscreenFragment : DialogFragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter

    private lateinit var currentImage: GalleryImage
    private lateinit var navController: NavController

    private var fileUri: Uri? = null
    private var selectedPosition: Int = 0
    private var processed: Boolean = false

    private lateinit var retrofitUploader: RetrofitUploader
    private lateinit var fileWorker: FileWorker

    private val model: ImageListProvider by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        fileWorker = FileWorker()
        //Detect rar directory, exists = true
        processed = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("GalleryFullscreenDebug", "Fullscreen called.")
        val view = inflater.inflate(
                R.layout.fragment_gallery_fullscreen,
                container,
                false
        )

        navController = this.findNavController()
        retrofitUploader = RetrofitUploader()

        galleryPagerAdapter = GalleryPagerAdapter()

        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())

        setCurrentItem(requireArguments().getInt("position"))
        generateFloatingActionButton(view)

        return view
    }

    private fun generateFloatingActionButton(view: View){
        val floatingActionButton = view.findViewById<SpeedDialView>(R.id.speedDial)

        floatingActionButton.addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_delete, R.drawable.ic_delete_black)
                        .setLabel(getString(R.string.delete))
                        .setTheme(R.style.Theme_Notable_OPENCV)
                        .setLabelClickable(false)
                        .create()
        )
        floatingActionButton.addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_process, R.drawable.sync)
                        .setLabel(getString(R.string.process_music))
                        .setTheme(R.style.Theme_Notable_OPENCV)
                        .setLabelClickable(false)
                        .create()
        )
        floatingActionButton.addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_inspect, R.drawable.search_icon)
                        .setLabel(getString(R.string.inspect))
                        .setTheme(R.style.Theme_Notable_OPENCV)
                        .setLabelClickable(false)
                        .create()
        )


        floatingActionButton.setOnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.fab_delete -> {
                    toast("Delete action")
                    Log.d("delete", "delete launched")
                    Log.d("delete", currentImage.imageUrl.toString() + " " + currentImage.name)
                    GlobalScope.launch(Dispatchers.Main) {
                        Log.d("delete", "deleting")

                        if(model.getImageListSize() != 0){
                            fileWorker.deleteImage(
                                    currentImage.imageUrl,
                                    requireContext()
                            )
                            model.deleteGalleryImage(selectedPosition)
                            viewPager.adapter?.notifyDataSetChanged()
                        }

                        if(model.getImageListSize() == 0) dismiss() else setCurrentItem(selectedPosition)
                    }
                    Log.d("delete", "done deleting")
                }
                R.id.fab_inspect -> {
                    toast("Inspect action")
                    //val bundle = Bundle()
                    //bundle.putParcelable("imageUri", currentImage.imageUrl);
                    //inspectFragment.setArguments(bundle)
                    //inspectFragment.show(fragmentTransaction, "inspect")
                    dismiss()
                    navController.navigate(
                            GalleryFragmentDirections.actionGalleryFragmentToInspectFragment())
//                    val fragmentTransaction = childFragmentManager.beginTransaction()
//                    val inspectFragment = InspectFragment()
//                    fragmentTransaction.replace(R.id.fragment_container, inspectFragment)
//                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
//                    fragmentTransaction.commit()
                }
                R.id.fab_process -> {
                    toast("Process action")
                    processImage()
                }
            }
            true
        }
    }

    @SuppressLint("RestrictedApi")
    private fun processImage() {
        fileUri?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                retrofitUploader.uploadFile(File(it.path!!), it)
            }
        }
        toast("Image Processed")
        dismiss()
    }
    private fun setCurrentItem(position: Int) {
        viewPager.setCurrentItem(position, false)
        currentImage = model.getGalleryImage(position) as GalleryImage
        selectedPosition = position
        fileUri = currentImage.imageUrl
    }
    // viewpager page change listener
    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                Log.d("GalleryFullscreen", "$position")
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
            val layoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            val view = layoutInflater.inflate(R.layout.gallery_image_fullscreen, container, false)
            val image = model.getGalleryImage(position)
            if (image != null) {
                view.ivFullscreenImage.tag = image.imageUrl
            }

            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            // load image
            if (image != null) {
                GlideApp.with(context!!)
                        .load(image.imageUrl)
                        .placeholder(circularProgressDrawable)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(view.ivFullscreenImage)
            }
            container.addView(view)
            return view
        }

        override fun getItemPosition(`object`: Any): Int {
            val imageView = `object` as ImageView
            val tag = imageView.tag

            var flag = false
            model.imageList.value?.forEach {
                if(it.imageUrl == tag){
                    flag = true
                    return@forEach
                }
            }
            return if (flag) super.getItemPosition(`object`) else POSITION_NONE
        }

        override fun getCount(): Int {
            return model.getImageListSize() ?: 0
        }
        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj as View
        }
        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }
    }

}