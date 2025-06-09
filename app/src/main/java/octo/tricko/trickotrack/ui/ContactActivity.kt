package octo.tricko.trickotrack.ui

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.model.Sweeter
import octo.tricko.trickotrack.repository.ContactRepository
import octo.tricko.trickotrack.utils.ViewUtils

class ContactActivity : AppCompatActivity() {
    private val viewUtils = ViewUtils
    private lateinit var sweeter: Sweeter
    private lateinit var repository: ContactRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sweeter = (intent.getSerializableExtra("sweeter") as? Sweeter)!!
        setContentView(R.layout.activity_contact)

        viewUtils.adapterView(window, findViewById<LinearLayout>(R.id.contact_container)) // Appel de la méthode adapterView pour adapter la vue à la barre de statut

        //repository.initialize()

    }
}