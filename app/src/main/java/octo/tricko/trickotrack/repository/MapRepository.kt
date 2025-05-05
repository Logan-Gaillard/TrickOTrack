package octo.tricko.trickotrack.repository

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.ui.MapFragment
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.util.Random

class MapRepository {

    fun initMap(view: View, activity: MapFragment, context: Context) {
        // Initialisation de la MapView

        activity.mapView = view.findViewById(R.id.map) // Récupération de la référence à la MapView

        // Configuration de la carte
        activity.mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.DEFAULT_TILE_SOURCE) // Définition de la source de tuiles
        activity.mapView.setTilesScaledToDpi(true) // Mise à l'échelle des tuiles pour s'adapter à la densité de l'écran
        activity.mapView.setMultiTouchControls(true) // Activation du zoom par pincement
        activity.mapView.maxZoomLevel = 18.5 // Définition du niveau de zoom maximum
        activity.mapView.minZoomLevel = 3.0 // Définition du niveau de zoom minimum
        activity.mapView.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER) // Masquer les boutons de zoom

        // Configuration de la position initiale de la carte
        activity.mapView.controller.setZoom(18.5) // Définition du niveau de zoom initial
        //mapView.controller.setCenter(org.osmdroid.util.GeoPoint(48.8566, 2.3522)) // Définition de la position initiale de la carte (Paris)

        // Ajout d'un marqueur à la carte
        val marker = org.osmdroid.views.overlay.Marker(activity.mapView)

        // Coordonnées d'une ville aléatoire en France
        val random = Random()
        val minLat = 42.33
        val maxLat = 50.75
        val minLon = -4.75
        val maxLon = 7.5
        val randomLat = minLat + (maxLat - minLat) * random.nextDouble()
        val randomLon = minLon + (maxLon - minLon) * random.nextDouble()

        marker.position = GeoPoint(randomLat, randomLon)
        //val icon: Drawable? = ResourcesCompat.getDrawable(resources, R.drawable.pos_icon, null)
        //marker.icon = icon // Icône du marqueur

        marker.title = "Ville aléatoire" // Titre du marqueur
        marker.setOnMarkerClickListener { _,_ ->
            Log.d("Marker", "Marker clicked") // Log lorsque le marqueur est cliqué
            true // Indique que l'événement a été traité
        } // Action à effectuer lorsque le marqueur est cliqué
        activity.mapView.overlays.add(marker) // Ajout du marqueur à la carte
    }
}