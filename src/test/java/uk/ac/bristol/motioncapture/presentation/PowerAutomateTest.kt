package uk.ac.bristol.motioncapture.presentation

import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Test
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


// Sends test
class PowerAutomateTest {
    // initialise variables
    private val endpointURL = "https://prod-30.westeurope.logic.azure.com:443/workflows/b101ac1b98504a27bb028bbced6a9369/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=YGVg81YYkXgrWvFuWfG4oUMfJ8JUIBoMpEmpQe1x3ZQ"
    private val client = OkHttpClient.Builder()
        // timeout for connection
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private var Tag : String = "PowerAutomateTest"


    data class PostData(
        val id: String,
        val userID : String,
        val date: String,
        val timeStamp: String,
        val xAxis: String,
        val yAxis: String,
        val zAxis: String,
        val activity: String
    )

    @Test
    fun sendDataToPowerAutomateTest() {

        // building the test data
        val testData = listOf(
            PostData(
                id = "12345",
                userID = "testUser",
                date = "2024-11-15",
                timeStamp = "testTimeStamp",
                xAxis = "1.0",
                yAxis = "2.0",
                zAxis = "3.0",
                activity = "TEST"
            )
        )
        // convert the data to
        val postDataJSON = gson.toJson(testData)

        // Create the request body
        val requestBody = postDataJSON.toRequestBody("application/json".toMediaType())

        // Build the request
        val request = Request.Builder()
            .url(endpointURL)
            .header("Content-Type", "application/json")
            .header("SecurityToken", "23632hbc9")
            .post(requestBody)
            .build()

        // send the request
        val executor = Executors.newSingleThreadScheduledExecutor()
        executor.execute {
            try {
                client.newCall(request).execute().use { response ->
                    if(response.isSuccessful) {
                        Log.d(Tag, "Data sent sucessfully: $requestBody")
                    } else {
                        Log.d(Tag, "Failed to send data ${response.code} - ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.d(Tag, e.printStackTrace().toString())
            }

        }




    }



}