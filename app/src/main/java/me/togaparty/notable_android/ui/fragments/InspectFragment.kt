package me.togaparty.notable_android.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_inspect_image.view.*
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.data.ImageListProvider
import me.togaparty.notable_android.data.InspectPrediction
import me.togaparty.notable_android.helper.GlideApp
import me.togaparty.notable_android.helper.GlideZoomOutPageTransformer
import me.togaparty.notable_android.ui.adapter.PredictionsAdapter
import me.togaparty.notable_android.utils.toast

//TODO: When you are going to finish the InspectFragment implementation
// Delete commented code that's marked with <*> and uncomment the code marked with <?>

class InspectFragment : Fragment(), PredictionsAdapter.OnItemClickListener {

    private lateinit var viewPager: ViewPager
    private var selectedPosition: Int = 0
    private var finalPosition: Int = 0
    private lateinit var model: ImageListProvider //by activityViewModels()
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter
    private var currentPosition : Int? = null
    private lateinit var currentImage: GalleryImage

    private var wavFiles: Map<String,Uri>? = null
    private var textFiles: Map<String,Uri>? = null
    private var imageFiles: ArrayList<Uri>? = null
    private var imageMap: Map<String,Uri>? = null

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
        model = ViewModelProvider(requireActivity()).get(ImageListProvider::class.java)
        currentPosition?.let{
            currentImage = model.getGalleryImage(it)
            wavFiles = currentImage.wavFiles
            textFiles = currentImage.textFiles
            imageFiles = ArrayList(currentImage.imageFiles.values)
            imageMap = currentImage.imageFiles
            imageFiles?.let {
                finalPosition = it.size
            }
        }
        // Lookup the recyclerview in activity layout
        val inspectRecycler = view.findViewById(R.id.recycler_predictions) as RecyclerView
        // Initialize predictions
        val rows = InspectPrediction.createPredictionList(textFiles)
        // Create adapter passing in the sample data
        val adapter = PredictionsAdapter(rows,this)
        // Attach the adapter to the recyclerview to populate items
        inspectRecycler.adapter = adapter
        // Set layout manager to position the items
        inspectRecycler.layoutManager = LinearLayoutManager(requireContext())



        viewPager = view.findViewById(R.id.viewPagerBanner)
        galleryPagerAdapter = GalleryPagerAdapter(finalPosition)
        galleryPagerAdapter.notifyDataSetChanged()
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())

        return view
    }
    override fun onItemClick(position: Int, view: TextView) {
        //Button Click event exposes aligned TextView control
        toast(view.text.toString() +" $position clicked")
    }
    internal fun setCurrentItem(position: Int) {
        viewPager.setCurrentItem(position, false)
        selectedPosition = position
    }

    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            @SuppressLint("LogConditional")
            override fun onPageSelected(position: Int) {
                Log.d("Inspect", "Selected: $position Max: $finalPosition")
                setCurrentItem(position)
            }
            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
            }
            override fun onPageScrollStateChanged(arg0: Int) {
            }
        }

    inner class GalleryPagerAdapter(itemCount: Int) : PagerAdapter() {
        private var count : Int = itemCount
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            Log.d("Inspect", "Pager Adapter: $position")
            val view = layoutInflater.inflate(R.layout.fragment_inspect_image, container, false)

            val fileurl = imageMap?.get("slice$position.png")
            view.inspectImage.tag = fileurl
            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            // load image
            GlideApp.with(context!!)
                .load(fileurl)
                .placeholder(circularProgressDrawable)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view.inspectImage)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return count
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