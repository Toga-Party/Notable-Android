package me.togaparty.notable_opencv.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import me.togaparty.notable_opencv.R

class DashboardFragment : Fragment(), View.OnClickListener {
    private lateinit var navController: NavController
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = this.findNavController()
        view.findViewById<CardView>(R.id.camera_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.files_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.settings_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.glossary_cardview).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.camera_cardview ->
                return if(ContextCompat.checkSelfPermission(requireContext(),
                          Manifest.permission.READ_EXTERNAL_STORAGE) ==
                          PackageManager.PERMISSION_GRANTED){
                          navController.navigate(
                                DashboardFragmentDirections.actionDashboardFragmentToCameraFragment())
                    } else {
                        setFragmentResult("requestKey",
                                bundleOf("actionDirection"
                                        to "toCamera"))
                        navController.navigate(
                                DashboardFragmentDirections.actionDashboardFragmentToPermissionsFragment())
                    }
            R.id.files_cardview ->
                return if(ContextCompat.checkSelfPermission(requireContext(),
                          Manifest.permission.READ_EXTERNAL_STORAGE) == 
                          PackageManager.PERMISSION_GRANTED){
                          navController.navigate(
                                  DashboardFragmentDirections.actionDashboardFragmentToGalleryFragment())
                        } else {
                            setFragmentResult("requestKey",
                                    bundleOf("actionDirection"
                                            to "toGallery"))
                            navController.navigate(
                                    DashboardFragmentDirections.actionDashboardFragmentToPermissionsFragment())
                        }


            R.id.settings_cardview -> navController.navigate(
                    DashboardFragmentDirections.actionDashboardFragmentToSettingsFragment())
            R.id.glossary_cardview -> navController.navigate(
                    DashboardFragmentDirections.actionDashboardFragmentToGlossaryFragment())
        }
    }

    companion object
}