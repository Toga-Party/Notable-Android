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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.gallery_image_fullscreen.view.*
import kotlinx.android.synthetic.main.item_gallery_image.view.*
import kotlinx.coroutines.*
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.data.ImageListProvider
import me.togaparty.notable_android.helper.GlideApp
import me.togaparty.notable_android.helper.GlideZoomOutPageTransformer
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import me.togaparty.notable_android.utils.toast
import java.io.IOException


class GalleryFullscreenFragment : DialogFragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter

    private lateinit var currentImage: GalleryImage
    private lateinit var navController: NavController

    private var fileUri: Uri? = null
    private var selectedPosition: Int = 0

    internal val model: ImageListProvider by activityViewModels()


    @SuppressLint("LogConditional")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
                R.layout.fragment_gallery_fullscreen,
                container,
                false
        )

        navController = this.findNavController()
        //model = ViewModelProvider(this).get(ImageListProvider::class.java)
        galleryPagerAdapter = GalleryPagerAdapter()
        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())

        setCurrentItem(requireArguments().getInt("position"))
        generateFloatingActionButton(view)

        model.getList().observe(viewLifecycleOwner, {
            Log.d(TAG, "Fullscreen: Something changed")
            viewPager.adapter?.notifyDataSetChanged()
            if(model.getImageListSize() == 0) dismiss() else setCurrentItem(selectedPosition)
            editFloatingActionButton()
        })


        return view
    }
    private fun editFloatingActionButton() {
        val floatingActionButton = view?.findViewById<SpeedDialView>(R.id.speedDial)
        if (currentImage.processed == true) {
            floatingActionButton?.removeActionItem(1)
            floatingActionButton?.addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_inspect, R.drawable.search_icon)
                    .setLabel(getString(R.string.inspect))
                    .setTheme(R.style.Theme_Notable_OPENCV)
                    .setLabelClickable(false)
                    .create()
            )
        } else {
            floatingActionButton?.removeActionItem(1)
            floatingActionButton?.addActionItem(
                SpeedDialActionItem.Builder(R.id.fab_process, R.drawable.sync)
                    .setLabel(getString(R.string.process_music))
                    .setTheme(R.style.Theme_Notable_OPENCV)
                    .setLabelClickable(false)
                    .create()
            )
        }
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
            floatingActionButton.addActionItem( when(currentImage.processed == true) {

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
                        toast("Delete action")
                        Log.d(TAG, "Full screen: delete launched")
                        GlobalScope.launch(Dispatchers.Main) {
                            Log.d(TAG, "Full screen: deleting")

                            if(model.getImageListSize() != 0){
                                model.deleteGalleryImage(selectedPosition, currentImage.imageUrl)
                            }
                        }
                        Log.d(TAG, "Full screen: deleted")
                    }
                    R.id.fab_inspect -> {
                        toast("Inspect action")
                        dismiss()
                        navController.navigate(
                            GalleryFragmentDirections.actionGalleryFragmentToInspectFragment())
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
        //var image: GalleryImage? = null
        GlobalScope.launch(Dispatchers.Default + NonCancellable) {
            try {
            model.uploadImage(currentImage, selectedPosition)
            } catch (ex: IOException) {
                toast("Upload failed")
            }
        }
        dismiss()
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
            view.ivFullscreenImage.tag = image.imageUrl

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
                    .into(view.ivFullscreenImage)

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

}