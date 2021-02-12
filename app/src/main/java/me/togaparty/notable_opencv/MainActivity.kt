package me.togaparty.notable_opencv

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import android.os.Environment.MEDIA_MOUNTED_READ_ONLY
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var container: FragmentContainerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.fragment_container)
    }


    companion object {
        fun isExternalStorageWritable(): Boolean {
            return Environment.getExternalStorageState() == MEDIA_MOUNTED
        }
        fun isExternalStorageReadable(): Boolean {
            return Environment.getExternalStorageState() in
                    setOf(MEDIA_MOUNTED, MEDIA_MOUNTED_READ_ONLY)
        }
        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext

            return if (isExternalStorageReadable() and isExternalStorageWritable()) {
                val externalCacheDir = context.externalCacheDirs.firstOrNull()?.let {
                    File(it, "Notable_OPENCV").apply { mkdir() }}
                if (externalCacheDir != null && externalCacheDir.exists())
                    externalCacheDir else appContext.cacheDir
            } else {
                appContext.cacheDir
            }
        }
    }
}