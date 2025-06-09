package octo.tricko.trickotrack.model

import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import octo.tricko.trickotrack.data.TokenManager
import octo.tricko.trickotrack.ui.ContactActivity
import octo.tricko.trickotrack.utils.RequestAPI
import org.json.JSONObject

class Messages(
    private val id: Int,
    private val nickname: String
)

class ContactModel(
    private val activity: ContactActivity
) {
    private val requestAPI: RequestAPI = RequestAPI
    private val messages: MutableList<Messages> = mutableListOf()
    private val tokenManager: TokenManager = TokenManager

    suspend fun getContact(sweeter: Sweeter) {
        messages.clear()

        activity.lifecycleScope.launch {
            val token = tokenManager.getToken(activity).first()
            val datas = JSONObject()
            datas.put("sweeter_id", sweeter.id)

            val contactRes = requestAPI.requestPOST("contact/get", JSONObject(), token.toString())
            Log.d("ContactModel", "contactRes : $contactRes")

        }.join() // Wait for the coroutine to finish

    }
}