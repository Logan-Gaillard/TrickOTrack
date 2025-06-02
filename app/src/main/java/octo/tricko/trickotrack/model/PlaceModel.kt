package octo.tricko.trickotrack.model

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.data.TokenManager
import octo.tricko.trickotrack.ui.MapFragment
import octo.tricko.trickotrack.ui.components.CIWMark
import octo.tricko.trickotrack.utils.RequestAPI
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

enum class PlaceType{ // Enum pour représenter le type du marquage
    HOUSE, // Habitation
    EVENT, // Evenement
}

class Mark(markId: Int, markMessage: String, markIsCelebrated: Boolean, markIsDecorated: Boolean, markAuthorId: Int, markAuthorNickname: String, markCreatedAt: String) {
    val id : Int = markId
    val message: String = markMessage
    val isCelebrated: Boolean = markIsCelebrated
    val isDecorated: Boolean = markIsDecorated
    val authorId: Int = markAuthorId
    val authorNickname: String = markAuthorNickname
    val createdAt: String = markCreatedAt

    override fun toString(): String {
        return "Mark(id=$id, message='$message', isCelebrated=$isCelebrated, isDecorated=$isDecorated, authorId=$authorId, authorNickname=$authorNickname, createdAt='$createdAt')"
    }
}

class Place(placeId: Int, placeIsAlert: Boolean, placeLatitude: Double, placeLongitude: Double, placeDesignation: String, placeType: PlaceType, placeAdresse: String, placeAuthorId: Int, placeAuthorNickname: String) {
    val id : Int = placeId
    val isAlert : Boolean = placeIsAlert
    val latitude : Double = placeLatitude
    val longitude : Double = placeLongitude
    val designation : String = placeDesignation
    val type : PlaceType = placeType
    val adresse : String = placeAdresse
    val authorId : Int = placeAuthorId
    val authorNickname : String = placeAuthorNickname
    val marks : MutableList<Mark> = mutableListOf<Mark>()

    override fun toString(): String {
        return "Place(id=$id, isAlert=$isAlert, latitude=$latitude, longitude=$longitude, designation='$designation', type=$type, adresse='$adresse', authorId=$authorId, authorNickname$authorNickname, marks=$marks)"
    }
}

class PlaceModel(mapFragment: MapFragment) {

    private var fragment : MapFragment = mapFragment
    private val tokenManager : TokenManager = TokenManager

    private val requestApi = RequestAPI
    val actualPlaces = mutableListOf<Place>() // Liste des marquages actuels

    private fun getPlaceType(place: Place): Drawable? {
        if(place.type == PlaceType.EVENT) return ResourcesCompat.getDrawable(fragment.resources, R.mipmap.mark_event, null)
        else if(place.type == PlaceType.HOUSE) {

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
            val now = LocalDateTime.now()

            var celebratedPts = 0.0
            var nonCelebratedPts = 0.0

            for (mark in place.marks) {
                val createdAt = LocalDateTime.parse(mark.createdAt, formatter)
                val ageInHours = Duration.between(createdAt, now).toHours().toDouble()

                val points = 1.0 / (1.0 + ageInHours)
                if (mark.isCelebrated) {
                    celebratedPts += points
                } else {
                    nonCelebratedPts += points
                }
            }

            Log.d("MarkModel", "celebratedPts: $celebratedPts, nonCelebratedPts: $nonCelebratedPts")

            if(celebratedPts >= nonCelebratedPts) { // Si le nombre de points fêtés est supérieur au nombre de points non fêtés
                return ResourcesCompat.getDrawable(fragment.resources, R.mipmap.mark_halloween, null)
            } else {
                return ResourcesCompat.getDrawable(fragment.resources, R.mipmap.mark_non_halloween, null)
            }
        }
        return ResourcesCompat.getDrawable(fragment.resources, R.mipmap.mark_non_halloween, null)
    }

