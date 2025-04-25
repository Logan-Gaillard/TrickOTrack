package octo.tricko.trickotrack.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import octo.tricko.trickotrack.R

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val openDrawerButton = findViewById<Button>(R.id.open_drawer_button)

        openDrawerButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //Routage des fragments
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.accueil -> {
                    // Tu affiches un fragment ou fais une action ici
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AccueilFragment())
                        .commit()
                }
                R.id.settings -> {
                    // Autre action
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SettingsFragment())
                        .commit()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}