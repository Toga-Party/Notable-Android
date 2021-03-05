package me.togaparty.notable_android.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
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
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_inspect_image.view.*
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.GalleryImage
import me.togaparty.notable_android.data.ImageListProvider
import me.togaparty.notable_android.data.files.InspectPrediction
import me.togaparty.notable_android.data.files.JsonParser
import me.togaparty.notable_android.helper.GlideApp
import me.togaparty.notable_android.helper.GlideZoomOutPageTransformer
import me.togaparty.notable_android.ui.adapter.PredictionsAdapter
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import me.togaparty.notable_android.utils.toast


class InspectFragment : Fragment(), PredictionsAdapter.OnItemClickListener {

    private lateinit var viewPager: ViewPager

    internal var selectedPosition: Int = 0
    internal var finalPosition: Int = 0

    private lateinit var model: ImageListProvider

    internal lateinit var predictionsAdapter: PredictionsAdapter
    private lateinit var galleryPagerAdapter: GalleryPagerAdapter

    internal lateinit var rows: ArrayList<InspectPrediction>

    private lateinit var jsonParser: JsonParser
    private lateinit var mediaSheetPlayer: MediaPlayer
    private lateinit var mediaSegmentPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var progressHandler: Handler
    private lateinit var runnable: Runnable

    private lateinit var currentImage: GalleryImage
    private var isPlaying: Boolean = false
    private var wavFiles: Map<String, Uri>? = null
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
        progressHandler = Handler()
    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
                R.layout.fragment_inspect,
                container,
                false
        )
        model = ViewModelProvider(requireActivity()).get(ImageListProvider::class.java)

        // Initial setup of mediaplayers to position 0
        val btnPlaySegment: Button = view.findViewById(R.id.play_segment) as Button
        val btnPlaySheet: Button = view.findViewById(R.id.play_sheet) as Button

        val seekBarHandler = Handler()

        seekBar = view.findViewById(R.id.seekBar) as SeekBar
        seekBar.max = 0

        setButtonEvents(view, btnPlaySheet, selectedPosition)
        setButtonEvents(view, btnPlaySegment, selectedPosition)

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
        val inspectRecycler = view.findViewById(R.id.recycler_predictions) as RecyclerView
        // Initialize predictions
        Log.d("Inspect", "Creating entry at position: $selectedPosition")
        rows = InspectPrediction.createPredictionList(textFiles, selectedPosition)
        // Create adapter passing in the sample data
        predictionsAdapter = PredictionsAdapter(rows, this)
        // Attach the adapter to the recyclerview to populate items
        inspectRecycler.adapter = predictionsAdapter
        // Set layout manager to position the items
        inspectRecycler.layoutManager = LinearLayoutManager(requireContext())
        viewPager = view.findViewById(R.id.viewPagerBanner)
        galleryPagerAdapter = GalleryPagerAdapter(finalPosition)
        galleryPagerAdapter.notifyDataSetChanged()
        viewPager.adapter = galleryPagerAdapter
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager.setPageTransformer(true, GlideZoomOutPageTransformer())
        navController = this.findNavController()
        return view
    }
    private fun setSeekBar(mediaPlayer: MediaPlayer){
        val fileDuration = mediaPlayer.duration
        seekBar.progress = 0
        seekBar.max = fileDuration / 1000
        runnable = object : Runnable {
            override fun run() {
                try {
                    seekBar.progress = mediaPlayer.currentPosition / 1000
                    Log.d("Inspect", "DURATION:" + mediaPlayer.duration.toString())
                    Log.d("Inspect", "CURRENT:" + mediaPlayer.currentPosition.toString())
                    Log.d("Inspect", "PROGRESS:" + seekBar.progress.toString())
                    Log.d("Inspect", "MAX:" + seekBar.max.toString())
                    seekBar.refreshDrawableState()
                    progressHandler.postDelayed(this, 1000)
                }catch (e: Exception){
                    seekBar.progress = 0
                }
            }
        }
        progressHandler.postDelayed(runnable, 1000)
    }
    private fun setButtonEvents(view: View, button: Button, position: Int) {
        lateinit var uri: Uri
        when (button.id) {
            R.id.play_sheet -> {
                if (wavFiles == null) Log.e(TAG, "THIS IS NULL")
                Log.d(TAG, "WAVFILE size ${wavFiles?.isEmpty()}")
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
                        Log.d("Inspect", "${e.printStackTrace()}")
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
                        Log.d("Inspect", "${e.printStackTrace()}")
                    }
                }
            }
        }

    }
    private fun prepareMediaPlayer(uri: Uri?, mediaPlayer: MediaPlayer){
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
        //Button Click event exposes aligned TextView control
//        jsonParser.mapOfTermsAndDef.forEach{
//            (key, value) ->
//            Log.i(TAG, "Key: $key, Value: $value")
//        }
        //TODO: Proof of concept
        toast(view.text.toString() + " $position clicked")
//        val definition: String? = jsonParser.mapOfTermsAndDef["Staff"]
//        val bundle = bundleOf("term" to view.text.toString(), "definition" to definition)
//        navController.navigate(R.id.action_inspectFragment_to_glossaryDefinitionFragment,bundle)


        val regexPattern =
            Regex("(?=.*?-.*?_?.*?)(note|gracenote|rest|multirest|clef|keySignature|timeSignature)[_\\-]?(?:([A-G][b#]?[1-6])|rest)?(\\S*)?")

        val matches = regexPattern.find(view.text.toString())
        matches?.let {
            val (term, note, duration) = it.destructured
            Log.d(TAG, "term caught: $term")
            Log.d(TAG, "note caught: $note")
            val replacedDuration = duration.replace(Regex("_\\.*-?"), " ").trim()
            Log.d(TAG, "duration caught: $replacedDuration")

            val bundle = bundleOf("term" to term, "note" to note, "duration" to replacedDuration)
            navController.navigate(R.id.action_inspectFragment_to_wikiFragment, bundle)
        }?: Log.d(TAG, "This term is not matched by regex: ${view.text}")
        val bundle = bundleOf("term" to view.text, "note" to "", "duration" to "")
        navController.navigate(R.id.action_inspectFragment_to_wikiFragment)
    }
    internal fun setCurrentItem(position: Int) {
        viewPager.setCurrentItem(position, false)
        selectedPosition = position
    }

    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            @SuppressLint("LogConditional")
            override fun onPageSelected(position: Int) {
                Log.d("Inspect", "Selected: $position Max: $finalPosition")
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

            Log.d("Inspect", "Pager Adapter: $position")
            val view = layoutInflater.inflate(R.layout.fragment_inspect_image, container, false)

            val fileurl = imageMap?.get("slice$position")
            view.inspectImage.tag = fileurl

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
                .into(view.inspectImage)
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