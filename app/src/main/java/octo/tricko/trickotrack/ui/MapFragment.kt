package octo.tricko.trickotrack.ui

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
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
import androidx.core.view.doOnDetach
import com.google.android.material.floatingactionbutton.FloatingActionButton
import octo.tricko.trickotrack.repository.MapRepository
import org.osmdroid.util.GeoPoint

class MapFragment : Fragment() {

    public lateinit var mapView: MapView
    private lateinit var mLocationOverlay: MyLocationNewOverlay // Déclaration de la variable mLocatationOverlay (= la localisation de l'utilisateur)
    private lateinit var mRotationGestureOverlay: RotationGestureOverlay // Déclaration de la variable mRotationGestureOverlay (= la rotation de la carte)

    private var mapRepository: MapRepository = MapRepository() // Instanciation de la classe MapRepository

    private lateinit var centreBtn: FloatingActionButton // Déclaration de la variable centreBtn (= le bouton de centrage de la carte)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        //Si une préférence à été sauvegardée, on la charge sinon on en crée une nouvelle
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("osmdroid", 0)
        Configuration.getInstance().load(context, sharedPreferences) // Chargement de la configuration de la carte
        Configuration.getInstance().userAgentValue = requireContext().packageName // Définition de l'agent utilisateur pour la carte

        mapRepository.initMap(view, this@MapFragment, requireContext()) // Initialisation de la carte

        centreBtn = view.findViewById(R.id.centerPosBtn) // Récupération de la référence au bouton de centrage

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Ajout de ma position sur la carte
        mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView) // Création d'un nouvel overlay de localisation
        mLocationOverlay.enableMyLocation() // Activation de la localisation
        mLocationOverlay.setPersonIcon(getPosIcon()) // Icone de position de l'utilisateur
        mLocationOverlay.setDirectionIcon(getPosIcon()) // Icone de direction de l'utilisateur
        mLocationOverlay.setPersonAnchor(0.5f, 0.5f) // Position de l'icône de la personne
        mLocationOverlay.setDirectionAnchor(0.5f, 0.5f) // Position de l'icône de direction
        mLocationOverlay.isDrawAccuracyEnabled = false // Activer l'affichage de la marge d'erreur

        mapView.overlays.add(mLocationOverlay)

        mRotationGestureOverlay = RotationGestureOverlay(mapView) // Création d'un nouvel overlay de rotation

        mRotationGestureOverlay.setEnabled(true) // Activation de la rotation
        mapView.overlays.add(mRotationGestureOverlay) // Ajout de l'overlay de rotation à la carte

        //vérification des permissions de localisation
        if(PermissionChecker.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED) {
            Log.d("Permission", "Permission accordée")
            val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager // Accès au service de localisation android
            val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) // Récupération de la dernière position connue sur le GPS
            location?.let { // Si la position n'est pas nulle
                val latitude = it.latitude
                val longitude = it.longitude
                val userLocation = GeoPoint(latitude, longitude)

                // Centrer la carte sur la position
                mapView.controller.setCenter(userLocation) // Définition de la position de la carte sur la position de l'utilisateur
            }

        }else{
            Log.d("Permission", "Permission non accordée")
            // Permission non accordée, alors nous l'alertons
            mapView.controller.setCenter(GeoPoint(48.8566, 2.3522)) // Définition de la position de la carte sur Paris
        }
        mLocationOverlay.enableFollowLocation() // Activation du suivi de la position de l'utilisateur

        centreBtn.setOnClickListener {
            mapView.controller.zoomTo(18.5, 1000)
            mLocationOverlay.enableFollowLocation() // Activation du suivi de la position de l'utilisateur
            centreBtn.visibility = View.INVISIBLE // Masquer le bouton de centrage
        }

        mapView.setOnTouchListener { _, event ->
            centreBtn.visibility = View.VISIBLE // Masquer le bouton de centrage
            mapView.controller.stopAnimation(true)
            false
        }
    }

    private fun getPosIcon(): Bitmap? {
        val drawable = ResourcesCompat.getDrawable(resources,R.drawable.pos_icon,null)
        var bitmap : Bitmap? = drawable?.toBitmap()
        bitmap = bitmap?.scale(70, 70, false) // Redimensionnement de l'icône
        return bitmap
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