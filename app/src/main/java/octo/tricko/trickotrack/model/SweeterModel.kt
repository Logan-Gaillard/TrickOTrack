package octo.tricko.trickotrack.model

import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.data.TokenManager
import octo.tricko.trickotrack.repository.InfoWindowType
import octo.tricko.trickotrack.ui.MapFragment
import octo.tricko.trickotrack.ui.components.CIWSweeter
import octo.tricko.trickotrack.utils.PhoneUtils
import octo.tricko.trickotrack.utils.RequestAPI
import org.json.JSONObject
import org.osmdroid.views.overlay.Marker
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import java.io.Serializable

class Sweeter(
    var id: Int,
    var nickname: String,
    var latitude: Double,
    var longitude: Double,
    var marker: Marker? = null
) : Serializable {
    override fun toString(): String {
        return "Sweeter(id=$id, nickname='$nickname', latitude=$latitude, longitude=$longitude)"
    }
}

class SweeterModel(fragment: MapFragment) {
    private val fragment = fragment
    private val tokenManager = TokenManager

    private val sweeters: MutableList<Sweeter> = mutableListOf()
    private var recoverOpenedMarkerId : Int = -1


    /*
        * Méthode permettant d'appeler une fonction de partage automatique de la position actuelle de l'utilisateur
        * Elle sera appelée dans le MainActivity lors de l'initialisation de l'application pour toutes les 10 minutes
     */
    fun initAutoSweeters(){
        Thread {
            Looper.prepare() // Prépare le Looper pour le thread actuel
            while (fragment.isAdded) {
                shareMyPos() // Appel de la méthode de partage de position
                Thread.sleep(600000) // Pause de 5 minutes (600000 ms)
            }
        }.start()

        Thread {
            Looper.prepare() // Prépare le Looper pour le thread actuel
            while (fragment.isAdded) {
                getConersPos() // Appel de la méthode de partage de position
                Thread.sleep(30000) // Pause de 30 secondes (30000 ms)
            }
        }.start()
    }

    /*
        * Méthode permettant de supprimer tous les coins de la carte
        * Elle sera appelée dans la méthode gestSweeter() pour afficher les coins sur la carte
     */
    private fun clearAllSweeters() {
        if(sweeters.count() > 0) {
            sweeters.forEach {
                if(it.marker != null) {
                    if(it.marker!!.infoWindow.isOpen()) {
                        recoverOpenedMarkerId = it.id // Enregistre l'ID du marqueur ouvert pour le récupérer plus tard
                        it.marker!!.infoWindow.close() // Ferme la fenêtre d'information si elle est ouverte
                    }
                    fragment.mapView.overlays.remove(it.marker)
                    Log.d("SweeterModel", "Sweeter supprimé : $it")
                }
            }
            sweeters.clear()
        } else {
            Log.d("SweeterModel", "Aucun Sweeter à supprimer.")
        }
    }

    /*
        * Méthode permettant d'ouvrir la fenêtre d'information du sweeter
        * Elle sera appelée dans la méthode gestSweeter() pour afficher les coins sur la carte
     */
    private fun openSweeterInfoWindow(marker: Marker, id: Int) {
            val result = Bundle().apply {
                putBoolean("open", true)
                putInt("id", id) // Envoi de l'ID du marquage
            }
            fragment.parentFragmentManager.setFragmentResult("SweeterInfo", result)
            fragment.infoWindowOpened.set(InfoWindowType.SWEETER, id, marker.infoWindow, marker)
            marker.showInfoWindow()
    }

