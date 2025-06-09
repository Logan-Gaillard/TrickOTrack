package octo.tricko.trickotrack.ui.components

import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.model.Sweeter
import octo.tricko.trickotrack.ui.ContactActivity
import octo.tricko.trickotrack.ui.MapFragment
import octo.tricko.trickotrack.utils.ViewUtils
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CIWSweeter(mapView: MapView, activityFragment: MapFragment, sweeter: Sweeter) : InfoWindow(R.layout.ciw_sweeter, mapView) {
    private val activity: MapFragment = activityFragment
    private val sweeter: Sweeter = sweeter

    private lateinit var btn_contact: android.widget.Button

    private val viewUtils = ViewUtils

    override fun onOpen(item: Any?) {
        if (item is Marker) {
            mView.findViewById<android.widget.TextView>(R.id.ciw_sweeter_nickname).text = sweeter.nickname
            btn_contact = mView.findViewById<android.widget.Button>(R.id.ciw_sweeter_contact)
            btn_contact.isEnabled = false
            btn_contact.setOnClickListener {

                viewUtils.openActivity(activity.requireContext(), ContactActivity::class.java, mapOf("sweeter" to sweeter))

            }
        }
    }

    override fun onClose() {
    }
}