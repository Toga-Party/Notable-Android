package me.togaparty.notable_opencv.fragments

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.widget.Toast
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.togaparty.notable_opencv.MainActivity
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.helper.GlideApp
import java.io.File
import java.io.FileOutputStream


class PreviewImageFragment : Fragment() {

    private lateinit var fileName: String
    private var fileUri: Uri? = null
    private lateinit var container: ConstraintLayout
    private lateinit var imageView: ImageView
    private lateinit var outputCacheDirectory: File
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("requestKey") { _, bundle ->
            Log.d("PreviewDebug", "Bundle retrieved.")
            fileName = bundle.getString("photoPath").toString()
            outputCacheDirectory = (MainActivity.getOutputCacheDirectory(requireContext()))
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
            Log.d("PreviewDebug", "Receieved uri: $fileUri")
        }
    }

    private fun cropImage() {
        val destinationUri = Uri.fromFile(File(outputCacheDirectory, fileName))
        UCrop.of(Uri.fromFile(File(outputCacheDirectory, fileName)), destinationUri)
                //.withAspectRatio(16F, 9F)
                .withMaxResultSize(imageView.width, imageView.height)
                .start(requireContext(), this)
    }
    private fun processImage() {
        Log.d("Preview", "Processing Image")
        //Log.i("Preview", MainActivity.getAppSpecificAlbumStorageDir(requireContext()).toString())
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Do you want to save this image in the gallery?")
                .setTitle("Save Image")
                .setPositiveButton("Yes") { _, _ ->
                    val file = File(MainActivity.getAppSpecificAlbumStorageDir(requireContext()),fileName)
                    val bitmap = BitmapFactory.decodeFile(File(outputCacheDirectory, fileName).absolutePath)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
                    Toast.makeText(requireContext(), "Image saved", Toast.LENGTH_SHORT).show()
                    MainActivity.deleteCache(requireContext())
                }
                .setNegativeButton("No") {_, _ ->
                    MainActivity.deleteCache(requireContext())
                }
                .create()
                .show()


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