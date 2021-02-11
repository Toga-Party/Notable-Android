package me.togaparty.notable_opencv

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_notable_glide.*
import me.togaparty.notable_opencv.adapter.GlideGalleryImageAdapter
import me.togaparty.notable_opencv.adapter.GlideGalleryImageClickListener
import me.togaparty.notable_opencv.adapter.GlideImage
import me.togaparty.notable_opencv.fragments.GlideGalleryFullscreenFragment

class NotableGlideActivity : AppCompatActivity(), GlideGalleryImageClickListener {
    // gallery column count
    private val SPAN_COUNT = 2
    private val imageList = ArrayList<GlideImage>()
    lateinit var galleryAdapter: GlideGalleryImageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notable_glide)
        // init adapter
        galleryAdapter = GlideGalleryImageAdapter(imageList)
        galleryAdapter.listener = this
        // init recyclerview
        recyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        recyclerView.adapter = galleryAdapter
        // load images
        loadImages()
    }
    private fun loadImages() {
        imageList.add(GlideImage("https://i.ibb.co/gM5NNJX/butterfly.jpg", "Butterfly"))
        imageList.add(GlideImage("https://i.ibb.co/10fFGkZ/car-race.jpg", "Car Racing"))
        imageList.add(GlideImage("https://i.ibb.co/ygqHsHV/coffee-milk.jpg", "Coffee with Milk"))
        imageList.add(GlideImage("https://post.medicalnewstoday.com/wp-content/uploads/sites/3/2020/02/322868_1100-1100x628.jpg", "Fox"))
        imageList.add(GlideImage("https://i.ibb.co/L1m1NxP/girl.jpg", "Mountain Girl"))
        imageList.add(GlideImage("https://i.ibb.co/wc9rSgw/desserts.jpg", "Desserts Table"))
        imageList.add(GlideImage("https://i.ibb.co/wdrdpKC/kitten.jpg", "Kitten"))
        imageList.add(GlideImage("https://i.ibb.co/dBCHzXQ/paris.jpg", "Paris Eiffel"))
        imageList.add(GlideImage("https://i.ibb.co/JKB0KPk/pizza.jpg", "Pizza Time"))
        imageList.add(GlideImage("https://i.ibb.co/VYYPZGk/salmon.jpg", "Salmon "))
        imageList.add(GlideImage("https://i.ibb.co/JvWpzYC/sunset.jpg", "Sunset in Beach"))
        galleryAdapter.notifyDataSetChanged()
    }
    override fun onClick(position: Int) {
        // handle click of image
        val bundle = Bundle()
        bundle.putSerializable("images", imageList)
        bundle.putInt("position", position)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val galleryFragment = GlideGalleryFullscreenFragment()
        galleryFragment.setArguments(bundle)
        galleryFragment.show(fragmentTransaction, "gallery")
    }
}