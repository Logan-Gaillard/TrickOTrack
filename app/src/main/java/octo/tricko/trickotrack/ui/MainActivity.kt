package octo.tricko.trickotrack.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import octo.tricko.trickotrack.R

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout //Déclaration de la variable drawerLayout (= la barre de navigation)
    private lateinit var navView: NavigationView //Déclaration de la variable navView (= la vue de navigation)
    private lateinit var navHeader: View //Déclaration de la variable navHeader (= l'en-tête de la vue de navigation)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //Initialisation de l'activité
        setContentView(R.layout.activity_main) // Définition du layout de l'activité (activity_main.xml)
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

        //Routage des fragments
        navView.setNavigationItemSelectedListener { menuItem -> // Lorsque l'utilisateur sélectionne un élément du menu
            when (menuItem.itemId) {
                R.id.accueil -> { // AccueilFragment
                    supportFragmentManager.beginTransaction() // Remplace le fragment actuel par AccueilFragment
                        .replace(R.id.fragment_container, AccueilFragment())
                        .commit()
                }
                R.id.settings -> { // SettingsFragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SettingsFragment())
                        .commit()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START) // Ferme la barre de navigation
            true // Indique que l'événement a été traité
        }

        closeDrawerButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START) // Action à effectuer lorsque le bouton est cliqué
        }
    }
}