    /*
        * Méthode permettant de gérer la réponse JSON contenant les sweeters récupérés depuis le serveur
        * Elle sera appelée dans la méthode getConersPos() pour afficher les coins sur la carte
     */
    private fun gestSweeter(sweetersJSON: JSONObject){

        clearAllSweeters()
        if(sweetersJSON.has("sweeters")) {
            val cornersArray = sweetersJSON.getJSONArray("sweeters")

            for (i in 0 until cornersArray.length()) {
                val cornerObject = cornersArray.getJSONObject(i)
                val id = cornerObject.getInt("id")
                val nickname = cornerObject.getString("nickname")
                val latitude = cornerObject.getDouble("latitude")
                val longitude = cornerObject.getDouble("longitude")

                // Ajoute le coin à la liste

                val originalDrawable = ContextCompat.getDrawable(fragment.requireContext(), R.mipmap.sweeters_mark)
                val scaledDrawable = (originalDrawable as BitmapDrawable).bitmap.scale(90, 90, false)


                var marker = Marker(fragment.mapView)
                marker.position = org.osmdroid.util.GeoPoint(latitude, longitude)
                marker.title = nickname
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.infoWindow = CIWSweeter(fragment.mapView, fragment, Sweeter(id, nickname, latitude, longitude))
                marker.icon = scaledDrawable.toDrawable(fragment.resources)

                marker.setOnMarkerClickListener { marker, _ ->
                    openSweeterInfoWindow(marker, id)
                    true
                }
                sweeters.add(Sweeter(id, nickname, latitude, longitude, marker))
                fragment.mapView.overlays.add(marker)

                if(recoverOpenedMarkerId == id) {
                    openSweeterInfoWindow(marker, id)
                    recoverOpenedMarkerId = -1 // Réinitialise l'ID du marqueur ouvert
                }
            }
        } else {
            Log.d("CornerModel", "Aucun sweeteur trouvé dans la réponse JSON.")
        }

    }

    /*
        * Méthode permettant de partager la position actuelle de l'utilisateur avec le serveur
        * Elle sera appelée toutes les 10 minutes par la méthode initAutoShare()
     */
    private fun shareMyPos() {
        PhoneUtils.getLocalisation(fragment.requireActivity()) { location ->
            if (location != null) {

                fragment.lifecycleScope.launch { // Lance une coroutine dans le scope du fragment
                    val token = tokenManager.getToken(fragment.requireActivity()).first() // Récupération du token
                    val jsonInputBodyData = JSONObject().apply {
                        put("latitude", location.latitude)
                        put("longitude", location.longitude)
                    }

                    RequestAPI.requestPOST("sweeter/sharepos", jsonInputBodyData, token).let { response ->
                        if (response["status"] == "success") {
                            val jsonResponse = JSONObject(response["reponse"].toString())
                            Log.d("SweeterModel", "Position partagée avec succès : ${response["reponse"]}")
                        } else {
                            // Gérer l'erreur de partage de position
                            Log.d("SweeterModel", "Erreur lors du partage de position : ${response["reponse"]}")
                        }
                    }
                }
            }
        }
    }

    /*
        * Méthode permettant de récupérer la position des corners (utilisateur de l'app) depuis le serveur
        * Elle sera appelée dans le MainActivity lors de l'initialisation de l'application
     */
    private fun getConersPos(){
        PhoneUtils.getLocalisation(fragment.requireActivity()) { location ->
            if (location != null) {

                fragment.lifecycleScope.launch { // Lance une coroutine dans le scope du fragment
                    val token = tokenManager.getToken(fragment.requireActivity()).first() // Récupération du token
                    val jsonInputBodyData = JSONObject().apply {
                        put("latitude", location.latitude)
                        put("longitude", location.longitude)
                    }

                    RequestAPI.requestPOST("sweeter/getnearby", jsonInputBodyData, token).let { response ->
                        if (response["status"] == "success") {
                            val jsonResponse = JSONObject(response["reponse"].toString())
                            gestSweeter(jsonResponse)

                        } else {
                            // Gérer l'erreur de partage de position
                            Log.d("SweeterModel", "Erreur lors du partage de position : ${response["reponse"]}")
                        }
                    }
                }
            }
        }
    }

}