package octo.tricko.trickotrack.repository

import android.app.Activity
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.finishAffinity
import octo.tricko.trickotrack.data.TokenManager
import octo.tricko.trickotrack.ui.AccueilFragment
import octo.tricko.trickotrack.ui.MainActivity
import octo.tricko.trickotrack.ui.RegisterActivity
import octo.tricko.trickotrack.utils.RequestAPI
import octo.tricko.trickotrack.utils.ViewUtils
import org.json.JSONObject

class RegisterRepository {

    private val requestAPI : RequestAPI = RequestAPI // Instanciation de la classe RequestAPI
    private val tokenManager = TokenManager // Instanciation de la classe TokenManager
    private val viewUtils = ViewUtils // Instanciation de la classe ViewUtils

    suspend fun registerUser(nom: String, prenom: String, pseudo: String, email: String, password: String, passwordConfirm: String, errString: TextView, context: RegisterActivity): Boolean {
        // Envoie d'une requête POST à l'API pour enregistrer l'utilisateur
        val jsonObject = JSONObject()
        jsonObject.put("nom", nom)
        jsonObject.put("prenom", prenom)
        jsonObject.put("nickname", pseudo)
        jsonObject.put("email", email)
        jsonObject.put("password", password)
        jsonObject.put("password_confirmation", passwordConfirm)

        val response = requestAPI.requestPOST("register", jsonObject)
        if(response["status"] == "success") {
            //Transformation du json de la réponse en map
            val jsonResponse = JSONObject(response["reponse"].toString())
            if(jsonResponse["status"] == "error" && jsonResponse["type"] == "validation") {
                // Affichage de l'erreur
                val errors = jsonResponse.getJSONObject("errors")
                val errorMessages = mutableListOf<String>()

                if(errors.has("nickname")) {
                    errorMessages.add("Le pseudo est déjà utilisé ou invalide")
                }
                if(errors.has("email")) {
                   errorMessages.add("L'email est déjà utilisé ou invalide")
                }
                if(errors.has("password") || errors.has("password_confirmation")) {
                    errorMessages.add("Le mot de passe est invalide")
                }

                errString.text = errorMessages.joinToString("\n") // Affichage des messages d'erreur

                return false
            }else if(jsonResponse["status"] == "error" && jsonResponse["type"] == "userCreation") {
                // Affichage de l'erreur
                errString.text = "Erreur lors de la création de l'utilisateur"
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

    private fun isPasswordsValid(password: String, confirmPassword: String, registerBtn : Button): Boolean {
        // Logique de validation du mot de passe
        // Par exemple, vérifier la longueur minimale, la présence de caractères spéciaux, etc.
        var points = 0 // Initialisation du score de validation (Il en faut 12 pour valider le mot de passe)

        if(password.length >= 12) points += 1
        if(password.contains(Regex("[a-z]"))) points += 2
        if(password.contains(Regex("[A-Z]"))) points += 2
        if(password.contains(Regex("[0-9]"))) points += 3
        if(password.contains(Regex("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]"))) points += 3
        if(password == confirmPassword) points += 1

        if(points >= 12) {
            registerBtn.isEnabled = true // Le bouton est activé si le mot de passe est valide
            return true
        } else {
            registerBtn.isEnabled = false // Le bouton est désactivé si le mot de passe n'est pas valide
            return false
        }
    }

    fun requiredFieldsFilled(pseudo: String, email: String, password: String, passwordConfirm: String, registerBtn : Button) {
        // Vérification que tous les champs requis sont remplis
        var isAccepted = true
        if(pseudo.isEmpty() && email.isEmpty() && password.isEmpty() && passwordConfirm.isEmpty()) isAccepted = false;
        if(pseudo.length < 3) isAccepted = false // Le pseudo doit faire au moins 3 caractères
        if(!email.contains("@") || !email.contains(".")) isAccepted = false // L'email doit contenir un @ et un . (La vérif plus approfondie se fait côté serveur)
        if(isAccepted) {
            isPasswordsValid(password, passwordConfirm, registerBtn) // Vérification de la validité du mot de passe
        } else {
            registerBtn.isEnabled = false // Le bouton est désactivé si un champ requis est vide
        }
    }

    fun onBackPressed(window: Window, activity: RegisterActivity){
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