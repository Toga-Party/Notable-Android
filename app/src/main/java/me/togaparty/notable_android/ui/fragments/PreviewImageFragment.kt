package me.togaparty.notable_android.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.yalantis.ucrop.UCrop
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.*
import me.togaparty.notable_android.MainActivity
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.data.ImageListProvider
import me.togaparty.notable_android.databinding.FragmentPreviewImageBinding
import me.togaparty.notable_android.helper.GlideApp
import me.togaparty.notable_android.utils.showDialog
import me.togaparty.notable_android.utils.toast
import java.io.File


class PreviewImageFragment : Fragment(R.layout.fragment_preview_image) {

    private val binding by viewBinding(FragmentPreviewImageBinding::bind)

    private lateinit var fileName: String
    private var fileUri: Uri? = null
    private lateinit var outputCacheDirectory: File
    private lateinit var navController: NavController

    private lateinit var model: ImageListProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("requestKey") { _, bundle ->
            fileName = bundle.getString("photoPath").toString()
            outputCacheDirectory = MainActivity.getOutputCacheDirectory(requireContext())
            fileUri = Uri.fromFile(File(outputCacheDirectory, fileName))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = this.findNavController()
        binding.saveButton1.setOnClickListener{saveImage()}
        loadSpeedDials()
        model = ViewModelProvider(requireActivity()).get(ImageListProvider::class.java)
        lifecycleScope.launchWhenCreated {
            binding.root.post{
                setImageView()
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            fileUri = data?.let { UCrop.getOutput(it) }
            setImageView()
        }
    }
    private fun loadSpeedDials() {

        binding.speedDial2.addActionItem(
                SpeedDialActionItem.Builder(R.id.crop, R.drawable.ic_crop)
                        .setLabel(getString(R.string.crop))
                        .setTheme(R.style.Theme_Notable_OPENCV)
                        .setLabelClickable(false)
                        .create()
        )

        binding.speedDial2.addActionItem(
            SpeedDialActionItem.Builder(R.id.retake, R.drawable.ic_arrow_back_24px)
                .setLabel(getString(R.string.retake))
                .setTheme(R.style.Theme_Notable_OPENCV)
                .setLabelClickable(false)
                .create()
        )
        binding.speedDial2.setOnActionSelectedListener { actionItem ->

            when(actionItem.id) {
                R.id.retake -> navController.navigate(PreviewImageFragmentDirections.actionPreviewImagePop())
                R.id.crop -> cropImage()
                else -> throw IllegalAccessError("this shouldn't happen in the first place")
            }
            true
        }
    }
    private fun cropImage() {
        fileUri?.let {
            UCrop.of(it, it)
                .withMaxResultSize(binding.imageView.width, binding.imageView.height)
                .start(requireContext(), this)
        }
    }

    private fun saveImage() {
        Log.d("Preview", "Processing Image")
        showDialog("Save image", "Do you want to save this image in the gallery?") {
            val loadingFragment = LoadingFragment.show(childFragmentManager)

            fileUri?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val savingOperation = async(Dispatchers.IO) {
                        model.saveImageToStorage(fileName, it)
                        Thread.sleep(1500)
                    }
                    savingOperation.await()
                    withContext(Dispatchers.Main) {
                        loadingFragment.dismiss()
                        navController.navigate(PreviewImageFragmentDirections.actionPreviewImageToGalleryFragment())
                        toast("Image Saved")
                    }
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
            .into(object : CustomTarget<Drawable>(binding.root.width, binding.root.height) {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    binding.imageView.setImageDrawable(resource)
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    companion object
}