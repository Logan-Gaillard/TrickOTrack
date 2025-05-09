package octo.tricko.trickotrack.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import octo.tricko.trickotrack.R
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.core.graphics.scale
import com.google.android.material.floatingactionbutton.FloatingActionButton
import octo.tricko.trickotrack.repository.MapRepository
import octo.tricko.trickotrack.ui.components.CustomInfoWindow
import org.osmdroid.api.IGeoPoint
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MapFragment : Fragment() {

    lateinit var mapView: MapView
    lateinit var mLocationOverlay: MyLocationNewOverlay // Déclaration de la variable mLocatationOverlay (= la localisation de l'utilisateur)
    lateinit var mRotationGestureOverlay: RotationGestureOverlay // Déclaration de la variable mRotationGestureOverlay (= la rotation de la carte)

    lateinit var alertBottomFragment: AlertBottomFragment
    private var mapRepository: MapRepository = MapRepository() // Instanciation de la classe MapRepository

    lateinit var centreBtn: FloatingActionButton // Déclaration de la variable centreBtn (= le bouton de centrage de la carte)
    lateinit var alertBtn: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        //Si une préférence à été sauvegardée, on la charge sinon on en crée une nouvelle
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("osmdroid", 0)
        Configuration.getInstance().load(context, sharedPreferences) // Chargement de la configuration de la carte
        Configuration.getInstance().userAgentValue = requireContext().packageName // Définition de l'agent utilisateur pour la carte

        mapRepository.initMap(view, this@MapFragment) // Initialisation de la carte

        return view
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume() // Reprendre la carte lorsque le fragment est visible
    }

    override fun onPause() {
        // Sauvegarde de la configuration de la carte
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("osmdroid", 0)
        Configuration.getInstance().save(this.requireContext(), sharedPreferences)

        super.onPause()
        mapView.onPause() // Mettre en pause la carte lorsque le fragment n'est plus visible
    }
}