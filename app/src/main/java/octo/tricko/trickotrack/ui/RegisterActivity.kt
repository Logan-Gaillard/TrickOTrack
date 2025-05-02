package octo.tricko.trickotrack.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.repository.RegisterRepository
import octo.tricko.trickotrack.utils.ViewUtils

class RegisterActivity : AppCompatActivity() {

    private var repository : RegisterRepository = RegisterRepository() // Instanciation de la classe RegisterRepository
    private var viewUtils : ViewUtils = ViewUtils // Instanciation de la classe ViewUtils

    private lateinit var registerBtn : Button; //Le bouton 'créer mon compte'

    private lateinit var nicknameRegInput : EditText; //Le champ de texte pour le pseudo
    private lateinit var courrielRegInput : EditText; //Le champ de texte pour le nom
    private lateinit var passwordRegInput : EditText; //Le champ de texte pour le mot de passe
    private lateinit var passwordConfirmRegInput : EditText; //Le champ de texte pour la confirmation du mot de passe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Définition du layout de l'activité (activity_register.xml)

        val welcomeText = findViewById<TextView>(R.id.welcome_txt)
        val appName = getString(R.string.app_name) // Récupère le texte défini dans strings.xml
        welcomeText.text = "Bienvenue sur $appName" // Concaténation du texte

        viewUtils.adapterView(window, findViewById(R.id.constraint_layout)) // Appel de la méthode adapterView pour adapter la vue à la barre de statut
        nicknameRegInput = findViewById<TextInputLayout>(R.id.nicknameRegInput).editText!! // Récupération de la référence au champ de texte pour le pseudo
        courrielRegInput = findViewById<TextInputLayout>(R.id.emailRegInput).editText!! // Récupération de la référence au champ de texte pour le nom
        passwordRegInput = findViewById<TextInputLayout>(R.id.passwordRegInput).editText!! // Récupération de la référence au champ de texte pour le mot de passe
        passwordConfirmRegInput = findViewById<TextInputLayout>(R.id.passwordConfirmRegInput).editText!! // Récupération de la référence au champ de texte pour la confirmation du mot de passe

        registerBtn = findViewById(R.id.register_button) // Récupération de la référence au bouton d'enregistrement

        registerBtn.setOnClickListener {
            val nom = findViewById<TextInputLayout>(R.id.nomRegInput).editText?.text.toString() // Récupération du texte saisi dans le champ nom
            val prenom = findViewById<TextInputLayout>(R.id.prenomRegInput).editText?.text.toString() // Récupération du texte saisi dans le champ prénom
            val pseudo = nicknameRegInput.text.toString() // Récupération du texte saisi dans le champ pseudo
            val email = courrielRegInput.text.toString() // Récupération du texte saisi dans le champ email
            val errString = findViewById<TextView>(R.id.errorRegText) // Récupération de la référence au champ d'erreur

            lifecycleScope.launch {
                repository.registerUser(nom, prenom, pseudo, email, passwordRegInput.text.toString(), passwordRegInput.text.toString(), errString, this@RegisterActivity) // Appel de la méthode registerUser de la classe RegisterRepository
            }
        }

        //Lorsque le pseudo est modifié, on vérifie si le pseudo est valide
        nicknameRegInput.doOnTextChanged { text,_,_,_ ->
            repository.requiredFieldsFilled(text.toString(), courrielRegInput.text.toString(), passwordRegInput.text.toString(), passwordConfirmRegInput.text.toString(), registerBtn) // Appel de la méthode isPasswordsValid de la classe RegisterRepository
        }
        //Lorsque le nom est modifié, on vérifie si le nom est valide
        courrielRegInput.doOnTextChanged { text,_,_,_ ->
            repository.requiredFieldsFilled(nicknameRegInput.text.toString(), text.toString(), passwordRegInput.text.toString(), passwordConfirmRegInput.text.toString(), registerBtn) // Appel de la méthode isPasswordsValid de la classe RegisterRepository
        }
        //Lorsque le mot de passe est modifié, on vérifie si le mot de passe est valide
        passwordRegInput.doOnTextChanged { text,_,_,_ ->
            repository.requiredFieldsFilled(nicknameRegInput.text.toString(), courrielRegInput.text.toString(), text.toString(), passwordConfirmRegInput.text.toString(), registerBtn) // Appel de la méthode isPasswordsValid de la classe RegisterRepository
        }
        //Lorsque le mot de passe de confirmation est modifié, on vérifie si le mot de passe est valide
        passwordConfirmRegInput.doOnTextChanged { text,_,_,_ ->
            repository.requiredFieldsFilled(nicknameRegInput.text.toString(), courrielRegInput.text.toString(), passwordRegInput.text.toString(), text.toString(), registerBtn) // Appel de la méthode isPasswordsValid de la classe RegisterRepository
        }

        //Empêcher de fermer l'activité en allant en arrière
        onBackPressedDispatcher.addCallback(this) {
            repository.onBackPressed(window, this@RegisterActivity) // Appel de la méthode onBackPressed de la classe RegisterRepository
        }
    }
}