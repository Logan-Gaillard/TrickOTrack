package octo.tricko.trickotrack.repository

class MainRepository {
    private var countMessage: Int = 0;

    private fun incrementCountMessage() {
        countMessage++
    }

    fun getWelcomeMessage(): String {
        incrementCountMessage()
        return "Welcome to TrickoTrack!\nVous avez appuyé sur ce bouton $countMessage fois."
    }
}