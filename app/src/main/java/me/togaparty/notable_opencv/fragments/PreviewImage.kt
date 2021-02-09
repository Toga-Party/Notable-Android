package me.togaparty.notable_opencv.fragments

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
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import me.togaparty.notable_opencv.R
import java.io.File


class PreviewImage : Fragment() {

    private var filePath: String? = null
    private lateinit var container: FrameLayout
    private lateinit var imageView: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setFragmentResultListener("requestKey") { _, bundle ->
            Log.d("PreviewDebug", "Bundle retrieved.")
            filePath = bundle.getString("photoPath")
        }
        return inflater.inflate(R.layout.fragment_preview_image, container, false)
    }

    override fun onResume(){
        super.onResume()
        container = view as FrameLayout
        imageView = container.findViewById(R.id.imageView)
        filePath?.let { Log.d("PreviewDebug", it) }
        container.post{
            setImageView()
        }
    }

    private fun setImageView() {
        val imageFile = File(filePath!!)

        if(imageFile.exists()) {
            val options = RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .placeholder(R.drawable.ic_launcher_background)
                .error(ColorDrawable(Color.CYAN))

            Glide.with(this)
                .setDefaultRequestOptions(options)
                .load(Uri.fromFile(imageFile))
                .centerInside()
                .into(object : CustomTarget<Drawable>(container.width, container.height) {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        imageView.setImageDrawable(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
    }
    companion object
}