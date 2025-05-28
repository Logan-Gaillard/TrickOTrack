package octo.tricko.trickotrack.utils


import android.util.Log
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.ConnectException
import java.net.SocketTimeoutException

object RequestAPI {

    private var urlAPI : String = "https://trickotrapi.logangaillard.fr/"
    //private var urlAPI : String = "http://192.168.1.136:8000/"
    private var doingRequest = false

    suspend fun requestPOST(urlRequest: String, jsonInputBodyData: JSONObject, token: String? = null): Map<String, Any> = withContext(Dispatchers.IO) {
        try{
            //Retourner sans context si une requête est déjà en cours
            if(doingRequest) return@withContext mapOf("status" to "alreadyInRequest", "message" to "Une requête est déjà en cours", "type" to "RequestInProgress")
            doingRequest = true

            if (urlRequest.isEmpty()) throw Exception("URL vide") // Vérification de l'URL
            val url = URL("${urlAPI}${urlRequest}") // Création de l'URL à partir de la chaîne de caractères

            with(url.openConnection() as HttpURLConnection){ // Ouverture de la connexion
                requestMethod = "POST"
                connectTimeout = 10000 // Délai d'attente de connexion
                readTimeout = 10000 // Délai d'attente de lecture
                setRequestProperty("Content-Type", "application/json") // Type de donnée
                setRequestProperty("Accept", "application/json") // Type de réponse attendue
                if(token != null) { setRequestProperty("Authorization", "Bearer $token") }
                doOutput = true // Indique que l'on va envoyer des données

                outputStream.use { // Envoi des données
                    OutputStreamWriter(it).use { writer ->
                        Log.d("RequestAPI", "Données envoyées : $jsonInputBodyData") // Affichage des données envoyées dans les logs
                        writer.write(jsonInputBodyData.toString()) // Écriture des données JSON dans le flux de sortie
                        writer.flush() // Vidage du flux de sortie
                    }
                }

                val responseCode = responseCode
                val stream = if (responseCode in 200..299) inputStream else errorStream

                stream.bufferedReader().use {
                    val response = it.readText()
                    Log.d("RequestAPI", "Réponse avec le code : $responseCode : $response")
                    doingRequest = false
                    return@withContext mapOf("status" to if (responseCode in 200..299) "success" else "error","reponse" to response,"code" to responseCode)
                }
            }
        } catch (e: FileNotFoundException) {
            Log.e("RequestAPI", "FileNotFoundException : ${e}") // Affichage de l'erreur dans les logs
            doingRequest = false
            return@withContext mapOf("status" to "error", "message" to "FileNotFoundException : ${e.message}", "type" to "FileNotFoundException")
        } catch (e : ConnectException){
            doingRequest = false
            return@withContext mapOf("status" to "error", "message" to "ConnectException : ${e.message}", "type" to "ConnectException")
        } catch (e: SocketTimeoutException){
            doingRequest = false
            return@withContext mapOf("status" to "error", "message" to "SocketTimeoutException : ${e.message}", "type" to "SocketTimeoutException")
        } catch (e: Exception) {
            doingRequest = false
            return@withContext mapOf("status" to "error", "message" to "Exception : ${e.message}", "type" to "Exception")
        }
    }

    suspend fun requestGET(urlRequest: String): String = withContext(Dispatchers.IO) {
        val url = URL("${urlAPI}${urlRequest}") // Création de l'URL à partir de la chaîne de caractères
        with(url.openConnection() as HttpURLConnection){ // Ouverture de la connexion
            requestMethod = "GET"
            setRequestProperty("Content-Type", "application/json; utf-8")

            inputStream.bufferedReader().use {
                val response = it.readText()
                println("Réponse de l'API : ${responseCode} -> ${responseMessage}")
                return@use response
            }
        }
    }
}