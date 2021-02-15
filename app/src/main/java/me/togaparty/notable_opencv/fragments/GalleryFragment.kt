package me.togaparty.notable_opencv.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_gallery.*
import me.togaparty.notable_opencv.MainActivity
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.adapter.GalleryImage
import me.togaparty.notable_opencv.adapter.GalleryImageAdapter
import me.togaparty.notable_opencv.adapter.GalleryImageClickListener
import java.io.File

class GalleryFragment : Fragment(), GalleryImageClickListener {
    // Gallery Column Count
    private val spanCount = 2
    private val imageList = ArrayList<GalleryImage>()
    private lateinit var galleryAdapter: GalleryImageAdapter
    private lateinit var navController: NavController
    private lateinit var galleryDirectory: File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        galleryDirectory = MainActivity.getAppSpecificAlbumStorageDir(requireContext())
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!PermissionsFragment.allPermissionsGranted(requireContext())) {
            Log.d("GalleryDebug", "Called to navigate to PermissionsFragment")
            setFragmentResult("requestKey", bundleOf("actionDirection" to "toGallery"))
            navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToPermissionsFragment())
        }
        // init adapter
        galleryAdapter = GalleryImageAdapter(imageList)
        galleryAdapter.listener = this
        // init recyclerview
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        recyclerView.adapter = galleryAdapter
        // load images
        navController = this.findNavController()
        loadImages()
    }


    private fun loadImages() {


        val galleryFiles = galleryDirectory.listFiles()

        if (galleryFiles != null) {
            Log.d("GalleryFragment", "galleryFiles size: ${galleryFiles.size}")
            for (files in galleryFiles) {
                imageList.add(GalleryImage(Uri.fromFile(files).toString(), files.toString()))
            }
        }

        /*
        imageList.add(GalleryImage("https://i.ibb.co/gM5NNJX/butterfly.jpg", "Butterfly"))
        imageList.add(GalleryImage("https://i.ibb.co/10fFGkZ/car-race.jpg", "Car Racing"))
        imageList.add(GalleryImage("https://i.ibb.co/ygqHsHV/coffee-milk.jpg", "Coffee with Milk"))
        imageList.add(GalleryImage("https://post.medicalnewstoday.com/wp-content/uploads/sites/3/2020/02/322868_1100-1100x628.jpg", "Fox"))
        imageList.add(GalleryImage("https://i.ibb.co/L1m1NxP/girl.jpg", "Mountain Girl"))
        imageList.add(GalleryImage("https://i.ibb.co/wc9rSgw/desserts.jpg", "Desserts Table"))
        imageList.add(GalleryImage("https://i.ibb.co/wdrdpKC/kitten.jpg", "Kitten"))
        imageList.add(GalleryImage("https://i.ibb.co/dBCHzXQ/paris.jpg", "Paris Eiffel"))
        imageList.add(GalleryImage("https://i.ibb.co/JKB0KPk/pizza.jpg", "Pizza Time"))
        imageList.add(GalleryImage("https://i.ibb.co/VYYPZGk/salmon.jpg", "Salmon "))
        imageList.add(GalleryImage("https://i.ibb.co/JvWpzYC/sunset.jpg", "Sunset in Beach"))
        */
        galleryAdapter.notifyDataSetChanged()
    }
    override fun onClick(position: Int) {
        /*setFragmentResult("requestKey",
            bundleOf("images" to imageList))
        setFragmentResult("requestKey",
            bundleOf("position" to position))

        navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToGalleryFullscreenFragment())
        */
        val bundle = Bundle()
            bundle.putSerializable("images", imageList)
            bundle.putInt("position", position)
        val fragmentTransaction = childFragmentManager.beginTransaction()
        val galleryFragment = GalleryFullscreenFragment()
        galleryFragment.setArguments(bundle)
        galleryFragment.show(fragmentTransaction, "gallery")
    }
    companion object
}