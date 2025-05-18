package octo.tricko.trickotrack.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.PermissionChecker
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import com.google.android.material.floatingactionbutton.FloatingActionButton
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.model.PlaceModel
import octo.tricko.trickotrack.ui.MarkAskBottomFragment
import octo.tricko.trickotrack.ui.MapFragment
import octo.tricko.trickotrack.ui.components.CustomInfoWindow
import org.osmdroid.api.IGeoPoint
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale
import java.util.Random

class MapRepository(mapFragment: MapFragment) {

    private val markers: MutableList<Marker> = mutableListOf() // Liste des marqueurs sur la carte
    private var actualMarker: Marker? = null; // Marqueur temporaire
    private lateinit var mapViewFragment: MapView // Déclaration de la variable mapView (= la carte)

    private val fragment: MapFragment = mapFragment // Récupération de la référence au fragment



    fun initMap(view: View) {
        // Initialisation de la MapView
        val initedMapView = view.findViewById<MapView>(R.id.map) // Récupération de la référence à la MapView
        fragment.mapView = initedMapView // Récupération de la référence à la MapView
        mapViewFragment = initedMapView // Récupération de la référence à la MapView

        // Configuration de la carte
        mapViewFragment.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.DEFAULT_TILE_SOURCE) // Définition de la source de tuiles
        mapViewFragment.setTilesScaledToDpi(true) // Mise à l'échelle des tuiles pour s'adapter à la densité de l'écran
        mapViewFragment.setMultiTouchControls(true) // Activation du zoom par pincement
        mapViewFragment.maxZoomLevel = 18.5 // Définition du niveau de zoom maximum
        mapViewFragment.minZoomLevel = 3.0 // Définition du niveau de zoom minimum
        mapViewFragment.zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER) // Masquer les boutons de zoom

        // Configuration de la position initiale de la carte
        mapViewFragment.controller.setZoom(18.5) // Définition du niveau de zoom initial

        fragment.alertBtn = view.findViewById(R.id.reportBtn)
        fragment.centreBtn = view.findViewById(R.id.centerPosBtn) // Récupération de la référence au bouton de centrage

        //Ajout de ma position sur la carte
        fragment.mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(mapViewFragment.context), mapViewFragment) // Création d'un nouvel overlay de localisation
        fragment.mLocationOverlay.enableMyLocation() // Activation de la localisation
        fragment.mLocationOverlay.setPersonIcon(getPosIcon()) // Icone de position de l'utilisateur
        fragment.mLocationOverlay.setDirectionIcon(getPosIcon()) // Icone de direction de l'utilisateur
        fragment.mLocationOverlay.setPersonAnchor(0.5f, 0.5f) // Position de l'icône de la personne
        fragment.mLocationOverlay.setDirectionAnchor(0.5f, 0.5f) // Position de l'icône de direction
        fragment.mLocationOverlay.isDrawAccuracyEnabled = false // Activer l'affichage de la marge d'erreur
        initedMapView.overlays.add(fragment.mLocationOverlay)

        fragment.mRotationGestureOverlay = RotationGestureOverlay(mapViewFragment) // Création d'un nouvel overlay de rotation
        fragment.mRotationGestureOverlay.setEnabled(true) // Activation de la rotation
        mapViewFragment.overlays.add(fragment.mRotationGestureOverlay) // Ajout de l'overlay de rotation à la carte

        //vérification des permissions de localisation
        if(PermissionChecker.checkSelfPermission(fragment.requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED) {
            Log.d("Permission", "Permission accordée")
            val locationManager = fragment.requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager // Accès au service de localisation android
            val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) // Récupération de la dernière position connue sur le GPS
            location?.let { // Si la position n'est pas nulle
                val latitude = it.latitude
                val longitude = it.longitude
                val userLocation = GeoPoint(latitude, longitude)

                // Centrer la carte sur la position
                mapViewFragment.controller.setCenter(userLocation) // Définition de la position de la carte sur la position de l'utilisateur
            }

        }else{
            Log.d("Permission", "Permission non accordée")
            // Permission non accordée, alors nous l'alertons
            mapViewFragment.controller.setCenter(GeoPoint(48.8566, 2.3522)) // Définition de la position de la carte sur Paris
        }
        fragment.mLocationOverlay.enableFollowLocation() // Activation du suivi de la position de l'utilisateur

        initEvents() // Initialisation des événements de la carte
        fragment.placeModel.initAutoMarkUpdater() // Initialisation de l'auto-mise à jour des marquages
    }

