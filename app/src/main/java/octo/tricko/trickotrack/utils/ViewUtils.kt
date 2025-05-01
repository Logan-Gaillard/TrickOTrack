package octo.tricko.trickotrack.utils

import android.view.View
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class ViewUtils {
    fun adapterView(window: Window, view: View){
        WindowCompat.setDecorFitsSystemWindows(window, false)// permet d'afficher les element sous la barre de status
        ViewCompat.setOnApplyWindowInsetsListener(view){ v, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars()) //On récupère les insets de la barre de status
            v.updatePadding(top = systemBarsInsets.top) // On met à jour le padding du haut de la vue
            v.updatePadding(bottom = systemBarsInsets.bottom) // On met à jour le padding du bas de la vue
            insets
        }
    }
}