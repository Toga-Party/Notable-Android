package me.togaparty.notable_android.ui.fragments

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import me.togaparty.notable_android.BuildConfig
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.data.ImageListProvider
import me.togaparty.notable_android.data.files.InspectPrediction
import me.togaparty.notable_android.data.files.JsonParser
import me.togaparty.notable_android.databinding.FragmentInspectBinding
import me.togaparty.notable_android.helper.GlideApp
import me.togaparty.notable_android.helper.GlideZoomOutPageTransformer
import me.togaparty.notable_android.ui.adapter.PredictionsAdapter
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import me.togaparty.notable_android.utils.toast

class InspectFragment :
    Fragment(R.layout.fragment_inspect),
    PredictionsAdapter.OnItemClickListener {

    private val binding by viewBinding(FragmentInspectBinding::bind)

    internal var selectedPosition: Int = 0
    internal var finalPosition: Int = 0

    private lateinit var model: ImageListProvider

    internal lateinit var predictionsAdapter: PredictionsAdapter
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter

    internal lateinit var rows: ArrayList<InspectPrediction>

    private lateinit var jsonParser: JsonParser
    internal lateinit var mediaSheetPlayer: MediaPlayer
    internal lateinit var mediaSegmentPlayer: MediaPlayer
    internal lateinit var seekBar: SeekBar
    internal lateinit var progressHandler: Handler
    private lateinit var runnable: Runnable

    private lateinit var currentImage: GalleryImage

    private var isPlaying: Boolean = false
    internal var wavFiles: Map<String, Uri>? = null
    internal var textFiles: Map<String, Uri>? = null
    private var imageFiles: ArrayList<Uri>? = null
    internal var imageMap: Map<String, Uri>? = null

    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaSegmentPlayer = MediaPlayer()
        mediaSheetPlayer = MediaPlayer()
        arguments?.let { bundle ->
            Log.d(TAG, "Inspect: Retrieving Bundle")

            currentImage = bundle.getParcelable<GalleryImage>("currentImage") as GalleryImage

            wavFiles = currentImage.wavFiles
            textFiles = currentImage.textFiles
            imageFiles = ArrayList(currentImage.imageFiles.values)

            imageMap = currentImage.imageFiles

            imageFiles?.let {
                finalPosition = it.size
            }
        }
        jsonParser = JsonParser.getInstance(requireContext())
        progressHandler = Handler(Looper.getMainLooper())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model = ViewModelProvider(requireActivity()).get(ImageListProvider::class.java)

        // Initial setup of media players to position 0
        seekBar = binding.seekBar
        seekBar.max = 0

        setButtonEvents(binding.playSegment)
        setButtonEvents(binding.playSheet)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var originalProgress: Int = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (mediaSegmentPlayer.isPlaying) {
                        mediaSegmentPlayer.seekTo(progress)
                    } else if (mediaSheetPlayer.isPlaying) {
                        mediaSheetPlayer.seekTo(progress)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    originalProgress = seekBar.progress
                }
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    originalProgress = seekBar.progress
                }
            }
        })
        // Lookup the recyclerview in activity layout
        val inspectRecycler = binding.recyclerPredictions
        // Initialize predictions
        if(BuildConfig.DEBUG) {
            Log.d("Inspect", "Creating entry at position: $selectedPosition")
        }
        rows = InspectPrediction.createPredictionList(textFiles, selectedPosition)
        // Create adapter passing in the sample data
        predictionsAdapter = PredictionsAdapter(rows, this)
        // Attach the adapter to the recyclerview to populate items
        inspectRecycler.adapter = predictionsAdapter
        // Set layout manager to position the items
        inspectRecycler.layoutManager = LinearLayoutManager(requireContext())


        galleryPagerAdapter = GalleryPagerAdapter(finalPosition)
        galleryPagerAdapter.notifyDataSetChanged()

        binding.viewPagerBanner.adapter = galleryPagerAdapter
        binding.viewPagerBanner.addOnPageChangeListener(viewPagerPageChangeListener)
        binding.viewPagerBanner.setPageTransformer(true, GlideZoomOutPageTransformer())
        navController = this.findNavController()
    }

    private fun setSeekBar(mediaPlayer: MediaPlayer){
        val fileDuration = mediaPlayer.duration
        seekBar.progress = 0
        seekBar.max = fileDuration / 1000
        runnable = object : Runnable {
            override fun run() {
                try {
                    seekBar.progress = mediaPlayer.currentPosition / 1000
                    if(BuildConfig.DEBUG) {
                        Log.d("Inspect", "DURATION:" + mediaPlayer.duration.toString())
                        Log.d("Inspect", "CURRENT:" + mediaPlayer.currentPosition.toString())
                        Log.d("Inspect", "PROGRESS:" + seekBar.progress.toString())
                        Log.d("Inspect", "MAX:" + seekBar.max.toString())
                    }
                    seekBar.refreshDrawableState()
                    progressHandler.postDelayed(this, 1000)
                }catch (e: Exception){
                    seekBar.progress = 0
                }
            }
        }
        progressHandler.postDelayed(runnable, 1000)
    }
    private fun setButtonEvents(button: Button) {
        lateinit var uri: Uri
        when (button.id) {
            R.id.play_sheet -> {
                if (wavFiles == null) Log.e(TAG, "THIS IS NULL")
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "WAVFILE size ${wavFiles?.isEmpty()}")
                }
                uri = wavFiles?.get("full_song")!!
                button.setOnClickListener {
                    try {
                        prepareMediaPlayer(uri, mediaSheetPlayer)
                        if (isPlaying) {
                            toast("Playback is busy")
                        } else {
                            mediaSheetPlayer.prepareAsync()
                            isPlaying = true
                        }
                    } catch (e: Exception) {
                        if(BuildConfig.DEBUG) {
                            Log.d("Inspect", "${e.printStackTrace()}")
                        } else {
                            e.printStackTrace()
                        }
                    }
                }
            }
            R.id.play_segment -> {
                button.setOnClickListener {
                    try {
                        uri = wavFiles?.get("staff$selectedPosition")!!
                        prepareMediaPlayer(uri, mediaSegmentPlayer)
                        if (isPlaying) {
                            toast("Playback is busy")
                        } else {
                            mediaSegmentPlayer.prepareAsync()
                            isPlaying = true
                        }
                    } catch (e: Exception) {
                        if(BuildConfig.DEBUG) {
                            Log.d("Inspect", "${e.printStackTrace()}")
                        } else {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

    }
    internal fun prepareMediaPlayer(uri: Uri?, mediaPlayer: MediaPlayer){
        if(mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)
            progressHandler.removeCallbacks(runnable)
            seekBar.progress = 0
            seekBar.max = 0
            isPlaying = false
        }else{
            mediaPlayer.reset()
            if (uri != null) {
                mediaPlayer.setDataSource(requireContext(), uri)
            }
            mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            )
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.reset()
                progressHandler.removeCallbacks(runnable)
                seekBar.progress = 0
                seekBar.max = 0

                isPlaying = false
            }
            mediaPlayer.setOnPreparedListener { mp ->
                if (!mp.isPlaying) {
                    mp.start()
                    setSeekBar(mediaPlayer)
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        try {
            if(::mediaSheetPlayer.isInitialized) {
                mediaSheetPlayer.release()
            }
            if(::mediaSegmentPlayer.isInitialized) {
                mediaSegmentPlayer.release()
            }
        } catch (e: IllegalStateException) {
            // media player is not initialized
        }

    }
    override fun onItemClick(position: Int, view: TextView) {
        val regexPattern =
            Regex("(?=.*?-.*?_?.*?)(note|gracenote|rest|multirest|clef|keySignature|timeSignature)[_\\-]?(?:([A-G][b#]?[1-6])|rest)?(\\S*)?")

        val matches = regexPattern.find(view.text.toString())
        matches?.let {
            val (first, second, third) = it.destructured
            val replacedDuration = third.replace(Regex("_\\.*-?"), " ").trim()

            val bundle = bundleOf("first" to first, "second" to second, "third" to replacedDuration)
            navController.navigate(R.id.action_inspectFragment_to_wikiFragment, bundle)
        }?: run {
            val bundle = bundleOf("first" to view.text.toString(), "second" to "", "third" to "")
            navController.navigate(R.id.action_inspectFragment_to_wikiFragment, bundle)
        }
    }
    internal fun setCurrentItem(position: Int) {
        binding.viewPagerBanner.setCurrentItem(position, false)
        selectedPosition = position
    }

    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
                if(BuildConfig.DEBUG) {
                    Log.d("Inspect", "Selected: $position Max: $finalPosition")
                }
                setCurrentItem(position)
                val uri = wavFiles?.get("staff$position")!!
                prepareMediaPlayer(uri, mediaSegmentPlayer)
                InspectPrediction.replacePredictionList(
                    textFiles,
                    selectedPosition,
                    predictionsAdapter,
                    rows
                )
            }
            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {
            }
            override fun onPageScrollStateChanged(arg0: Int) {
            }
        }

    inner class GalleryPagerAdapter(itemCount: Int) : PagerAdapter() {
        private var count : Int = itemCount
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if(BuildConfig.DEBUG) {
                Log.d("Inspect", "Pager Adapter: $position")
            }

            val view = layoutInflater.inflate(R.layout.fragment_inspect_image, container, false)

            val fileurl = imageMap?.get("slice$position")

            val circularProgressDrawable = CircularProgressDrawable(requireContext())
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            // load image
            GlideApp.with(context!!)
                .load(fileurl)
                .placeholder(circularProgressDrawable)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view.findViewById(R.id.inspectImage))
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return count
        }
        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj as View
        }
        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }
    }
    companion object
}
