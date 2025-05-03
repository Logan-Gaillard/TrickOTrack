package octo.tricko.trickotrack.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.data.TokenManager
import octo.tricko.trickotrack.data.TokenState
import octo.tricko.trickotrack.utils.ViewUtils

import octo.tricko.trickotrack.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel = MainViewModel()
    private val viewUtils = ViewUtils
    private val tokenManager = TokenManager // Instanciation de la classe TokenManager

    private lateinit var drawerLayout: DrawerLayout //Déclaration de la variable drawerLayout (= la barre de navigation)
    private lateinit var navView: NavigationView //Déclaration de la variable navView (= la vue de navigation)
    private lateinit var navHeader: View //Déclaration de la variable navHeader (= l'en-tête de la vue de navigation)
    //private lateinit var navFooter: View //Déclaration de la variable navFooter (= le pied de page de la vue de navigation)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //Initialisation de l'activité
        setContentView(R.layout.activity_main) // Définition du layout de l'activité (activity_main.xml)

        viewUtils.adapterView(window, findViewById<ConstraintLayout>(R.id.main_container)) // Appel de la méthode adapterView pour adapter la vue à la barre de statut

        if (!viewModel.checkPermission(this)) { // Vérification de la permission de localisation
            viewModel.requestPosition(this) // Demande de permission de localisation.
        }

        supportFragmentManager.beginTransaction() // Afficher par défaut le fragment AccueilFragment
            .replace(R.id.fragment_container, AccueilFragment())
            .commit()

        drawerLayout = findViewById(R.id.drawer_layout) // Récupération de la référence à la barre de navigation
        navView = findViewById(R.id.nav_view) // Récupération de la référence à la vue de navigation
        navHeader = navView.getHeaderView(0) // Récupération de la référence à l'en-tête de la vue de navigation

        val openDrawerButton = findViewById<Button>(R.id.open_drawer_button) // Récupération de la référence au bouton d'ouverture de la barre de navigation
        val closeDrawerButton = navHeader.findViewById<Button>(R.id.close_drawer_button) // Récupération de la référence au bouton de fermeture de la barre de navigation

        openDrawerButton.setOnClickListener { // Action à effectuer lorsque le bouton est cliqué
            drawerLayout.openDrawer(GravityCompat.START) // Ouverture de la barre de navigation
        }
        closeDrawerButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START) // Action à effectuer lorsque le bouton est cliqué
        }

        //Temporaire
        openDrawerButton.setOnLongClickListener {
            //Remplacer l'activité par RegisterActivity
            startActivity(Intent(this, RegisterActivity::class.java)) // Démarre l'activité RegisterActivity
            finish()
            true // Indique que l'événement a été traité
        }

        //Routage des fragments
        navView.setNavigationItemSelectedListener { menuItem -> // Lorsque l'utilisateur sélectionne un élément du menu
            viewModel.route(menuItem, supportFragmentManager) // Appel de la méthode route du ViewModel pour changer de fragment
            drawerLayout.closeDrawer(GravityCompat.START) // Ferme la barre de navigation
            true // Indique que l'événement a été traité
        }
    }
}