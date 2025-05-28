package octo.tricko.trickotrack.viewmodel

import android.app.Activity
import android.content.pm.PackageManager
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.ui.MapFragment
import octo.tricko.trickotrack.ui.SettingsFragment
import octo.tricko.trickotrack.utils.PhoneUtils

class MainViewModel : ViewModel() {


    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun requestPosition(activity: Activity) {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            // La permission n'est pas accordée, on la demande
            PhoneUtils.requestLocationPermission(activity) // Demande de permission de localisation
        }
    }

    fun checkPermission(activity: Activity): Boolean {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }

    //Traitement plus lisible ici pour le routage des fragments
    fun route(menuItem: MenuItem, supportFragmentManager: FragmentManager){
        when (menuItem.itemId) {
            R.id.accueil -> { // AccueilFragment
                supportFragmentManager.beginTransaction() // Remplace le fragment actuel par AccueilFragment
                    .replace(R.id.fragment_container, MapFragment())
                    .commit()
            }
            R.id.settings -> { // SettingsFragment
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SettingsFragment())
                    .commit()
            }
        }
    }
}