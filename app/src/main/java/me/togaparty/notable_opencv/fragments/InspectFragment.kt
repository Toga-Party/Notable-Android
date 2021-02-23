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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_inspect.*
import kotlinx.android.synthetic.main.fragment_inspect.view.*
import kotlinx.android.synthetic.main.fragment_inspect_image.view.*
import kotlinx.android.synthetic.main.gallery_image_fullscreen.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.adapter.GalleryImage
import me.togaparty.notable_opencv.helper.GlideApp
import me.togaparty.notable_opencv.helper.GlideZoomOutPageTransformer
import me.togaparty.notable_opencv.utils.FileWorkerViewModel

class InspectFragment : Fragment() {
    //private lateinit var mainCategoryRecycler: RecyclerView
    //private var mainRecyclerAdapter: MainRecyclerAdapter? = null
    private lateinit var imageList: MutableList<*>
    private lateinit var viewPager: ViewPager
    private var selectedPosition: Int = 0
    private lateinit var fileWorkerViewModel: FileWorkerViewModel
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileWorkerViewModel = FileWorkerViewModel()
        //imageList = ArrayList(arguments?.getSerializable("images") as ArrayList<*>)
        imageList = fileWorkerViewModel.loadImages(requireContext())
    }

//    private fun setMainCategoryRecycler(allCategory: List<AllCategory>){
//        //mainCategoryRecycler = findViewById(R.id.main_recycler)
//        val layoutManager:RecyclerView.LayoutManager = LinearLayoutManager(this.context)
//        main_recycler!!.layoutManager = layoutManager
//        mainRecyclerAdapter = this.context?.let { MainRecyclerAdapter(it,allCategory) }
//        main_recycler!!.adapter = mainRecyclerAdapter
//    }

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
//        GlobalScope.launch(Dispatchers.Main) {
//            imageList.addAll(fileWorkerViewModel.loadImages(requireContext()))
////            galleryAdapter.notifyDataSetChanged()
//        }

//        viewPager = view.findViewById(R.id.viewPagerBanner)
//        viewPager.adapter = galleryPagerAdapter
//        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
//        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())

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