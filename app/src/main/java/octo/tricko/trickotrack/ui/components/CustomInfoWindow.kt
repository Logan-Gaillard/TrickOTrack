package octo.tricko.trickotrack.ui.components

import android.app.Activity
import android.content.Context
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import org.osmdroid.views.overlay.Marker
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.ui.AlertBottomFragment
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomInfoWindow(mapView: MapView, activityFragment: FragmentActivity) : InfoWindow(R.layout.custom_info_window, mapView) {
    private val activity = activityFragment;
    override fun onOpen(item: Any?) {
        val marker = item as Marker
        val position = "${String.format("%.4f", marker.position.latitude)} ${String.format("%.4f", marker.position.longitude)}"
        val locationTextView = mView.findViewById<TextView>(R.id.ciw_localisation_value)
        locationTextView.text = position
        // Tu peux ajouter ici d'autres vues ou comportements

        val signalementBtn: MaterialButton = mView.findViewById(R.id.ciw_report_btn)
        signalementBtn.setOnClickListener {
            val alertBottomFragment = AlertBottomFragment()
            alertBottomFragment.show(activity.supportFragmentManager, "AlertBottomFragment")
            //Afficher AlerBottomFragment
        }
    }

    override fun onClose() {
        // Actions à faire quand la fenêtre se ferme
    }
}