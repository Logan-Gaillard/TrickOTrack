package octo.tricko.trickotrack.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.data.TokenManager
import octo.tricko.trickotrack.data.TokenState

class Launcher : AppCompatActivity() {
    private val tokenManager = TokenManager // Instanciation de la classe TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            tokenManager.getTokenState(this@Launcher).let { tokenState ->
                if(tokenState == TokenState.MISSING) {
                    startActivity(Intent(this@Launcher, RegisterActivity::class.java)) // Démarre l'activité RegisterActivity
                    finish() // Termine l'activité MainActivity
                    return@launch
                } else if(tokenState == TokenState.INVALID) {
                    //startActivity(Intent(this@MainActivity, LoginActivity::class.java)) // Démarre l'activité LoginActivity
                    //finish() // Termine l'activité MainActivity
                    startActivity(Intent(this@Launcher, RegisterActivity::class.java)) // Démarre l'activité RegisterActivity
                    finish() // Termine l'activité MainActivity
                }else{
                    startActivity(Intent(this@Launcher, MainActivity::class.java)) // Démarre l'activité RegisterActivity
                    finish() // Termine l'activité MainActivity
                }
            }
        }
    }
}