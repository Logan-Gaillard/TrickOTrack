package octo.tricko.trickotrack.utils


import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.ConnectException

object RequestAPI {

    private var urlAPI : String = "http://192.168.101.242:8000/"

    suspend fun requestPOST(urlRequest: String, jsonInputBodyData: JSONObject, token: String? = null): Map<String, Any> = withContext(Dispatchers.IO) {
        try{
            if (urlRequest.isEmpty()) throw Exception("URL vide") // Vérification de l'URL
            val url = URL("${urlAPI}${urlRequest}") // Création de l'URL à partir de la chaîne de caractères

            with(url.openConnection() as HttpURLConnection){ // Ouverture de la connexion
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json") // Type de donnée
                if(token != null) setRequestProperty("Authorization", "Bearer $token") // Token d'authentification (si nécessaire)
                doOutput = true // Indique que l'on va envoyer des données

                outputStream.use { // Envoi des données
                    OutputStreamWriter(it).use { writer ->
                        writer.write(jsonInputBodyData.toString()) // Écriture des données JSON dans le flux de sortie
                        writer.flush() // Vidage du flux de sortie
                    }
                }

                inputStream.bufferedReader().use { // Lecture de la réponse de l'API
                    val response = it.readText()
                    println("Réponse complète : $response")
                    return@use mapOf("status" to "success", "reponse" to response)
                }
            }
        } catch (e: FileNotFoundException) {
            return@withContext mapOf("status" to "error", "message" to "Erreur 404 : ${e.message}", "type" to "FileNotFoundException")
        } catch (e : ConnectException){
            return@withContext mapOf("status" to "error", "message" to "Erreur 404 : ${e.message}", "type" to "ConnectException")
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