package octo.tricko.trickotrack.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import octo.tricko.trickotrack.model.Sweeter

object ViewUtils {
    fun adapterView(window: Window, view: View){
        WindowCompat.setDecorFitsSystemWindows(window, false)// permet d'afficher les element sous la barre de status
        ViewCompat.setOnApplyWindowInsetsListener(view){ v, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars()) //On récupère les insets de la barre de status
            v.updatePadding(top = systemBarsInsets.top) // On met à jour le padding du haut de la vue
            v.updatePadding(bottom = systemBarsInsets.bottom) // On met à jour le padding du bas de la vue
            insets
        }
    }

    fun openActivity(context: Context, activity: Class<*>, extras: Map<String, Any>? = null){
        val intent = Intent(context, activity)
        extras?.forEach { (key, value) ->
            when (value) {
                is String -> intent.putExtra(key, value)
                is Int -> intent.putExtra(key, value)
                is Boolean -> intent.putExtra(key, value)
                is Double -> intent.putExtra(key, value)
                is Float -> intent.putExtra(key, value)
                is Long -> intent.putExtra(key, value)
                is Sweeter -> intent.putExtra(key, value)
                // Ajoutez d'autres types si nécessaire
            }
        }
        context.startActivity(intent)
    }

    fun replaceActivity(context: Activity, activity: Class<*>, extras: Map<String, Any>? = null) {
        val intent = Intent(context, activity)
        extras?.forEach { (key, value) ->
            when (value) {
                is String -> intent.putExtra(key, value)
                is Int -> intent.putExtra(key, value)
                is Boolean -> intent.putExtra(key, value)
                is Double -> intent.putExtra(key, value)
                is Float -> intent.putExtra(key, value)
                is Long -> intent.putExtra(key, value)
                // Ajoutez d'autres types si nécessaire
            }
        }
        context.startActivity(intent)
        context.finish() // Termine l'activité actuelle
    }

}