package me.togaparty.notable_opencv.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.glide_image_fullscreen.view.*
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.adapter.GalleryImage
import me.togaparty.notable_opencv.helper.GlideApp
import me.togaparty.notable_opencv.helper.GlideZoomOutPageTransformer

class GalleryFullscreenFragment : DialogFragment() {

    private lateinit var imageList: ArrayList<GalleryImage>
    private lateinit var viewPager: ViewPager
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter
    private var selectedPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        /*setFragmentResultListener("requestKey")  { _ , bundle ->
            Log.d("FullscreenGallery", "Bundle Retrieved.")
            @Suppress("UNCHECKED_CAST")
            imageList = ArrayList(bundle.getSerializable("images") as ArrayList<GalleryImage>)
            selectedPosition = bundle.getInt("position")
        }*/
        @Suppress("UNCHECKED_CAST")
        imageList = ArrayList(arguments?.getSerializable("images") as ArrayList<GalleryImage>)
        selectedPosition = requireArguments().getInt("position")

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("GalleryFullscreenDebug", "Fullscreen called.")
        val view = inflater.inflate(R.layout.fragment_gallery_fullscreen, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        galleryPagerAdapter = GalleryPagerAdapter()
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())
        setCurrentItem(selectedPosition)
        return view
    }

    private fun setCurrentItem(position: Int) {
        viewPager.setCurrentItem(position, false)
    }
    // viewpager page change listener
    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                // set gallery title
                //tvGalleryTitle.text = imageList.get(position).title
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
            val view = layoutInflater.inflate(R.layout.glide_image_fullscreen, container, false)
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