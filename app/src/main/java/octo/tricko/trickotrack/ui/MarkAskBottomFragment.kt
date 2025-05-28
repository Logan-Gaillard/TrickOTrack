package octo.tricko.trickotrack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.repository.MaskAskBottomRepository

class MarkAskBottomFragment : BottomSheetDialogFragment() {

    lateinit var houseFormSubmitBtn: MaterialButton
    lateinit var eventFormSubmitBtn: MaterialButton

    lateinit var houseAdresseText: EditText
    lateinit var eventAdresseText: EditText
    lateinit var titleEvent : EditText

    lateinit var tabLayout: TabLayout

    private val markAskBottomRepository = MaskAskBottomRepository(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_mark_ask_bottom, container, false)

        houseFormSubmitBtn = view.findViewById(R.id.house_form_submit_btn)
        eventFormSubmitBtn = view.findViewById(R.id.event_form_submit_btn)

        houseAdresseText = view.findViewById<TextInputLayout>(R.id.house_adresse_mark).editText!!
        eventAdresseText = view.findViewById<TextInputLayout>(R.id.event_adresse_mark).editText!!
        titleEvent = view.findViewById<TextInputLayout>(R.id.event_title_mark).editText!!

        tabLayout = view.findViewById(R.id.fmab_tab_layout)
        tabLayout.addTab(tabLayout.newTab().setText("Habitation"))
        tabLayout.addTab(tabLayout.newTab().setText("Évènement"))

        markAskBottomRepository.initEvents(view)

        houseAdresseText.setText("${arguments?.getString("adresse")}")
        eventAdresseText.setText("${arguments?.getString("adresse")}")

        return view
    }

}