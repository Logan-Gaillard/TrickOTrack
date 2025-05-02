package octo.tricko.trickotrack.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class TokenState { // Enum pour représenter l'état du token
    MISSING, // Token manquant
    INVALID, // Token invalide (ex: vide, expiré)
    VALID // Token valide
}

object TokenManager { // Singleton pour gérer le token de l'utilisateur

    private val Context.dataStore by preferencesDataStore(name = "userDatas") // Crée un DataStore de préférences nommé "userDatas"
    private val TOKEN_KEY = stringPreferencesKey("user_token") // Clé pour stocker le token
    public var tokenState = TokenState.MISSING // État initial du token

    suspend fun saveToken(context: Context, token: String) { //Context = contexte de l'application (ex: activité, service, etc.)
        context.dataStore.edit { prefs -> // Édite le DataStore
            prefs[TOKEN_KEY] = token // Enregistre le token dans le DataStore
        }
    }

    fun getToken(context: Context): Flow<String?> { //Flow = flux de données asynchrone en Kotlin.
        return context.dataStore.data.map { prefs -> // Récupère le token du DataStore
            prefs[TOKEN_KEY] // Renvoie le token ou null s'il n'existe pas
        }
    }

    suspend fun getTokenState(context: Context): TokenState {
        val prefs = context.dataStore.data.first()
        val token = prefs[TOKEN_KEY]
        tokenState = when { // assigne l'état du token en fonction de sa valeur
            token == null -> TokenState.MISSING
            token.isEmpty() -> TokenState.INVALID
            else -> TokenState.VALID
        }
        return tokenState
    }

}