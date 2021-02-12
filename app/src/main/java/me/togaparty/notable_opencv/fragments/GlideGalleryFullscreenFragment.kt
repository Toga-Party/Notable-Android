package me.togaparty.notable_opencv.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.glide_image_fullscreen.view.*
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.adapter.GlideImage
import me.togaparty.notable_opencv.helper.GlideApp
import me.togaparty.notable_opencv.helper.GlideZoomOutPageTransformer
import java.io.File

class GlideGalleryFullscreenFragment : DialogFragment() {
    private var imageList = ArrayList<GlideImage>()
    private var selectedPosition: Int = 0
    lateinit var tvGalleryTitle: TextView
    lateinit var viewPager: ViewPager
    lateinit var galleryPagerAdapter: GalleryPagerAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.glide_fragment_gallery_fullscreen, container, false)
        viewPager = view.findViewById(R.id.viewPager)
//        tvGalleryTitle = view.findViewById(R.id.tvGalleryTitle)
        galleryPagerAdapter = GalleryPagerAdapter()
        imageList = arguments?.getSerializable("images") as ArrayList<GlideImage>
        selectedPosition = requireArguments().getInt("position")
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())
        setCurrentItem(selectedPosition)
        return view
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }
    private fun setCurrentItem(position: Int) {
        viewPager.setCurrentItem(position, false)
    }
    // viewpager page change listener
    internal var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                // set gallery title
//                tvGalleryTitle.text = imageList.get(position).title
            }
            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
            }
            override fun onPageScrollStateChanged(arg0: Int) {
            }
        }
    // gallery adapter
    inner class GalleryPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.glide_image_fullscreen, container, false)
            val image = imageList.get(position)

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