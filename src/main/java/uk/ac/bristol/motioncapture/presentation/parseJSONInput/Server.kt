package uk.ac.bristol.motioncapture.presentation.parseJSONInput


import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import android.util.Log

private const val Tag = "Server.kt"

fun startServer() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        configureRouting()
    }.start(wait = true)
}


fun Application.configureRouting() {
    install(ContentNegotiation) {
        // Using Kotlin serialisation for JSON parsing
        json()
    }
    routing {
        post("/receive-data") {
            val inputData = call.receive<IncomingData>()
            Log.d(Tag, "Received Activities: ${inputData.activities}")
            Log.d(Tag, "Received UserIDs : ${inputData.userIDs}")

        }

    }

}

@Serializable
data class IncomingData(
    @SerialName("Activities") val activities : List<String>,
    @SerialName("UserIDs") val userIDs : String
)