package octo.tricko.trickotrack.model

import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import octo.tricko.trickotrack.data.TokenManager
import octo.tricko.trickotrack.ui.MapFragment
import octo.tricko.trickotrack.utils.RequestAPI
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

enum class PlaceType{ // Enum pour représenter le type du marquage
    HOUSE, // Habitation
    EVENT, // Evenement
}

class Mark(markId: Int, markMessage: String, markIsCelebrated: Boolean, markIsDecorated: Boolean, markAuthorId: Int, markCreatedAt: String) {
    val id : Int = markId
    val message: String = markMessage
    val isCelebrated: Boolean = markIsCelebrated
    val isDecorated: Boolean = markIsDecorated
    val authorId: Int = markAuthorId
    val createdAt: String = markCreatedAt

    override fun toString(): String {
        return "Mark(id=$id, message='$message', isCelebrated=$isCelebrated, isDecorated=$isDecorated, authorId=$authorId, createdAt='$createdAt')"
    }
}

class Place(placeId: Int, placeIsAlert: Boolean, placeLatitude: Double, placeLongitude: Double, placeDesignation: String, placeType: PlaceType, placeAdresse: String, placeAuthorId: Int) {
    val id : Int = placeId
    val isAlert : Boolean = placeIsAlert
    val latitude : Double = placeLatitude
    val longitude : Double = placeLongitude
    val designation : String = placeDesignation
    val type : PlaceType = placeType
    val adresse : String = placeAdresse
    val authorId : Int = placeAuthorId
    val marks : MutableList<Mark> = mutableListOf<Mark>()

    override fun toString(): String {
        return "Place(id=$id, isAlert=$isAlert, latitude=$latitude, longitude=$longitude, designation='$designation', type=$type, adresse='$adresse', authorId=$authorId, marks=$marks)"
    }
}

class PlaceModel(mapFragment: MapFragment) {

    private var fragment : MapFragment = mapFragment
    private val tokenManager : TokenManager = TokenManager

    private val requestApi = RequestAPI
    private val newPlaces = mutableListOf<Place>() // Liste des nouveaux marquages
    val actualPlaces = mutableListOf<Place>() // Liste des marquages actuels

    fun updateMarksOnMap() {
        Log.d("MarkModel", "actualPlaces : ${actualPlaces}\n")
        Log.d("MarkModel", "newPlaces : ${newPlaces}\n")
        for (place in newPlaces) { // Parcours toutes les places
            if (actualPlaces.none { it.id == place.id }) { // Si la place n'est pas présente
                val marker = Marker(fragment.mapView).apply {
                    id = place.id.toString() // ID du marquage
                    position = GeoPoint(place.latitude, place.longitude) // Position du marquage
                    title = place.designation + "\n\n" + place.marks.first().message // Titre du marquage
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) // Positionnement du marqueur
                }
                fragment.mapView.overlays.add(marker) // Ajout du marquage sur la carte
                actualPlaces.add(place) // Ajout de la place à la liste des marquages actuels
                Log.d("MarkModel", "Ajout du marquage : ${marker.id}") // Affichage de l'ajout dans les logs
            }
        }

        var deleteMarks = mutableListOf<Marker>() // Liste des marquages à supprimer
        for(place in actualPlaces){
            if(newPlaces.none { it.id == place.id }){ // Si la place n'est pas présente
                val marker = fragment.mapView.overlays.find { it is Marker && (it as Marker).id == place.id.toString() } as? Marker
                if(marker != null){
                    deleteMarks.add(marker)
                }
            }
        }

