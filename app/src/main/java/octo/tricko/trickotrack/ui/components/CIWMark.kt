package octo.tricko.trickotrack.ui.components

import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.model.Place
import octo.tricko.trickotrack.model.PlaceType
import octo.tricko.trickotrack.ui.MarkAskBottomFragment
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

data class ViewMark(val pseudo: String, val message: String, val celebrated: Boolean, val decorated: Boolean)

class CIWMark(mapView: MapView, activityFragment: FragmentActivity, place: Place) : InfoWindow(R.layout.ciw_mark, mapView) {
    private val activity = activityFragment
    private val place = place

    override fun onOpen(item: Any?) {
        if (item is Marker) {

            val markBtn : Button = mView.findViewById<Button>(R.id.ciw_mark_report_btn)

            if(place.type == PlaceType.EVENT){
                mView.findViewById<TextView>(R.id.ciw_designation).text = place.designation
                mView.findViewById<TextView>(R.id.ciw_mark_adresse_value).text = place.adresse

            }else{
                mView.findViewById<TextView>(R.id.ciw_designation).text = place.adresse
                mView.findViewById<TextView>(R.id.ciw_mark_adresse_text).visibility = TextView.GONE
                mView.findViewById<TextView>(R.id.ciw_mark_adresse_value).visibility = TextView.GONE
            }

            val scrollLinear = view.findViewById<LinearLayout>(R.id.ciw_mark_scroll_linear)
            scrollLinear.removeAllViews()

            val listViewMark = place.marks.map { mark ->
                ViewMark(pseudo = mark.authorNickname, message = mark.message, celebrated = mark.isCelebrated, decorated = mark.isDecorated)
            }

            for (viewMark in listViewMark) {
                val textPseudoView = TextView(activity).apply {
                    text = viewMark.pseudo
                    textSize = 18f
                    setTypeface(null, Typeface.BOLD)
                    setPadding(8, 16, 8, 16)
                }
                val textMessageView = TextView(activity).apply {
                    text = viewMark.message
                    visibility = if (viewMark.message == "null") TextView.GONE else TextView.VISIBLE
                    textSize = 16f
                    setPadding(8, 0, 8, 16)
                }
                val statusFete = TextView(activity).apply {
                    when(viewMark.celebrated){
                        true -> {
                            when(viewMark.decorated){
                                true -> {
                                    text = "Fêté et décorée"
                                }
                                false -> {
                                    text = "Fêté mais pas décorée"
                                }
                            }
                        }
                        false -> {
                            when(viewMark.decorated){
                                true -> {
                                    text = "Ne fête pas mais décorée"
                                }
                                false -> {
                                    text = "Ne fête pas et pas décorée"
                                }
                            }
                        }
                    }
                    setTextColor(if (viewMark.celebrated) activity.getColor(R.color.green) else activity.getColor(R.color.red))
                    visibility = if (place.type == PlaceType.EVENT) TextView.GONE else TextView.VISIBLE
                    textSize = 14f
                    setPadding(8, 0, 8, 16)
                }
                scrollLinear.addView(textPseudoView)
                scrollLinear.addView(textMessageView)
                scrollLinear.addView(statusFete)
            }

            markBtn.setOnClickListener {
                val markAskBottomFragment = MarkAskBottomFragment()
                markAskBottomFragment.arguments = Bundle().apply {
                    putDouble("latitude", item.position.latitude)
                    putDouble("longitude", item.position.longitude)
                    putString("adresse", place.adresse)
                }
                markAskBottomFragment.show(activity.supportFragmentManager, "AlertBottomFragment")
            }

            markBtn.isEnabled = true;

        }
    }

    override fun onClose() {
    }
}