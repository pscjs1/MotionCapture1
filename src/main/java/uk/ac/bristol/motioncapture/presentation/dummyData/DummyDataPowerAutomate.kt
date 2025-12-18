package uk.ac.bristol.motioncapture.presentation.dummyData

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class DummyDataPowerAutomate(context: Context) {

    // initialise variables
    private val endpointURL = "https://prod-30.westeurope.logic.azure.com:443/workflows/b101ac1b98504a27bb028bbced6a9369/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=YGVg81YYkXgrWvFuWfG4oUMfJ8JUIBoMpEmpQe1x3ZQ"
    private val endpointURL2 = "https://prod-56.westeurope.logic.azure.com:443/workflows/7aad2bc5cb7f4119add584ef8f8a3d4f/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=Mi5sAXcM0K0PA8lOkIqAPqhJCRfNGG8X9jxcS_fPOvA"
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
    // building the test data
    private val testData = listOf(
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

    fun sendDataToPowerAutomateTest() {
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
                        Log.d(Tag, "Data sent successfully: $requestBody")
                    } else {
                        Log.d(Tag, "Failed to send data ${response.code} - ${response.message}")
                    }
                }
            } catch (e: Exception) {
                Log.d(Tag, e.printStackTrace().toString())
            }

        }

    }

    fun testRetrieveData() {

        val requestBody = "Sending POST request to configure app".toRequestBody("text/plain".toMediaType())
        val request = Request.Builder()
            .url(endpointURL2)
            .header("Content-Type", "text/plain")
            .post(requestBody)
            .build()
        val executor = Executors.newSingleThreadScheduledExecutor()
        executor.execute {
            try {
                client.newCall(request).execute().use { response ->
                    if(response.isSuccessful) {
                        val jsonData = response.body?.string()
                        if (jsonData!=null) {
                            val activitiesList = gson.fromJson(jsonData, List::class.java)
                            Log.d(Tag, "Response body is : $activitiesList")
                        } else {
                            Log.d(Tag, "json body is null")
                        }

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