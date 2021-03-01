package me.togaparty.notable_android.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_inspect.*
import kotlinx.android.synthetic.main.fragment_inspect.view.*
import kotlinx.android.synthetic.main.fragment_inspect_image.view.*
import kotlinx.android.synthetic.main.gallery_image_fullscreen.view.*
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.ui.adapter.PredictionsAdapter
import me.togaparty.notable_android.helper.GlideApp
import me.togaparty.notable_android.helper.GlideZoomOutPageTransformer
import me.togaparty.notable_android.data.InspectPrediction
import me.togaparty.notable_android.data.ImageListProvider
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import me.togaparty.notable_android.utils.toast
//TODO: When you are going to finish the InspectFragment implementation
// Delete commented code that's marked with <*> and uncomment the code marked with <?>

class InspectFragment : Fragment(), PredictionsAdapter.OnItemClickListener {

    private lateinit var viewPager: ViewPager
    private var selectedPosition: Int = 0
    internal lateinit var model: ImageListProvider //by activityViewModels()
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter
    private var currentPosition : Int? = null
    internal lateinit var currentImage: GalleryImage

    private var wavFiles: Map<String,Uri>? = null
    private var textFiles: Map<String,Uri>? = null
    private var imageFiles: ArrayList<Uri>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentPosition = requireArguments().getInt("position")
        }
    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
                R.layout.fragment_inspect,
                container,
                false
        )

        // Lookup the recyclerview in activity layout
        val inspectRecycler = view.findViewById(R.id.recycler_predictions) as RecyclerView
        // Initialize predictions
        val rows = InspectPrediction.createPredictionList(12)
        // Create adapter passing in the sample data
        val adapter = PredictionsAdapter(rows,this)
        // Attach the adapter to the recyclerview to populate items
        inspectRecycler.adapter = adapter
        // Set layout manager to position the items
        inspectRecycler.layoutManager = LinearLayoutManager(requireContext())
        model = ViewModelProvider(requireActivity()).get(ImageListProvider::class.java)
        viewPager = view.findViewById(R.id.viewPagerBanner)
        galleryPagerAdapter = GalleryPagerAdapter()
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())

        //<?>
        //currentPosition?.let{
            //currentImage = model.getGalleryImage(it)
            //wavFiles = currentImage.wavFiles
            //textFiles = currentImage.textFiles

            //imageFiles = currentImage.imageFiles.flatMap { (_, values) -> arrayListOf(values)}
        //}
        return view
    }
    override fun onItemClick(position: Int, view: TextView) {
        //Button Click event exposes aligned TextView control
        toast(view.text.toString() +" $position clicked")
        //Log.d("Click", view.text.toString() + " $position clicked")
    }
    internal fun setCurrentItem(position: Int) {
        viewPager.setCurrentItem(position, false)
        selectedPosition = position
    }

    // viewpager page change listener
    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            @SuppressLint("LogConditional")
            override fun onPageSelected(position: Int) {
                Log.d(TAG, "Inspect Fragment: $position")
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

            val view = layoutInflater.inflate(R.layout.fragment_inspect_image, container, false)

            val image = model.getGalleryImage(position)//TODO:<*>
            if(!::currentImage.isInitialized) {
                throw ExceptionInInitializerError("Current Image is not initialized")
            }

            view.inspectImage.tag = image.imageUrl
            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            // load image
            GlideApp.with(context!!)

                    .load(image.imageUrl)//TODO:<*>
                    //TODO:<?>
                    //.load(imageFiles.get(position))
                    .placeholder(circularProgressDrawable)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view.inspectImage)
            container.addView(view)
            return view
        }
// Only needed when updating the adapter especially when deleting files.
//        override fun getItemPosition(`object`: Any): Int {
//            val imageView = `object` as ImageView
//            val tag = imageView.tag
//
//            var flag = false
//            model.getList().value?.forEach {
//                if(it.imageUrl == tag){
//                    flag = true
//                    return@forEach
//                }
//            }
//            return if (flag) super.getItemPosition(`object`) else POSITION_NONE
//        }

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
    companion object
}