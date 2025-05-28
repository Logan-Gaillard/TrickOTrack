package octo.tricko.trickotrack.ui.components

import android.location.Geocoder
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import org.osmdroid.views.overlay.Marker
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.ui.MarkAskBottomFragment
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.util.Locale

class CIWTempMark(mapView: MapView, activityFragment: FragmentActivity) : InfoWindow(R.layout.ciw_temp_mark, mapView) {
    private val activity = activityFragment;
    override fun onOpen(item: Any?) {
        val marker = item as Marker
        val position = "${String.format("%.4f", marker.position.latitude)} ${String.format("%.4f", marker.position.longitude)}"

        val locationTextView = mView.findViewById<TextView>(R.id.ciw_localisation_value)
        locationTextView.text = position

        val signalementBtn: MaterialButton = mView.findViewById(R.id.ciw_report_btn)

        val adresseTextView = mView.findViewById<TextView>(R.id.ciw_adresse_value)
        adresseTextView.text = "Chargement de l'adresse..."

        // Récupération de l'adresse à partir des coordonnées
        Thread {
            try {
                val geocoder = Geocoder(activity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(marker.position.latitude, marker.position.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val num = addresses[0].subThoroughfare
                    val rue = addresses[0].thoroughfare
                    val ville = addresses[0].locality
                    val adresse = "${if (num != null) "$num. " else ""}${if (rue != null) "$rue, " else ""}${ville ?: "Ville inconnue"}"

                    activity?.runOnUiThread {
                        signalementBtn.isEnabled = true
                        adresseTextView.text = adresse
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        signalementBtn.setOnClickListener {
            val markAskBottomFragment = MarkAskBottomFragment()
            markAskBottomFragment.arguments = Bundle().apply {
                putDouble("latitude", marker.position.latitude)
                putDouble("longitude", marker.position.longitude)
                putString("adresse", adresseTextView.text.toString())
            }
            markAskBottomFragment.show(activity.supportFragmentManager, "AlertBottomFragment")
        }
    }

    override fun onClose() {
        // Actions à faire quand la fenêtre se ferme
    }
}