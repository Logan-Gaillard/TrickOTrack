package octo.tricko.trickotrack.repository

import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.finishAffinity
import octo.tricko.trickotrack.data.TokenManager
import octo.tricko.trickotrack.ui.LoginActivity
import octo.tricko.trickotrack.ui.MainActivity
import octo.tricko.trickotrack.utils.RequestAPI
import octo.tricko.trickotrack.utils.ViewUtils
import org.json.JSONObject

class LoginRepository {

    private val requestAPI : RequestAPI = RequestAPI // Instanciation de la classe RequestAPI
    private val tokenManager = TokenManager // Instanciation de la classe TokenManager
    private val viewUtils = ViewUtils // Instanciation de la classe ViewUtils

    suspend fun loginUser(email: String, password: String, errString: TextView, context: LoginActivity): Boolean {
        // Envoie d'une requête POST à l'API pour enregistrer l'utilisateur
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)

        val response = requestAPI.requestPOST("login", jsonObject)
        if(response["status"] == "success") {
            //Transformation du json de la réponse en map
            val jsonResponse = JSONObject(response["reponse"].toString())
            if(jsonResponse["status"] == "error" && jsonResponse["type"] == "validation") {
                // Affichage de l'erreur
                val errors = jsonResponse.getJSONObject("errors")

                if(errors.has("email") || errors.has("password")) {
                    errString.text = "L'email ou le mot de passe est invalide"
                }

                return false
            }else if(jsonResponse["status"] == "error" && jsonResponse["type"] == "userCreation") {
                // Affichage de l'erreur
                errString.text = "Erreur lors de la connexion !"
                return false
            }else if(jsonResponse["status"] == "success"){
                // Affichage du message de succès
                errString.text = ""
                tokenManager.saveToken(context, jsonResponse["token"].toString()).let {
                    viewUtils.replaceActivity(context, MainActivity::class.java)
                }
                return true
            }
        }else if(response["status"] == "error" && response["type"] == "ConnectException"){
            Toast.makeText(context, "Une erreur de communication est survenue", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    fun requiredFieldsFilled(email: String, password: String, loginBtn : Button) {
        // Vérification que tous les champs requis sont remplis
        var isAccepted = true
        if(email.isEmpty() && password.isEmpty() && password.length < 12) isAccepted = false;
        if(!email.contains("@") || !email.contains(".")) isAccepted = false // L'email doit contenir un @ et un . (La vérif plus approfondie se fait côté serveur)
        loginBtn.isEnabled = isAccepted // Le bouton est activé si tous les champs sont remplis
    }

    fun onBackPressed(window: Window, activity: LoginActivity){
        AlertDialog.Builder(window.context)
            .setTitle("Quitter l'application ?")
            .setMessage("Êtes-vous sûr de vouloir quitter l'application ?")
            .setPositiveButton("Oui") { _, _ ->
                //Fermer l'application
                finishAffinity(activity) // Action à effectuer lorsque le bouton est cliqué
            }
            .setNegativeButton("Non") { dialog, _ ->
                dialog.dismiss() // Action à effectuer lorsque le bouton est cliqué
            }
            .show()
    }

}