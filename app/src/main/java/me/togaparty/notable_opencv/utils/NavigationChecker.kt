package me.togaparty.notable_opencv.utils

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

fun Fragment.mayNavigate(navigationID: Int): Boolean {

    val navController = NavHostFragment.findNavController(this)
    val destinationIdInNavController = navController.currentDestination?.id

    // add tag_navigation_destination_id to your ids.xml so that it's unique:
    val destinationIdOfThisFragment = view?.getTag(navigationID) ?: destinationIdInNavController

    // check that the navigation graph is still in 'this' fragment, if not then the app already navigated:
    return if (destinationIdInNavController == destinationIdOfThisFragment) {
        view?.setTag(navigationID, destinationIdOfThisFragment)
        Log.d("FragmentExtensions", "May navigate: current destination is the current fragment.")
        true
    } else {
        Log.d("FragmentExtensions", "May not navigate: current destination is not the current fragment.")
        false
    }
}