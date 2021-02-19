package me.togaparty.notable_opencv.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
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
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.togaparty.notable_opencv.MainActivity
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.helper.GlideApp
import me.togaparty.notable_opencv.network.RetrofitUploader
import me.togaparty.notable_opencv.utils.FileSaveViewModel
import me.togaparty.notable_opencv.utils.toast
import java.io.File


class PreviewImageFragment : Fragment() {

    private lateinit var fileName: String
    private var fileUri: Uri? = null
    private lateinit var container: ConstraintLayout
    private lateinit var imageView: ImageView
    private lateinit var outputCacheDirectory: File
    private lateinit var galleryDirectory: File
    private lateinit var navController: NavController
    private lateinit var fileSaveViewModel: FileSaveViewModel
    private lateinit var retrofitUploader: RetrofitUploader

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
        container = view as ConstraintLayout
        navController = container.findNavController()

        imageView = container.findViewById(R.id.imageView)
        container.findViewById<Button>(R.id.retake).setOnClickListener(
                Navigation.createNavigateOnClickListener(R.id.action_previewImage_pop, null))
        container.findViewById<Button>(R.id.crop).setOnClickListener {cropImage()}
        container.findViewById<Button>(R.id.process).setOnClickListener {processImage()}
        fileSaveViewModel = FileSaveViewModel()
        retrofitUploader = RetrofitUploader()
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
            fileUri = data?.let { UCrop.getOutput(data) }!!
            Log.d("PreviewDebug", "Received uri: $fileUri")
        }
    }

    private fun cropImage() {
        fileUri?.let {
            UCrop.of(it, it)
                .withMaxResultSize(imageView.width, imageView.height)
                .start(requireContext(), this)
        }
    }
    @SuppressLint("RestrictedApi")
    private fun processImage() {
        Log.d("Preview", "Processing Image")

            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Do you want to save this image in the gallery?")
                .setTitle("Save Image")
                .setPositiveButton("Yes") { _, _ ->

                    fileUri?.let {
                        GlobalScope.launch(Dispatchers.IO) {
                        fileSaveViewModel.saveImage(
                            requireContext(),
                            "Notable",
                            fileName,
                            it
                        )}
                    }
                    toast("Image Saved")
                }
                .setNegativeButton("No") { _, _ ->
                }
                .create()
                .show()
            fileUri?.let {
                Log.d("PreviewDebug", it.toString())
                GlobalScope.launch(Dispatchers.IO) {
                    retrofitUploader.uploadFile(File(it.path!!), it)
                }
            }

    }
    private fun setImageView() {
        Log.d("PreviewDebug", "FileURI is : $fileUri")
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