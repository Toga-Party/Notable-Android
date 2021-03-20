package me.togaparty.notable_android


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import android.os.Environment.MEDIA_MOUNTED_READ_ONLY
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.preference.PreferenceManager
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var container: FragmentContainerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.fragment_container)
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            if (!getBoolean(OnBoardingActivity.COMPLETED_ONBOARDING_PREF_NAME, false)) {
                val intent = Intent(this@MainActivity, OnBoardingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
    companion object {
        private fun isExternalStorageWritable(): Boolean {
            return Environment.getExternalStorageState() == MEDIA_MOUNTED
        }
        private fun isExternalStorageReadable(): Boolean {
            return Environment.getExternalStorageState() in
                    setOf(MEDIA_MOUNTED, MEDIA_MOUNTED_READ_ONLY)
        }
        fun getOutputCacheDirectory(context: Context): File {
            val appContext = context.applicationContext
            if (isExternalStorageWritable() && isExternalStorageReadable()) {
                val externalCacheDir = appContext.externalCacheDirs.firstOrNull()?.let {
                    File(it, "Notable").apply { mkdir() }}
                return if (externalCacheDir != null && externalCacheDir.exists())
                    externalCacheDir else appContext.cacheDir
            }
            return appContext.cacheDir
        }
        fun externalAppSpecificStorage(context: Context): File {
            val appContext = context.applicationContext
            if (isExternalStorageWritable() && isExternalStorageReadable()) {
                return appContext.getExternalFilesDirs(Environment.DIRECTORY_DOWNLOADS)
                    .first().let{
                        File(it, "Notable").apply { mkdir() }
                    }

            }
            return appContext.filesDir
        }
    }
}