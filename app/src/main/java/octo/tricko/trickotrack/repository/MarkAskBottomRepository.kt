package octo.tricko.trickotrack.repository

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import octo.tricko.trickotrack.R
import octo.tricko.trickotrack.data.TokenManager
import octo.tricko.trickotrack.ui.MarkAskBottomFragment
import octo.tricko.trickotrack.utils.RequestAPI
import org.json.JSONObject

class MarkAskBottomRepository(markAskBottomFragment : MarkAskBottomFragment) {
    private val fragment: MarkAskBottomFragment = markAskBottomFragment
    private val requestAPI = RequestAPI
    private val tokenManager = TokenManager

    fun initEvents(view: View) {
        fragment.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val formHouseLayout = view.findViewById<View>(R.id.constraintLayout_house_form)
                val formEventLayout = view.findViewById<View>(R.id.constraintLayout_event_form)
                val isHouse = tab.position == 0
                formHouseLayout.visibility = if (isHouse) View.VISIBLE else View.GONE
                formEventLayout.visibility = if (!isHouse) View.VISIBLE else View.GONE
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        fragment.houseAdresseText.doOnTextChanged { _, _, _, _ ->
            fragment.houseFormSubmitBtn.isEnabled = !fragment.houseAdresseText.text.isNullOrEmpty() && fragment.houseAdresseText.text.length >= 10 && fragment.houseAdresseText.text.length <= 100
        }
        fragment.eventAdresseText.doOnTextChanged { _, _, _, _ ->
            fragment.eventFormSubmitBtn.isEnabled = (!fragment.eventAdresseText.text.isNullOrEmpty() && fragment.eventAdresseText.text.length >= 10 && fragment.eventAdresseText.text.length <= 100) && (!fragment.titleEvent.text.isNullOrEmpty() && fragment.titleEvent.text.length >= 5 && fragment.titleEvent.text.length <= 40)
        }
        fragment.titleEvent.doOnTextChanged { _, _, _, _ ->
            fragment.eventFormSubmitBtn.isEnabled = (!fragment.eventAdresseText.text.isNullOrEmpty() && fragment.eventAdresseText.text.length >= 5 && fragment.eventAdresseText.text.length <= 100) && (!fragment.titleEvent.text.isNullOrEmpty() && fragment.titleEvent.text.length >= 5 && fragment.titleEvent.text.length <= 40)
        }

        fragment.houseFormSubmitBtn.setOnClickListener {
            fragment.houseFormSubmitBtn.text = "Envoi en cours..."
            fragment.houseFormSubmitBtn.isEnabled = false

            val isCelebrated = view.findViewById<CheckBox>(R.id.house_check_celebrate).isChecked
            val isDecorated = view.findViewById<CheckBox>(R.id.house_check_decorated).isChecked
            val message = view.findViewById<TextInputLayout>(R.id.house_commentaire_mark).editText?.text.toString()

            val jsonInputBodyData = JSONObject()
                .put("title", fragment.arguments?.getString("adresse"))
                .put("message", message)
                .put("latitude", fragment.arguments?.getDouble("latitude"))
                .put("longitude", fragment.arguments?.getDouble("longitude"))
                .put("adresse", fragment.houseAdresseText.text.toString())
                .put("is_celebrated", isCelebrated)
                .put("is_decorated", isDecorated)

            fragment.lifecycleScope.launch { // Launched as a coroutine
                val token = tokenManager.getToken(fragment.requireContext()).first() // Get token in a suspending way
                val response = requestAPI.requestPOST("mark/createHouse", jsonInputBodyData, token.toString()) // Make API call in a suspending way

                val textToast = when (response["code"]) {
                    422 -> "Champs entrées invalides"
                    401 -> "Non autorisé"
                    409 -> "Habitation déjà signalée"
                    else -> "Votre marquage pris en compte, merci !"
                }

                Toast.makeText(fragment.requireContext(), textToast, Toast.LENGTH_SHORT).show() // Show toast with the relevant message

                val result = Bundle().apply {
                    putBoolean("is_close", true)
                }
                fragment.parentFragmentManager.setFragmentResult("MarkAskBottom", result)
                fragment.dismiss() // Dismiss the fragment after showing the toast
            }
        }
        fragment.eventFormSubmitBtn.setOnClickListener {
            fragment.eventFormSubmitBtn.text = "Envoi en cours..."
            fragment.eventFormSubmitBtn.isEnabled = false

            val message = view.findViewById<TextInputLayout>(R.id.event_commentaire_mark).editText?.text.toString()

            val jsonInputBodyData = JSONObject()
                .put("title", fragment.titleEvent.text.toString())
                .put("message", message)
                .put("latitude", fragment.arguments?.getDouble("latitude"))
                .put("longitude", fragment.arguments?.getDouble("longitude"))
                .put("adresse", fragment.houseAdresseText.text.toString())

            fragment.lifecycleScope.launch {
                try {
                    val token = tokenManager.getToken(fragment.requireContext()).first()
                    val response = requestAPI.requestPOST("mark/createEvent", jsonInputBodyData, token.toString())

                    val textToast = when (response["code"]) {
                        422 -> "Champs entrées invalides"
                        401 -> "Non autorisé"
                        409 -> "Habitation déjà signalée"
                        else -> "Votre marquage pris en compte, merci !"
                    }

                    Toast.makeText(fragment.requireContext(), textToast, Toast.LENGTH_SHORT).show()

                    val result = Bundle().apply {
                        putBoolean("is_close", true)
                    }
                    fragment.parentFragmentManager.setFragmentResult("MarkAskBottom", result)
                    fragment.dismiss()
                } catch (e: Exception) {
                    Log.e("CoroutineError", "Erreur : ${e.message}", e)
                }
            }
        }
    }
}