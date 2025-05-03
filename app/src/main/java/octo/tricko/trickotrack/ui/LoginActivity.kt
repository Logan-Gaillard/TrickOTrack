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
import octo.tricko.trickotrack.repository.LoginRepository
import octo.tricko.trickotrack.utils.ViewUtils

class LoginActivity : AppCompatActivity() {
    private var repository : LoginRepository = LoginRepository() // Instanciation de la classe RegisterRepository
    private var viewUtils : ViewUtils = ViewUtils // Instanciation de la classe ViewUtils

    private lateinit var passwordInput : EditText; //Le champ de texte pour le mot de passe
    private lateinit var emailInput : EditText; //Le champ de texte pour le nom
    private lateinit var loginBtn : Button; //Le bouton 'se connecter'

    private lateinit var registerLoginBtn : Button; //Le bouton 's'inscrire'

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = setContentView(R.layout.activity_login) // Définition du layout de l'activité (activity_login.xml)

        viewUtils.adapterView(window, findViewById(R.id.constraint_layout)) // Appel de la méthode adapterView pour adapter la vue à la barre de statut

        emailInput = findViewById<TextInputLayout>(R.id.emailLogInput).editText!! // Récupération de la référence au champ de texte pour le nom
        passwordInput = findViewById<TextInputLayout>(R.id.passwordLogInput).editText!! // Récupération de la référence au champ de texte pour le mot de passe
        loginBtn = findViewById(R.id.login_button) // Récupération de la référence au bouton d'enregistrement
        registerLoginBtn = findViewById(R.id.registerActivity_button) // Récupération de la référence au bouton d'enregistrement

        passwordInput.doOnTextChanged { text,_,_,_ ->
            repository.requiredFieldsFilled(emailInput.text.toString(), text.toString(), loginBtn) // Vérification des champs requis
        }

        emailInput.doOnTextChanged { text,_,_,_ ->
            repository.requiredFieldsFilled(text.toString(), passwordInput.text.toString(), loginBtn) // Vérification des champs requis
        }

        loginBtn.setOnClickListener {
            val email = emailInput.text.toString() // Récupération du texte saisi dans le champ email
            val password = passwordInput.text.toString() // Récupération du texte saisi dans le champ mot de passe
            val errString = findViewById<TextView>(R.id.errorLogText) // Récupération de la référence au champ d'erreur

            lifecycleScope.launch {
                repository.loginUser(email, password, errString, this@LoginActivity) // Appel de la méthode registerUser de la classe RegisterRepository
            }
        }

        registerLoginBtn.setOnClickListener {
            viewUtils.replaceActivity(this, RegisterActivity::class.java) // Appel de la méthode replaceActivity pour changer d'activité
            finish()
        }

        //Empêcher de fermer l'activité en allant en arrière
        onBackPressedDispatcher.addCallback(this) {
            repository.onBackPressed(window, this@LoginActivity) // Appel de la méthode onBackPressed de la classe RegisterRepository
        }
    }
}