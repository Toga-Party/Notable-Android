package me.togaparty.notable_opencv.fragments

//import kotlinx.android.synthetic.main.fragment_glossary.*
//import me.togaparty.notable_opencv.adapter.MainRecyclerAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
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
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.adapter.GalleryImage
import me.togaparty.notable_opencv.adapter.PredictionsAdapter
import me.togaparty.notable_opencv.helper.GlideApp
import me.togaparty.notable_opencv.helper.GlideZoomOutPageTransformer
import me.togaparty.notable_opencv.model.Inspect_Prediction
import me.togaparty.notable_opencv.utils.FileWorker

class InspectFragment : Fragment() {
    //private lateinit var mainCategoryRecycler: RecyclerView
    //private var mainRecyclerAdapter: MainRecyclerAdapter? = null
    private lateinit var imageList: MutableList<*>
    private lateinit var viewPager: ViewPager
    private var selectedPosition: Int = 0
    private lateinit var fileWorker: FileWorker
    lateinit var predictions: ArrayList<Inspect_Prediction>
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileWorker = FileWorker()
        //imageList = ArrayList(arguments?.getSerializable("images") as ArrayList<*>)
        imageList = fileWorker.loadImages(requireContext())

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
        val inspect_recycler = view.findViewById(R.id.recycler_predictions) as RecyclerView
        // Initialize predictions
        val rows = Inspect_Prediction.createPredictionList(12)
        // Create adapter passing in the sample user data
        val adapter = PredictionsAdapter(rows)
        // Attach the adapter to the recyclerview to populate items
        inspect_recycler.adapter = adapter
        // Set layout manager to position the items
        inspect_recycler.layoutManager = LinearLayoutManager(requireContext())
        viewPager = view.findViewById(R.id.viewPagerBanner)
        galleryPagerAdapter = GalleryPagerAdapter()
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())

        return view
    }
    private fun setCurrentItem(position: Int) {
        viewPager.setCurrentItem(position, false)
        selectedPosition = position
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

            val view = layoutInflater.inflate(R.layout.fragment_inspect_image, container, false)
            val image = imageList[position] as GalleryImage
            view.inspectImage.tag = image.imageUrl

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
                    .into(view.inspectImage)
            container.addView(view)
            return view
        }

        override fun getItemPosition(`object`: Any): Int {
            val imageView = `object` as ImageView
            val tag = imageView.tag

            var flag = false
            imageList.forEach {
                if((it as GalleryImage).imageUrl == tag){
                    flag = true
                    return@forEach
                }
            }
            return if (flag) super.getItemPosition(`object`) else POSITION_NONE
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
    companion object
}