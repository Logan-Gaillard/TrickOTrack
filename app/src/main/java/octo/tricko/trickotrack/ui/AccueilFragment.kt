package octo.tricko.trickotrack.ui

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import octo.tricko.trickotrack.R
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.core.graphics.scale
import java.util.Random


class AccueilFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var mLocationOverlay: MyLocationNewOverlay // Déclaration de la variable mLocatationOverlay (= la localisation de l'utilisateur)
    private lateinit var mRotationGestureOverlay: RotationGestureOverlay // Déclaration de la variable mRotationGestureOverlay (= la rotation de la carte)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_accueil, container, false)

        initMap(view) // Initialisation de la carte

        return view
    }

    private fun initMap(view: View) {
        val configuration = org.osmdroid.config.Configuration.getInstance()
        //Si une préférence à été sauvegardée, on la charge sinon on en crée une nouvelle
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("osmdroid", 0)
        Configuration.getInstance().load(this.requireContext(), sharedPreferences) // Chargement de la configuration de la carte
        Configuration.getInstance().userAgentValue = this.requireContext().packageName // Définition de l'agent utilisateur pour la carte
        // Initialisation de la MapView

        mapView = view.findViewById(R.id.map) // Récupération de la référence à la MapView


        // Configuration de la carte
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.DEFAULT_TILE_SOURCE) // Définition de la source de tuiles
        mapView.setTilesScaledToDpi(true) // Mise à l'échelle des tuiles pour s'adapter à la densité de l'écran
        mapView.setMultiTouchControls(true) // Activation du zoom par pincement
        mapView.maxZoomLevel = 20.0 // Définition du niveau de zoom maximum
        mapView.minZoomLevel = 3.0 // Définition du niveau de zoom minimum
        mapView.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER) // Masquer les boutons de zoom

        // Configuration de la position initiale de la carte
        mapView.controller.setZoom(19.0) // Définition du niveau de zoom initial
        mapView.controller.setCenter(org.osmdroid.util.GeoPoint(48.8566, 2.3522)) // Définition de la position initiale de la carte (Paris)

        // Ajout d'un marqueur à la carte
        val marker = org.osmdroid.views.overlay.Marker(mapView)

        // Coordonnées d'une ville aléatoire en France
        val random = Random()
        val minLat = 42.33
        val maxLat = 50.75
        val minLon = -4.75
        val maxLon = 7.5
        val randomLat = minLat + (maxLat - minLat) * random.nextDouble()
        val randomLon = minLon + (maxLon - minLon) * random.nextDouble()

        marker.position = org.osmdroid.util.GeoPoint(randomLat, randomLon)
        //val icon: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.pos_icon, null)
        //marker.icon = icon // Icône du marqueur

        marker.title = "Ville aléatoire" // Titre du marqueur
        marker.setOnMarkerClickListener { _,_ ->
            true // Indique que l'événement a été traité
        } // Action à effectuer lorsque le marqueur est cliqué
        mapView.overlays.add(marker) // Ajout du marqueur à la carte
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Ajout de ma position sur la carte
        mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView) // Création d'un nouvel overlay de localisation
        mLocationOverlay.enableMyLocation() // Activation de la localisation
        mLocationOverlay.setPersonIcon(getPosIcon()) // Icone de position de l'utilisateur
        mLocationOverlay.setPersonAnchor(0.5f, 0.5f) // Position de l'icône de la personne
        mLocationOverlay.isDrawAccuracyEnabled = false // Activer l'affichage de la précision

        mLocationOverlay.enableFollowLocation() // Suivre l'utilisateur
        mapView.overlays.add(mLocationOverlay)

        mRotationGestureOverlay = RotationGestureOverlay(mapView) // Création d'un nouvel overlay de rotation

        mRotationGestureOverlay.setEnabled(true) // Activation de la rotation
        mapView.overlays.add(mRotationGestureOverlay) // Ajout de l'overlay de rotation à la carte
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