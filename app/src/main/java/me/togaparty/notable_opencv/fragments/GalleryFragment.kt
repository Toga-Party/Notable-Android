package me.togaparty.notable_opencv.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.adapter.GalleryImage
import me.togaparty.notable_opencv.adapter.GalleryImageAdapter
import me.togaparty.notable_opencv.adapter.GalleryImageClickListener
import me.togaparty.notable_opencv.utils.ALL_REQUIRED_PERMISSIONS
import me.togaparty.notable_opencv.utils.FILE_REQUIRED_PERMISSIONS
import me.togaparty.notable_opencv.utils.FileWorkerViewModel
import me.togaparty.notable_opencv.utils.permissionsGranted

class GalleryFragment : Fragment(),
        GalleryImageClickListener {
    // Gallery Column Count
    private val spanCount = 2
    private val imageList = ArrayList<GalleryImage>()
    private lateinit var galleryAdapter: GalleryImageAdapter
    private lateinit var navController: NavController
    private lateinit var fileWorkerViewModel: FileWorkerViewModel
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = this.findNavController()
        if(!permissionsGranted(requireContext(), FILE_REQUIRED_PERMISSIONS)) {
            navController.navigate(GalleryFragmentDirections.actionGalleryFragmentToDashboardFragment())
        }
        // init adapter
        galleryAdapter = GalleryImageAdapter(imageList)
        galleryAdapter.listener = this
        fileWorkerViewModel = FileWorkerViewModel()
        // init recyclerview
        recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)
        recyclerView.adapter = galleryAdapter
        // load images

        loadGallery()
    }


    private fun loadGallery() {
        val context = requireContext()
        GlobalScope.launch(Dispatchers.Main) {
            imageList.addAll(fileWorkerViewModel.loadImages(context))
            galleryAdapter.notifyDataSetChanged()
        }
    }
    override fun onClick(position: Int) {
        val bundle = Bundle()
            bundle.putSerializable("images", imageList)
            bundle.putInt("position", position)
        val fragmentTransaction = childFragmentManager.beginTransaction()
        val galleryFragment = GalleryFullscreenFragment()
        val iF = InspectFragment()
        galleryFragment.setArguments(bundle)
        galleryFragment.show(fragmentTransaction, "gallery")
    }
    companion object


}