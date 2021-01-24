package me.togaparty.notable_opencv.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import me.togaparty.notable_opencv.R

class DashboardFragment : Fragment(), View.OnClickListener {
    var navController: NavController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        view.findViewById<CardView>(R.id.camera_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.files_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.settings_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.glossary_cardview).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.camera_cardview -> navController!!.navigate(R.id.action_dashboardFragment_to_permissionsFragment)
            R.id.files_cardview -> navController!!.navigate(R.id.action_dashboardFragment_to_filesFragment)
            R.id.settings_cardview -> navController!!.navigate(R.id.action_dashboardFragment_to_settingsFragment)
            R.id.glossary_cardview -> navController!!.navigate(R.id.action_dashboardFragment_to_glossaryFragment)
        }
    }

    companion object {
    }
}