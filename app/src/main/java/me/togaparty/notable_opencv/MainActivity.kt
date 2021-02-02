package me.togaparty.notable_opencv

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var container: FragmentContainerView
    //private var _binding: MainActivityBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.fragment_container)

    }


    companion object {

        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext

            return if (MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                val externalDir = context.externalCacheDirs.firstOrNull()?.let {
                    File(it, "Notable_OPENCV").apply { mkdir() }}
                return if (externalDir != null && externalDir.exists())
                    externalDir else appContext.filesDir
            } else context.getCacheDir()
        }
    }
}