package me.togaparty.notable_opencv.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.leinardi.android.speeddial.SpeedDialView.OnActionSelectedListener
import me.togaparty.notable_opencv.R
import kotlinx.android.synthetic.main.gallery_image_fullscreen.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.togaparty.notable_opencv.adapter.GalleryImage
import me.togaparty.notable_opencv.helper.GlideApp
import me.togaparty.notable_opencv.helper.GlideZoomOutPageTransformer
import me.togaparty.notable_opencv.network.RetrofitUploader
import me.togaparty.notable_opencv.utils.FileWorkerViewModel
import me.togaparty.notable_opencv.utils.toast
import java.io.File


class GalleryFullscreenFragment : DialogFragment() {

    private lateinit var imageList: ArrayList<GalleryImage>
    private lateinit var viewPager: ViewPager
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter
    private lateinit var fileWorkerViewModel: FileWorkerViewModel
    private lateinit var retrofitUploader: RetrofitUploader
    private lateinit var currentImage: GalleryImage
    private var fileUri: Uri? = null
    private var selectedPosition: Int = 0
    private var processed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        @Suppress("UNCHECKED_CAST")
        fileWorkerViewModel = FileWorkerViewModel()
        imageList = ArrayList(arguments?.getSerializable("images") as ArrayList<GalleryImage>)

        selectedPosition = requireArguments().getInt("position")
        currentImage = imageList[selectedPosition]
        fileUri = currentImage.imageUrl
        //Detect rar directory, exists = true
        processed = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("GalleryFullscreenDebug", "Fullscreen called.")
        val view = inflater.inflate(R.layout.fragment_gallery_fullscreen, container, false)
        val floatingActionButton = view.findViewById<SpeedDialView>(R.id.speedDial)
        floatingActionButton.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_delete, R.drawable.ic_delete_black)
                .setLabel(getString(R.string.delete))
                .setTheme(R.style.Theme_Notable_OPENCV)
                .setLabelClickable(false)
                .create()
        )
        if(processed){
            floatingActionButton.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_inspect, R.drawable.ic_show_black)
                .setLabel(getString(R.string.inspect))
                .setTheme(R.style.Theme_Notable_OPENCV)
                .setLabelClickable(false)
                .create()
        )}
        else{
            floatingActionButton.addActionItem(
            SpeedDialActionItem.Builder(R.id.fab_process, R.drawable.sync)
                .setLabel(getString(R.string.process_music))
                .setTheme(R.style.Theme_Notable_OPENCV)
                .setLabelClickable(false)
                .create()
        )}
        floatingActionButton.setOnActionSelectedListener(OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.fab_delete -> {
                    toast("Delete action")
                    Log.d("delete", "delete launched")
                    Log.d("delete", currentImage.imageUrl.toString() + " " + currentImage.name)
                    GlobalScope.launch(Dispatchers.Main) {
                        Log.d("delete", "deleting")
                        fileWorkerViewModel.deleteImage(currentImage.imageUrl,requireContext())
                    }
                    Log.d("delete", "done dleteing")
                    //getActivity()?.onBackPressed();
                }
                R.id.fab_inspect -> {
                    toast("Inspect action")
                    //val bundle = Bundle()
                    //bundle.putParcelable("imageUri", currentImage.imageUrl);
                    //inspectFragment.setArguments(bundle)
                    //inspectFragment.show(fragmentTransaction, "inspect")
                    val fragmentTransaction = childFragmentManager.beginTransaction()
                    val inspectFragment = InspectFragment()
                    fragmentTransaction.replace(R.id.fragment_container, inspectFragment)
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    fragmentTransaction.commit()

                }
                R.id.fab_process -> {
                    toast("Process action")
                }
            }
            true
        })
        retrofitUploader = RetrofitUploader()
        viewPager = view.findViewById(R.id.viewPager)
        galleryPagerAdapter = GalleryPagerAdapter()
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())
        setCurrentItem(selectedPosition)
        return view
    }
    @SuppressLint("RestrictedApi")
    private fun processImage() {
        Log.d("Preview", "Processing Image")

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Do you want to save this image in the gallery?")
            .setTitle("Save Image")
            .setPositiveButton("Yes") { _, _ ->

                fileUri?.let {
                    GlobalScope.launch(Dispatchers.IO) {
                        fileWorkerViewModel.saveImage(
                            requireContext(),
                            "Notable",
                            currentImage.name,
                            it
                        )}
                }
                toast("Image Saved")
            }
            .setNegativeButton("No") { _, _ ->
            }
            .create()
            .show()
        fileUri?.let {
            Log.d("PreviewDebug", it.toString())
            GlobalScope.launch(Dispatchers.IO) {
                retrofitUploader.uploadFile(File(it.path!!), it)
            }
        }

    }
    private fun setCurrentItem(position: Int) {
        viewPager.setCurrentItem(position, false)
    }
    // viewpager page change listener
    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                Log.d("GalleryFullscreen", "$position")
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
            val image = imageList[position]

            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            // load image
            GlideApp.with(context!!)
                .load(image.imageUrl)
                .placeholder(circularProgressDrawable)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view.ivFullscreenImage)
            container.addView(view)
            return view
        }
        override fun getCount(): Int {
            return imageList.size
        }
        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj as View
        }
        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }
    }
}