// ---------------------- //
// EVENEMENTS DU FRAGMENT //
// ---------------------- //

    private fun initEvents(){
        //Lors d'un click sur la map
        initMapEventOverlay()
        initOnTouchEvent(mapViewFragment, fragment.centreBtn, fragment.mLocationOverlay)
        initOnClickAlertBtn(fragment.alertBtn) // Initialisation du bouton d'alerte
        initOnClickCentreBtn(fragment.centreBtn) // Initialisation du bouton de centrage

        fragment.parentFragmentManager.setFragmentResultListener("MaskAskBottom", fragment.viewLifecycleOwner) { _, bundle ->
            val isClose : Boolean = bundle.getBoolean("is_close")
            if (isClose){
                // Supprimer le pin
                if (actualMarker != null){
                    actualMarker!!.closeInfoWindow()
                    mapViewFragment.overlays.remove(actualMarker)
                    markers.remove(actualMarker)
                    actualMarker = null
                }

                Thread{
                    fragment.placeModel.updatePlaces()
                }.start()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    //Lorsqu'une interaction tactile est détectée sur la carte
    private fun initOnTouchEvent(mapView: MapView, centreBtn: View, mLocationOverlay: MyLocationNewOverlay) {
        //Lors d'un movement tactile sur la map
        mapView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                mapView.controller.stopAnimation(false)
                centreBtn.visibility = View.VISIBLE // Afficher le bouton de centrage
                mLocationOverlay.disableFollowLocation() // Désactivation du suivi de la position de l'utilisateur
            }
            false
        }
    }

    // Lorsqu'un click confirmé est détecté sur la carte
    private fun initMapEventOverlay(){
        val tapOverlay = MapEventsOverlay(object: MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                for( marker in markers){
                    if (marker.isInfoWindowShown){
                        marker.closeInfoWindow()
                    }
                }
                if (actualMarker != null){
                    actualMarker!!.closeInfoWindow()
                    mapViewFragment.overlays.remove(actualMarker)

                    markers.remove(actualMarker)
                    actualMarker = null
                }else{
                    actualMarker = Marker(mapViewFragment)
                    actualMarker!!.position = p
                    actualMarker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    actualMarker!!.icon = ResourcesCompat.getDrawable(fragment.resources, R.drawable.touched_pin, null)
                    actualMarker!!.infoWindow = CustomInfoWindow(mapViewFragment, fragment.requireActivity())
                    actualMarker!!.showInfoWindow()
                    mapViewFragment.overlays.add(actualMarker)
                    mapViewFragment.controller.animateTo(p)
                    markers.add(actualMarker!!)

                    mapViewFragment.invalidate()
                }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean {
                return true
            }
        })
        mapViewFragment.overlays.add(tapOverlay)
    }

    // Lorsqu'un click sur le bouton d'alerte est détecté
    private fun initOnClickAlertBtn(alertBtn: FloatingActionButton){
        alertBtn.setOnClickListener {

            // Récupération de l'adresse à partir des coordonnées
            Thread {
                try {
                    val geoPoint : IGeoPoint = fragment.mLocationOverlay.myLocation
                    val geocoder = Geocoder(fragment.requireContext(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                    val num = addresses?.get(0)?.subThoroughfare
                    val rue = addresses?.get(0)?.thoroughfare
                    val ville = addresses?.get(0)?.locality
                    val adresse = "${if (num != null) "$num. " else ""}${if (rue != null) "$rue, " else ""}${ville ?: "Ville inconnue"}"

                    fragment.requireActivity().runOnUiThread {
                        val markAskBottomFragment = MarkAskBottomFragment()
                        markAskBottomFragment.arguments = Bundle().apply {
                            putDouble("latitude", geoPoint.latitude)
                            putDouble("longitude", geoPoint.longitude)
                            putString("adresse", adresse)
                        }
                        markAskBottomFragment.show(fragment.requireActivity().supportFragmentManager, "AlertBottomFragment")

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun initOnClickCentreBtn(centreBtn: FloatingActionButton){
        centreBtn.setOnClickListener {
            centreBtn.setOnClickListener {
                val geoPoint : IGeoPoint = fragment.mLocationOverlay.myLocation
                mapViewFragment.controller.animateTo(geoPoint, 18.5, 1000)
                fragment.mLocationOverlay.enableFollowLocation() // Activation du suivi de la position de l'utilisateur
                centreBtn.visibility = View.GONE // Masquer le bouton de centrage
            }
        }
    }

// ----------- //
// UTILITAIRES //
// ----------- //

    //Permet d'obtenir l'icône de la position de l'utilisateur
    private fun getPosIcon(): Bitmap? {
        val drawable = ResourcesCompat.getDrawable(mapViewFragment.resources ,R.drawable.pos_icon,null)
        var bitmap : Bitmap? = drawable?.toBitmap()
        bitmap = bitmap?.scale(70, 70, false) // Redimensionnement de l'icône
        return bitmap
    }

}