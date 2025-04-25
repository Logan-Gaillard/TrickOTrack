package octo.tricko.trickotrack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import octo.tricko.trickotrack.repository.MainRepository

class MainViewModel : ViewModel() {

    private val repository = MainRepository()

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun loadMessage() {
        _message.value = repository.getWelcomeMessage()
    }
}