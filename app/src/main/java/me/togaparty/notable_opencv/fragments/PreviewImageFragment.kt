package me.togaparty.notable_opencv.fragments

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
import me.togaparty.notable_opencv.MainActivity
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.helper.GlideApp
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class PreviewImageFragment : Fragment() {

    private var fileName: String? = null
    private var fileUri: Uri? = null
    private lateinit var container: ConstraintLayout
    private lateinit var imageView: ImageView
    private lateinit var outputDirectory: File
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setFragmentResultListener("requestKey") { _, bundle ->
            Log.d("PreviewDebug", "Bundle retrieved.")
            fileName = bundle.getString("photoPath")
            outputDirectory = (MainActivity.getOutputDirectory(requireContext()))
            fileUri = Uri.fromFile(File(outputDirectory, fileName!!))
        }
        return inflater.inflate(R.layout.fragment_preview_image, container, false)
    }

    override fun onResume(){
        super.onResume()
        container = view as ConstraintLayout
        navController = container.findNavController()
        imageView = container.findViewById(R.id.imageView)
        container.findViewById<Button>(R.id.retake).setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_previewImage_pop, null))
        container.findViewById<Button>(R.id.crop).setOnClickListener {cropImage()}
        container.findViewById<Button>(R.id.process).setOnClickListener {processImage()}
        container.post{
            setImageView()
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("PreviewDebug", "On Activity result is called.")
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Log.d("PreviewDebug", "Image cropped successfully")
            fileUri = data?.let { UCrop.getOutput(data) }!!
            Log.d("PreviewDebug", "Receieved uri: $fileUri")
        }
    }

    private fun cropImage() {
        val photoName = SimpleDateFormat(FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + PHOTO_EXTENSION
        val destinationUri = Uri.fromFile(File(outputDirectory, photoName))
        UCrop.of(Uri.fromFile(File(outputDirectory, fileName!!)), destinationUri)
                //.withAspectRatio(16F, 9F)
                .withMaxResultSize(imageView.width, imageView.height)
                .start(requireContext(), this)
    }
    private fun processImage() {
        Log.d("Preview", "Processing Image")


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

    companion object {
        private const val FILENAME_FORMAT = "EEE_dd_MM_yyyy_HHmmss"
        private const val PHOTO_EXTENSION = ".jpg"
    }
}