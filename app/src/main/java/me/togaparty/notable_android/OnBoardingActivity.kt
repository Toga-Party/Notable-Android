package me.togaparty.notable_android


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.ramotion.paperonboarding.PaperOnboardingPage
import me.togaparty.notable_android.databinding.ActivityOnboardingBinding
import me.togaparty.notable_android.ui.fragments.CustomOnBoardingFragment
import me.togaparty.notable_android.utils.viewBinding


class OnBoardingActivity : AppCompatActivity() {

	private val binding by viewBinding(ActivityOnboardingBinding::inflate)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		val onBoardingFragment = CustomOnBoardingFragment.newInstance(data)

		val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
		fragmentTransaction.add(R.id.fragment_container, onBoardingFragment)
		fragmentTransaction.commit()

		onBoardingFragment.setOnRightOutListener {
			PreferenceManager.getDefaultSharedPreferences(this).edit().apply {
				putBoolean(COMPLETED_ONBOARDING_PREF_NAME, true)
				apply()
			}
			val intent = Intent(this@OnBoardingActivity, MainActivity::class.java)
			intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			startActivity(intent)
		}
	}
	private val data: ArrayList<PaperOnboardingPage> by lazy {

		arrayListOf<PaperOnboardingPage>().apply {

			add(
				PaperOnboardingPage(
					"Scan",
					"This section contains the following features: Capture Image, Retake Image, Crop, and Save Image.",
					Color.parseColor("#ffffff"),
					R.drawable.camera_icon_150,
					R.drawable.image_icon
				)
			)

			add(
				PaperOnboardingPage(
					"Capture Image",
					"An image of a music sheet must first be captured.",
					Color.parseColor("#ffe573"),
					R.drawable.capture_icon,
					R.drawable.capture_icon
				)
			)
			add(
				PaperOnboardingPage(
					"Retake Image",
					"If not satisfied with the initial captured image, the user can opt to retake the image.",
					Color.parseColor("#ffe573"),
					R.drawable.arrow_back,
					R.drawable.arrow_back
				)
			)
			add(
				PaperOnboardingPage(
					"Crop",
					"After capture of image, the user would be given the chance to crop, rotate or scale the photo.",
					Color.parseColor("#ffe573"),
					R.drawable.crop_icon,
					R.drawable.crop_icon
				)
			)
			add(
				PaperOnboardingPage(
					"Save Image",
					"If the user is satisfied with the image, they can now save it to the gallery.",
					Color.parseColor("#ffe573"),
					R.drawable.save_image,
					R.drawable.save_image
				)
			)

			add(
				PaperOnboardingPage(
					"Gallery",
					"This section contains: Delete Image, Process Image, and Inspect Music.",
					Color.parseColor("#ffffff"),
					R.drawable.files_icon_150,
					R.drawable.file_icon
				)
			)
			add(
					PaperOnboardingPage(
							"Import Image", "The user has the option to import an image from another directory.",
							Color.parseColor("#ffe573"),
							R.drawable.ic_import_black,
							R.drawable.ic_import_black
					)
			)
			add(
				PaperOnboardingPage(
					"Delete Image", "The user has the option to delete an image.",
					Color.parseColor("#ffe573"),
					R.drawable.trash,
					R.drawable.trash
				)

			)
			add(
				PaperOnboardingPage(
					"Process Image",
					"The music sheet must be first processed before they can be inspected.",
					Color.parseColor("#ffe573"),
					R.drawable.ic_icons8_refresh,
					R.drawable.ic_icons8_refresh
				)
			)
			add(
				PaperOnboardingPage(
					"Inspect Music",
					"After processing the music sheet, the inspect music button will then appear. " +
							"Clicking the inspect button will redirect the users to a page where an instant look-up of scanned symbols are shown and playback of a fully generated melody are accessible.",
					Color.parseColor("#ffe573"),
					R.drawable.search_icon_150,
					R.drawable.search_icon_150
				)
			)
			add(
				PaperOnboardingPage(
					"Glossary",
					"The user can search the glossary for common terms and familiarize themselves with the fundamentals of music theory",
					Color.parseColor("#ffffff"),
					R.drawable.book_icon_150,
					R.drawable.search_icon
				)
			)

			add(
					PaperOnboardingPage(
							"Monophonic Music Sheet",
							"This application is limited to processing Monophonic music sheets only. The Monophonic texture is the simplest among all the types of texture in music. " +
									"It is defined as a piece of music where there is only one melodic sound being heard. " +
									"An example of a Monophonic music sheet is shown at the next page.",
							Color.parseColor("#ffffff"),
							R.drawable.ic_check,
							R.drawable.ic_check_yellow
					)
			)

			add(
					PaperOnboardingPage(
							"",
							"",
							Color.parseColor("#ffffff"),
							R.drawable.ic_monophonic__joytotheworld,
							R.drawable.ic_musicnote
					)
			)

			add(
					PaperOnboardingPage(
							"Non-Monophonic Music Sheet",
							"Please refrain from using Non-monophonic music sheets. " +
									"Non-monophonic textures include polyphonic, homophonic, and heterophonic textures. " +
									"An example of non-monophonic music sheet (polyphonic) is shown at the next page.",
							Color.parseColor("#ffffff"),
							R.drawable.ic_close,
							R.drawable.ic_close_yellow
					)
			)

			add(
					PaperOnboardingPage(
							"",
							"",
							Color.parseColor("#ffffff"),
							R.drawable.ic_polyphonic,
							R.drawable.ic_musicnote
					)
			)
		}
	}
	companion object {
		const val COMPLETED_ONBOARDING_PREF_NAME = "FirstTimeUser"
	}
}