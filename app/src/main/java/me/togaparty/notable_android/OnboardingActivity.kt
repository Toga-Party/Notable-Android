package me.togaparty.notable_android

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.ramotion.paperonboarding.PaperOnboardingEngine
import com.ramotion.paperonboarding.PaperOnboardingPage

class OnboardingActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_onboarding)
		val engine = PaperOnboardingEngine(
			findViewById(R.id.onboardingRootView), data, applicationContext
		)
		engine.setOnChangeListener { oldElementIndex, newElementIndex ->
//			Toast.makeText(
//				applicationContext,
//				"Swiped from $oldElementIndex to $newElementIndex", Toast.LENGTH_SHORT
//			).show()
		}

		engine.setOnRightOutListener { // Probably here will be your exit action
//			Toast.makeText(applicationContext, "Swiped out right", Toast.LENGTH_SHORT).show()
			PreferenceManager.getDefaultSharedPreferences(this).edit().apply {
				putBoolean(COMPLETED_ONBOARDING_PREF_NAME, true)
				apply()
			}
			val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			startActivity(intent)
		}
	}
	private val data: ArrayList<PaperOnboardingPage> by lazy {

		arrayListOf<PaperOnboardingPage>().apply {
			add(
				PaperOnboardingPage(
				"Glossary", "Search the glossary for common terms and familiarize yourself with the fundamentals of music theory",
				Color.parseColor("#ffffff"), R.drawable.book_icon_150, R.drawable.search_icon
			)
			)
			add(
				PaperOnboardingPage(
				"Scan", "Supports image cropping and rotation adjustments after capture of the music sheet; resulting image can then be saved into the app's gallery",
				Color.parseColor("#ffffff"), R.drawable.camera_icon_150, R.drawable.image_icon
			)
			)
			add(
				PaperOnboardingPage(
				"Gallery", "Gives you the ability to process digital music sheets. Processed music sheets can then be inspected to provide an instant look-up of scanned symbols and access to the playback of a fully generated melody",
				Color.parseColor("#ffffff"), R.drawable.files_icon_150, R.drawable.file_icon
			)
			)
		}
	}
	companion object {
		val COMPLETED_ONBOARDING_PREF_NAME = "FirstTimeUser"
	}
}