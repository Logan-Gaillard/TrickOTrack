package octo.tricko.trickotrack.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.utils.RequestAPI
import org.json.JSONArray
import org.json.JSONObject

class SettingsFragment : Fragment() {

    private val resquestAPI = RequestAPI

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        lifecycleScope.launch {
            //val reponse = resquestAPI.requestGET("test")
            val jsonArray = JSONObject().put("Test1", "Ceci est un test1")

            val reponse = resquestAPI.requestPOST("test", jsonArray, null)
            Log.d("API Response", reponse.toString())
        }

        return view
    }
}
