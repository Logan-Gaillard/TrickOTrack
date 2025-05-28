package octo.tricko.trickotrack.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import octo.tricko.trickotrack.R
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import com.google.android.material.floatingactionbutton.FloatingActionButton
import octo.tricko.trickotrack.model.PlaceModel
import octo.tricko.trickotrack.repository.MapRepository

class MapFragment : Fragment() {

    lateinit var mapView: MapView
    lateinit var mLocationOverlay: MyLocationNewOverlay // Déclaration de la variable mLocatationOverlay (= la localisation de l'utilisateur)
    lateinit var mRotationGestureOverlay: RotationGestureOverlay // Déclaration de la variable mRotationGestureOverlay (= la rotation de la carte)

    private var mapRepository: MapRepository = MapRepository(this) // Instanciation de la classe MapRepository

    lateinit var centreBtn: FloatingActionButton // Déclaration de la variable centreBtn (= le bouton de centrage de la carte)
    lateinit var alertBtn: FloatingActionButton

    var placeModel: PlaceModel = PlaceModel(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        //Si une préférence à été sauvegardée, on la charge sinon on en crée une nouvelle
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("osmdroid", 0)
        Configuration.getInstance().load(context, sharedPreferences) // Chargement de la configuration de la carte
        Configuration.getInstance().userAgentValue = requireContext().packageName // Définition de l'agent utilisateur pour la carte

        mapRepository.initMap(view) // Initialisation de la carte

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