        for (marker in deleteMarks) { // Parcours toutes les places
            fragment.mapView.overlays.remove(marker) // Suppression du marquage sur la carte
            actualPlaces.removeIf { it.id.toString() == marker.id } // Suppression de la place de la liste des marquages actuels
            Log.d("MarkModel", "Suppression du marquage : ${marker.id}") // Affichage de la suppression dans les logs
        }
    }

    fun initAutoMarkUpdater(){
        Thread {
            Looper.prepare() // Prépare le Looper pour le thread actuel
            while (fragment.isAdded) { // Tant que le fragment est présent
                Thread.sleep(15000) // Pause de x secondes entre chaque appel
                updatePlaces();
            }
        }.start()
    }

    fun updatePlaces() {
        try { // try pour la gestion des exceptions
            fragment.lifecycleScope.launch { // Lance une coroutine dans le scope du fragment
                if (!fragment.isAdded || fragment.context == null) return@launch // Si le fragment n'est pas présent ou le contexte est nul, on sort de la coroutine sinon c'est erreur

                val token = tokenManager.getToken(fragment.requireContext()).first() // Récupération du token
                val jsonObject = JSONObject().apply { // Récupération des coordonnées d'un marquage
                    put("latitude", fragment.mapView.mapCenter.latitude)
                    put("longitude", fragment.mapView.mapCenter.longitude)
                }

                val response = requestApi.requestPOST("places/get", jsonObject, token.toString()) // Appel de l'API pour récupérer les marquages
                if (response["code"] == 200) { // Si la réponse est correcte
                    Log.d("MarkModel", "Réponse de l'API : ${response["reponse"]}")

                    val reponse = response["reponse"]
                    if(reponse != null) {
                        val marksArray = JSONObject(reponse.toString()) // Récupération du tableau de marquages

                        if(marksArray.getBoolean("hasPlaces")) {
                            gestPlaces(marksArray)// Appel de la fonction pour gérer les données
                        }else{
                            newPlaces.clear()
                            actualPlaces.clear()
                        }
                        updateMarksOnMap() // Appel de la fonction pour mettre à jour les marquages sur la carte
                    }

                } else { // Si la réponse est incorrecte
                    Log.d(fragment.context.toString(), "Erreur : ${response}") // Affichage de l'erreur dans les logs
                    Toast.makeText(fragment.context, "Erreur : ${response["message"]}", Toast.LENGTH_SHORT).show() // Affichage d'un message d'erreur
                }
            }
            Thread.sleep(15000) // Pause de x secondes entre chaque appel
        }catch (e: Exception){ // Gestion des exceptions
            Log.e("MarkModel", "Erreur dans la mise à jour des marquages: ${e.message}")
        }
    }

    private fun gestPlaces(placesReponses: JSONObject){
        val reponses = placesReponses.getJSONObject("places") // Récupération du tableau de marquages

        newPlaces.clear()
        for (i in reponses.keys()) { // Parcours toutes les places
            val placeJSONObject = reponses.getJSONObject(i)

            if (placeJSONObject.length() > 0) {
                val placeId = placeJSONObject.getInt("id")
                val placeIsAlert = placeJSONObject.getInt("is_alert") == 1
                val placeLatitude = placeJSONObject.getDouble("latitude")
                val placeLongitude = placeJSONObject.getDouble("longitude")
                val placeDesignation = placeJSONObject.getString("designation")
                val placeType = if (placeJSONObject.getInt("is_house") == 1) PlaceType.HOUSE else PlaceType.EVENT
                val placeAdresse = placeJSONObject.getString("adresse")
                val placeAuthorId = placeJSONObject.getInt("author_id")

                val place = Place(placeId, placeIsAlert, placeLatitude, placeLongitude, placeDesignation, placeType, placeAdresse, placeAuthorId)

                val marksResponse = placeJSONObject.getJSONArray("marks")
                if(marksResponse.length() > 0){
                    for (j in 0 until marksResponse.length()) {
                        val markJSONObject = marksResponse.getJSONObject(j)
                        val markId = markJSONObject.getInt("id")
                        val markMessage = markJSONObject.getString("message")
                        val markIsCelebrated = markJSONObject.getInt("is_celebrated") == 1
                        val markIsDecorated = markJSONObject.getInt("is_decorated") == 1
                        val markAuthorId = markJSONObject.getInt("author_id")
                        val markCreatedAt = markJSONObject.getString("created_at")

                        val mark = Mark(markId, markMessage, markIsCelebrated, markIsDecorated, markAuthorId, markCreatedAt)
                        place.marks.add(mark)
                    }
                }

                //Ajout de la place à la liste des nouveaux marquages
                newPlaces.add(place)
            }
        }

    }

}