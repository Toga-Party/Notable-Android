package me.togaparty.notable_opencv.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import me.togaparty.notable_opencv.MainActivity
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.adapter.GalleryImage
import me.togaparty.notable_opencv.helper.GlideApp
import me.togaparty.notable_opencv.network.RetrofitWorker
import me.togaparty.notable_opencv.utils.FileWorker
import me.togaparty.notable_opencv.utils.ImageListProvider
import me.togaparty.notable_opencv.utils.showDialog
import me.togaparty.notable_opencv.utils.toast
import java.io.File


class PreviewImageFragment : Fragment() {

    private lateinit var fileName: String
    private var fileUri: Uri? = null
    private lateinit var container: RelativeLayout
    private lateinit var imageView: ImageView
    private lateinit var outputCacheDirectory: File
    private lateinit var galleryDirectory: File
    private lateinit var navController: NavController
    private lateinit var fileWorker: FileWorker
    private lateinit var retrofitWorker: RetrofitWorker
    private val model: ImageListProvider by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("requestKey") { _, bundle ->
            Log.d("PreviewDebug", "Bundle retrieved.")
            fileName = bundle.getString("photoPath").toString()
            outputCacheDirectory = MainActivity.getOutputCacheDirectory(requireContext())
            galleryDirectory = MainActivity.getAppSpecificAlbumStorageDir(requireContext())
            fileUri = Uri.fromFile(File(outputCacheDirectory, fileName))
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preview_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view as RelativeLayout
        navController = container.findNavController()
        imageView = container.findViewById(R.id.imageView)
        container.findViewById<ImageButton>(R.id.save_button_1).setOnClickListener { saveImage() }
        loadSpeedDials(container) //goes BBRRR
        fileWorker = FileWorker()
        retrofitWorker = RetrofitWorker()
        GlobalScope.launch {
            container.post{
                setImageView()
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("PreviewDebug", "On Activity result is called.")
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Log.d("PreviewDebug", "Image cropped successfully")
            fileUri = data?.let { UCrop.getOutput(it) }
            Log.d("PreviewDebug", "Received uri: $fileUri")
            setImageView()
        }
    }
    private fun loadSpeedDials(container: RelativeLayout) {
        val floatingActionButton = container.findViewById<SpeedDialView>(R.id.speedDial2)
        floatingActionButton.addActionItem(
                SpeedDialActionItem.Builder(R.id.crop, R.drawable.ic_crop)
                        .setLabel(getString(R.string.crop))
                        .setTheme(R.style.Theme_Notable_OPENCV)
                        .setLabelClickable(false)
                        .create()
        )
        floatingActionButton.addActionItem(
            SpeedDialActionItem.Builder(R.id.retake, R.drawable.ic_arrow_back_24px)
                .setLabel(getString(R.string.retake))
                .setTheme(R.style.Theme_Notable_OPENCV)
                .setLabelClickable(false)
                .create()
        )
        floatingActionButton.setOnActionSelectedListener { actionItem ->

            when(actionItem.id) {
                R.id.retake -> navController.navigate(PreviewImageFragmentDirections.actionPreviewImagePop())
                R.id.crop -> cropImage()
                else -> throw IllegalAccessError("this shouldn't happen in the first place")
            }
            true
        }
    }
    private fun cropImage() {
        Log.d("PreviewDebug", "Before cropping: $fileUri")
        fileUri?.let {
            UCrop.of(it, it)
                .withMaxResultSize(imageView.width, imageView.height)
                .start(requireContext(), this)
        }
    }
    @SuppressLint("RestrictedApi")
    private fun saveImage() {
        Log.d("Preview", "Processing Image")

        showDialog("Save image", "Do you want to save this image in the gallery?") {
            fileUri?.let {
                lifecycleScope.launch {
                    var image: GalleryImage? = null
                    val savingOperation = async(Dispatchers.IO) {
                        image = fileWorker.saveImage(
                                requireContext(),
                                "Notable",
                                fileName,
                                it,
                        )
                    }
                    savingOperation.await()
                    image?.let {
                        Log.d("Preview", "Adding to list")
                        model.addtoList(it)
                    }
                    Log.d("Preview", "Navigating to Gallery")
                    navController.navigate(PreviewImageFragmentDirections.actionPreviewImageToGalleryFragment())
                    toast("Image Saved")
                }

            }
        }
    }
    private fun setImageView() {
        Log.d("PreviewDebug", "SetImageView is called")
        val options = RequestOptions()
            .format(DecodeFormat.PREFER_RGB_565)
            .placeholder(ColorDrawable(Color.WHITE))
            .error(ColorDrawable(Color.CYAN))

        GlideApp.with(this)
            .setDefaultRequestOptions(options)
            .load(fileUri)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(object : CustomTarget<Drawable>(container.width, container.height) {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    imageView.setImageDrawable(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    companion object
}