    private fun putNewPlace(place: Place){
        val marker = Marker(fragment.mapView).apply {
            id = place.id.toString() // ID du marquage
            position = GeoPoint(place.latitude, place.longitude) // Position du marquage
            title = place.designation + "\n\n" + place.marks.first().message // Titre du marquage
            infoWindow = CIWMark(fragment.mapView, fragment.requireActivity(), place) // Fenêtre d'information du marquage

            icon = getPlaceType(place) // Icône du marquage en fonction de son type
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM) // Positionnement du marqueur

            setOnMarkerClickListener { marker, _ -> // Listener pour le clic sur le marquage
                marker.showInfoWindow() // Affichage de la fenêtre d'information
                val result = Bundle().apply {
                    putBoolean("open", true)
                    putInt("id", place.id) // Envoi de l'ID du marquage
                }
                fragment.parentFragmentManager.setFragmentResult("MaskInfoBottom", result)
                fragment.infoWindowOpened = marker.infoWindow // Enregistrement de la fenêtre d'information ouverte
                fragment.mapView.controller.animateTo(marker.position)
                true // Retourne true pour indiquer que l'événement a été consommé
            }
        }
        fragment.mapView.overlays.add(marker) // Ajout du marquage sur la carte
        actualPlaces.add(place) // Ajout de la place à la liste des marquages actuels
        Log.d("MarkModel", "Ajout du marquage : ${marker.id}") // Affichage de l'ajout dans les logs
    }
    private fun updatePlaces(place: Place) {
        val existingMarker = fragment.mapView.overlays.find { it is Marker && it.id == place.id.toString() } as? Marker
        if (existingMarker != null) {
            var isOpened = false
            if( existingMarker.infoWindow.isOpen) {
                isOpened = true // Vérifie si la fenêtre d'information est ouverte
                existingMarker.infoWindow.close() // Ferme la fenêtre d'information si elle est ouverte
            }
            existingMarker.position = GeoPoint(place.latitude, place.longitude) // Mise à jour de la position du marquage
            existingMarker.title = place.designation + "\n\n" + place.marks.first().message // Mise à jour du titre du marquage
            existingMarker.infoWindow = CIWMark(fragment.mapView, fragment.requireActivity(), place) // Mise à jour de la fenêtre d'information
            existingMarker.icon = getPlaceType(place) // Icône du marquage en fonction de son type

            if(isOpened) {
                existingMarker.showInfoWindow() // Réaffiche la fenêtre d'information si elle était ouverte
                fragment.infoWindowOpened = existingMarker.infoWindow // Enregistrement de la fenêtre d'information ouverte
            }
        }
    }
    private fun deletePlaces(place: Place) {
        val existingMarker = fragment.mapView.overlays.find { it is Marker && it.id == place.id.toString() } as? Marker
        if (existingMarker != null) {
            existingMarker.infoWindow.close()
            fragment.mapView.overlays.remove(existingMarker) // Suppression du marquage de la carte
            actualPlaces.removeIf { it.id == place.id } // Suppression de la place de la liste des marquages actuels
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

                        gestPlaces(marksArray)// Appel de la fonction pour gérer les données
                    }

                } else { // Si la réponse est incorrecte
                    Log.d(fragment.context.toString(), "Erreur : ${response}") // Affichage de l'erreur dans les logs
                    if(response["status"] == "alreadyInRequest") return@launch
                    Toast.makeText(fragment.context, "Erreur : ${response["message"]}", Toast.LENGTH_SHORT).show() // Affichage d'un message d'erreur
                }
            }
            Thread.sleep(15000) // Pause de x secondes entre chaque appel
        }catch (e: Exception){ // Gestion des exceptions
            Log.e("MarkModel", "Erreur dans la mise à jour des marquages: ${e.message}")
        }
    }

    private fun gestPlaces(placesReponses: JSONObject){
        val newPlaces = mutableListOf<Place>() // Liste des nouveaux marquages

        if (placesReponses.has("places")) {
            val reponses = placesReponses.getJSONObject("places") // Récupération du tableau de marquages
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
                    val placeAuthorNickname = placeJSONObject.getString("author_nickname")

                    val place = Place(placeId, placeIsAlert, placeLatitude, placeLongitude, placeDesignation, placeType, placeAdresse, placeAuthorId, placeAuthorNickname)

                    val marksResponse = placeJSONObject.getJSONArray("marks")
                    if (marksResponse.length() > 0) {
                        for (j in 0 until marksResponse.length()) {
                            val markJSONObject = marksResponse.getJSONObject(j)
                            val markId = markJSONObject.getInt("id")
                            val markMessage = markJSONObject.getString("message")
                            val markIsCelebrated = markJSONObject.getInt("is_celebrated") == 1
                            val markIsDecorated = markJSONObject.getInt("is_decorated") == 1
                            val markAuthorId = markJSONObject.getInt("author_id")
                            val markCreatedAt = markJSONObject.getString("created_at")
                            val markAuthorNickname = markJSONObject.getString("author_nickname")

                            val mark = Mark(markId, markMessage, markIsCelebrated, markIsDecorated, markAuthorId, markAuthorNickname, markCreatedAt)
                            place.marks.add(mark)
                        }
                    }

                    //Ajout de la place à la liste des nouveaux marquages
                    newPlaces.add(place)
                }
            }
        }

        //Faire le tri des nouveaux marquages
        //Si la place n'est plus présente dans la liste : Dans NeedDeletePlaces
        //Si la place est nouveau : Dans NeedAddPlaces
        //Si la place y est déjà mais qu'il y a une différence de marquages : Dans needUpdatePlaces.
        val deletePlaceList : MutableList<Place> = mutableListOf<Place>() // Liste des marquages à supprimer
        actualPlaces.forEach { existingPlace ->
            if (newPlaces.none { it.id == existingPlace.id }) {
                newPlaces.removeIf { it.id == existingPlace.id }
                deletePlaceList.add(existingPlace)
            }
        }
        deletePlaceList.forEach { placeToDelete ->
            deletePlaces(placeToDelete) // Si la place n'existe plus, on la supprime de la carte
        }

        newPlaces.forEach { newPlace ->
            val existingPlace = actualPlaces.find { it.id == newPlace.id }
            if (existingPlace == null) {
                putNewPlace(newPlace) // Si la place n'existe pas, on l'ajoute à la carte
            } else {
                // Vérifier si la taille des listes de marques est différente
                // OU si une marque existante n'est plus présente dans la nouvelle liste de marques (basé sur l'ID)
                // OU si une marque correspondante a un message différent.
                val marksChanged = existingPlace.marks.size != newPlace.marks.size ||
                        existingPlace.marks.any { existingMark ->
                            newPlace.marks.none { it.id == existingMark.id } || // Marque supprimée
                            newPlace.marks.any { newMark -> newMark.id == existingMark.id && newMark.message != existingMark.message } // Message différent
                        }
                if (marksChanged) {
                    updatePlaces(newPlace) // Mise à jour du marquage
                }
            }
        }

    }

}