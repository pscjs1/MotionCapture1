package uk.ac.bristol.motioncapture.presentation.postRequest

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.SharedPreferences
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors


class HTTPRequestManagement(context: Context?) {

    // Extract the data from SharedPreferences
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedPrefActivities : SharedPreferences
    private val gson = Gson()
    //private val endpointURL = "https://prod-30.westeurope.logic.azure.com:443/workflows/b101ac1b98504a27bb028bbced6a9369/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=YGVg81YYkXgrWvFuWfG4oUMfJ8JUIBoMpEmpQe1x3ZQ"
    //CJS 18/08/25 - endpointURL changed for my flow "Upload Accelerometer Data CS" - old one above, new one below
    //private val endpointURL = "https://prod-02.westeurope.logic.azure.com:443/workflows/ea3a45026f024c23916528081b55430e/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=KHXc0Yf0TfcTzwRS8AfMlmV-Mdn-EcfzmAToMy2_OCg"
    //CJS 09/09/25 - endpointURL for flow "Upload Accelerometer Data CS" updated following change by Microsoft - old one above, new one below (both will work until 30th November 2025)
    //private val endpointURL = "https://defaultb2e47f30cd7d4a4ea5dab18cf1a415.1b.environment.api.powerplatform.com:443/powerautomate/automations/direct/workflows/ea3a45026f024c23916528081b55430e/triggers/manual/paths/invoke?api-version=1&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=qKcttaaPmkYVZzVmX9DgSfKnqbYYVROeJwW9akhmXaA"
    //CJS 27/10/25 - URL changed again by Microsoft, latest one below...
    private val endpointURL = "https://defaultb2e47f30cd7d4a4ea5dab18cf1a415.1b.environment.api.powerplatform.com:443/powerautomate/automations/direct/workflows/ea3a45026f024c23916528081b55430e/triggers/manual/paths/invoke?api-version=1&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=jvkRUrkElJ5O_O5iA1SaLed5ARihkg7sK4vbmeKIEoc"
    //private val configureURL = "https://prod-56.westeurope.logic.azure.com:443/workflows/7aad2bc5cb7f4119add584ef8f8a3d4f/triggers/manual/paths/invoke?api-version=2016-06-01&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=Mi5sAXcM0K0PA8lOkIqAPqhJCRfNGG8X9jxcS_fPOvA"
    //CJS 14/10/25 configureURL for flow "Send Response To App" updated following change by Microsoft - old one above, new one below (both will work until 30th November 2025)
    //private val configureURL = "https://defaultb2e47f30cd7d4a4ea5dab18cf1a415.1b.environment.api.powerplatform.com:443/powerautomate/automations/direct/workflows/7aad2bc5cb7f4119add584ef8f8a3d4f/triggers/manual/paths/invoke?api-version=1&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=mcrSOZg_wyCyP7thOvpBDnvDdZfjMjyXhqwPU184rr4"
    //CJS 27/10/25 - URL changed again by Microsoft, latest one below...
    private val configureURL = "https://defaultb2e47f30cd7d4a4ea5dab18cf1a415.1b.environment.api.powerplatform.com:443/powerautomate/automations/direct/workflows/7aad2bc5cb7f4119add584ef8f8a3d4f/triggers/manual/paths/invoke?api-version=1&sp=%2Ftriggers%2Fmanual%2Frun&sv=1.0&sig=g3YGwDd_5cYJZOw4uOism6uvwMzY-OF0Yr2mkS_QPqw"
    // Set up requestQueue variable to hold the list of PostData
    private val postDataQueue = ConcurrentLinkedQueue<PostData>()
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var Tag : String = "HTTPRequest"
    private val client = OkHttpClient()
    init {
        if (context == null) {
            throw IllegalArgumentException("Context cannot be null")
        }

        // Initialize SharedPreferences
        sharedPrefs = context.getSharedPreferences("accelerometerData", Context.MODE_PRIVATE)
        sharedPrefActivities = context.getSharedPreferences("activitiesList", Context.MODE_PRIVATE)
    }

    // Data Class to hold the data
    data class PostData(
        val sessionID : String,
        val id: String,
        val userID : String,
        val date: String,
        val timeStamp2 : String,
        val timeStamp: String,
        val xAxis: String,
        val yAxis: String,
        val zAxis: String,
        val activity: String
    )

    // Function to add new data to queue
    fun addDataToQueue(newPostDataList: List<PostData>) {
        Log.d(Tag, "Adding, ${newPostDataList.size} items to queue")
        newPostDataList.forEach { postDataQueue.add(it)}
        sendDataFromQueue()
    }

    private fun sendDataFromQueue() {
        executor.execute {
            while (postDataQueue.isNotEmpty()) {
                val currentPostData = postDataQueue.poll() // Retrieve and remove the head of the queue
                currentPostData?.let { sendDataToDataBase(it) }
            }
        }
    }

// Function to send a single POSTData Object
private fun sendDataToDataBase(postData: PostData) {
    val postDataJson = gson.toJson(listOf(postData))

    Log.d(Tag, "Sending JSON Body: $postDataJson")

    val requestBody = postDataJson.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url(endpointURL)
        .header("Content-Type", "application/json")
        .header("SecurityToken", "23632hbc9")
        .post(requestBody)
        .build()
    try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.d(Tag, "Failed to send data : $response")
                throw IOException("Unexpected response")
            } else {
                Log.d(Tag, "Data sent successfully")
            }
        }
    } catch (e:IOException) {
        Log.d(Tag, "Error while sending data : ${e.message}")
        e.printStackTrace()
    }

}

    fun retrieverAccelDataAsListPostData() : List<PostData> {

        // get the data from shared preferences in JSON format
        val accelerometerDataJson = sharedPrefs.getString("accelerometerData", null)
        // Convert the Data to a List<Map<String, String>> object
        val accelerometerDataList : List<Map<String, String>> = gson.fromJson(
            accelerometerDataJson,
            object  : TypeToken<List<Map<String, String>>>() {}.type
        ) ?: emptyList() // else return empty list

        // map the accelerometer data to PostData format
        // CJS 23/10/25 - when timestamp2 is added to saved preferences in CollectAccelerometerData, and the flow, add it here

        return accelerometerDataList.map { x ->
            PostData(
                sessionID = x["SessionID"] ?: "",
                id = x["deviceID"] ?: "",
                userID = x["userID"] ?: "",
                date = x["date"] ?: "",
                timeStamp2 = x["timeStamp2"] ?: "",
                timeStamp = x["timeStamp"] ?: "",
                xAxis = x["x-axis"] ?: "",
                yAxis = x["y-axis"] ?: "",
                zAxis = x["z-axis"] ?: "",
                activity = x["activity"] ?: ""
            )
        }
    }

    // function to retrieve the activities list from Power Automate
    fun retrieveActivitiesData() {
        // clear the previous activities List
        sharedPrefActivities.edit().remove("activitiesList").apply()

        val requestBody = "Sending POST request to configure app".toRequestBody("text/plain".toMediaType())
        val request = Request.Builder()
            .url(configureURL)
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
                            sharedPrefActivities.edit()
                                .putString("activitiesList", jsonData)
                                .apply()
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

    // function to get activities list data as a list<String>

    fun getSavedActivities() : List<String> {
        val jsonData = sharedPrefActivities.getString("activitiesList", null)
        if(jsonData !=null) {
            return gson.fromJson(jsonData, object : TypeToken<List<String>>() {}.type)
        } else {
            return emptyList()
        }
    }

}