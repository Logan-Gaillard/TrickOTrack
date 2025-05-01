package octo.tricko.trickotrack.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.utils.ViewUtils

class RegisterActivity : AppCompatActivity() {

    private var viewUtils : ViewUtils = ViewUtils() // Instanciation de la classe ViewUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Définition du layout de l'activité (activity_register.xml)

        val welcomeText = findViewById<TextView>(R.id.welcome_txt)
        val appName = getString(R.string.app_name) // Récupère le texte défini dans strings.xml
        welcomeText.text = "Bienvenue sur $appName" // Concaténation du texte


        viewUtils.adapterView(window, findViewById(R.id.constraint_layout)) // Appel de la méthode adapterView pour adapter la vue à la barre de statut

        //Empêcher de fermer l'activité en allant en arrière
        onBackPressedDispatcher.addCallback(this) {
            // Ne rien faire
            AlertDialog.Builder(window.context)
                .setTitle("Quitter l'application ?")
                .setMessage("Êtes-vous sûr de vouloir quitter l'application ?")
                .setPositiveButton("Oui") { _, _ ->
                    //Fermer l'application
                    finishAffinity() // Action à effectuer lorsque le bouton est cliqué
                }
                .setNegativeButton("Non") { dialog, _ ->
                    dialog.dismiss() // Action à effectuer lorsque le bouton est cliqué
                }
                .show()
        }

    }

}