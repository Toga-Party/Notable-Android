package me.togaparty.notable_opencv.ui.fragments

//import kotlinx.android.synthetic.main.fragment_glossary.*
//import me.togaparty.notable_opencv.adapter.MainRecyclerAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import me.togaparty.notable_opencv.ui.adapter.PredictionsAdapter
import me.togaparty.notable_opencv.helper.GlideApp
import me.togaparty.notable_opencv.helper.GlideZoomOutPageTransformer
import me.togaparty.notable_opencv.ui.items.InspectPrediction
import me.togaparty.notable_opencv.data.ImageListProvider
import me.togaparty.notable_opencv.utils.toast

class InspectFragment : Fragment(), PredictionsAdapter.OnItemClickListener {
    //private lateinit var mainCategoryRecycler: RecyclerView
    //private var mainRecyclerAdapter: MainRecyclerAdapter? = null
    private lateinit var viewPager: ViewPager
    private var selectedPosition: Int = 0
    internal val model: ImageListProvider by activityViewModels()
    //lateinit var predictions: ArrayList<InspectPrediction>
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter


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
        //model = ViewModelProvider(this).get(ImageListProvider::class.java)
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
        viewPager = view.findViewById(R.id.viewPagerBanner)
        galleryPagerAdapter = GalleryPagerAdapter()
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())

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
            val image = model.getGalleryImage(position)
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
            model.getList().value?.forEach {
                if(it.imageUrl == tag){
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
    companion object
}