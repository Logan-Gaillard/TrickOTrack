package octo.tricko.trickotrack.repository

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import octo.tricko.trickotrack.data.TokenManager
import octo.tricko.trickotrack.model.ContactModel
import octo.tricko.trickotrack.model.Sweeter
import octo.tricko.trickotrack.ui.ContactActivity
import octo.tricko.trickotrack.utils.RequestAPI

class ContactRepository(
    private val activity: ContactActivity,
    private val sweeterContact: Sweeter
) {
    private val tokenManager: TokenManager = TokenManager
    private val requestAPI: RequestAPI = RequestAPI
    private val model: ContactModel = ContactModel(activity)

    val idContact = -1 // L'id du contact que nous allons récupérer (-1 si pas de contact)

    fun initialize() {
        //Nous allons d'abord récupérer l'id du contact
        activity.lifecycleScope.launch {
            model.getContact(sweeterContact)
        }

    }
}