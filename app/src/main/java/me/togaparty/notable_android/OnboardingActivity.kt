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
			Toast.makeText(
				applicationContext,
				"Swiped from $oldElementIndex to $newElementIndex", Toast.LENGTH_SHORT
			).show()
		}

		engine.setOnRightOutListener { // Probably here will be your exit action
			Toast.makeText(applicationContext, "Swiped out right", Toast.LENGTH_SHORT).show()
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
				"Glossary", "Lorep ipsum yes",
				Color.parseColor("#678FB4"), R.drawable.book_icon, R.drawable.search_icon
			)
			)
			add(
				PaperOnboardingPage(
				"Camera", "Testing",
				Color.parseColor("#65B0B4"), R.drawable.camera_icon, R.drawable.camera_icon
			)
			)
			add(
				PaperOnboardingPage(
				"Files", "All files are categorized for your lazinees",
				Color.parseColor("#9B90BC"), R.drawable.files_icon, R.drawable.files_icon
			)
			)
		}
	}
	companion object {
		val COMPLETED_ONBOARDING_PREF_NAME = "FirstTimeUser"
	}
}