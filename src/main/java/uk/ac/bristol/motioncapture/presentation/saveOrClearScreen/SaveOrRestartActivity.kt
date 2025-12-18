package uk.ac.bristol.motioncapture.presentation.saveOrClearScreen

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import uk.ac.bristol.motioncapture.R
import uk.ac.bristol.motioncapture.presentation.MainActivity
import uk.ac.bristol.motioncapture.presentation.activityModel.ActivitySelectionTwo
import uk.ac.bristol.motioncapture.presentation.usefulFunctions.UsefulFunctions
import uk.ac.bristol.motioncapture.presentation.postRequest.HTTPRequestManagement
import java.io.File
import java.io.FileOutputStream
import android.os.Vibrator

class SaveOrRestartActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var userPrefs : SharedPreferences
    private var Tag : String = "Save or Clear Data"
    private var dataFileName : String = "Data7.csv"
    private lateinit var userID : String
    private val usefulFunctions = UsefulFunctions()
    private lateinit var vibrator : Vibrator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_save_or_restart)

        sharedPrefs = getSharedPreferences("accelerometerData", Context.MODE_PRIVATE)
        userPrefs = getSharedPreferences("userPreferences", Context.MODE_PRIVATE)


        val accelData : List<String> = usefulFunctions.retrieveAccelDataAsListString(this@SaveOrRestartActivity)
        Log.d(Tag, "From SaveOrRestart Screen: $accelData")

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val hTTPButton: Button = findViewById(R.id.button_HTTP)
        val newSessionButton: Button = findViewById(R.id.button_newSession)
        val clearAllDataButton: Button = findViewById(R.id.button_clearAll)

        // Handle save Button:
        hTTPButton.setOnClickListener {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(200)
            sendHTTP()
        }

        // Handle ReStart Button
        newSessionButton.setOnClickListener {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(200)
            newSession()
        }

        // Handle ClearAll Button
        clearAllDataButton.setOnClickListener {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(200)
            clearAllData()  //CJS 19/08/25 - this function should be removed as we do not want the participant to be able to delete data?
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }


    private fun sendHTTP() {
        val httpRequestManagement = HTTPRequestManagement(context = this@SaveOrRestartActivity)

        // Retrieve accelerometer data as a list of PostData objects
        val postDataList = httpRequestManagement.retrieverAccelDataAsListPostData()

        // Debug: Log the retrieved data
        Log.d(Tag, "PostDataList retrieved: $postDataList")

        // Check if the list is empty before sending
        if (postDataList.isNotEmpty()) {
            httpRequestManagement.addDataToQueue(postDataList)
            Toast.makeText(this, "HTTP Request Sent", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(Tag, "No data to send.")
            Toast.makeText(this, "No data to send.", Toast.LENGTH_SHORT).show()
        }

        // Clear SharedPreferences
        sharedPrefs.edit().remove("accelerometerData").apply()
        userID = userPrefs.getString("userID", "SoR: Unknown User") ?: "SoR: Unknown User"

        // Go to the start of the activity selection screen
        val intent = Intent(this, ActivitySelectionTwo::class.java)
        startActivity(intent)
    }

    private fun newSession() {
        // clear the Shared Preferences:
        sharedPrefs.edit().remove("accelerometerData").apply()
        userID = userPrefs.getString("userID", "SoR Unknown User") ?: "Unknown User"

        Toast.makeText(this, "No data sent; restarting session", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ActivitySelectionTwo::class.java)
        startActivity(intent)
    }

    private fun restartSession() {
        // clear the Shared Preferences:
        sharedPrefs.edit().remove("accelerometerData").apply()

        val externalDir : File? = getExternalFilesDir(null)

        // Get File Contents:
        if (externalDir !=null) {

            val file = File(externalDir, dataFileName)

            if (file.exists()) {
                // read the file contents:
                val fileContents = file.readText()
                val dataSessions = fileContents.split("\n").filter { it.isNotEmpty() }
                if (dataSessions.isNotEmpty()) {
                    //TODO: Calculate Sampling Frequency and number of samples collected per session and export from CollectAcclerometerData, and use that number in the dropLast method
                    val updatedContents = dataSessions.dropLast(1).joinToString("\n")

                    val fileOutputStream = FileOutputStream(file, false)
                    fileOutputStream.write((updatedContents).toByteArray())
                    fileOutputStream.close()

                    Toast.makeText(this, "Last Data Session Cleared", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Data File is empty, nothing to clear", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "File: $file not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(Tag, "External dir : ${externalDir} is not found")
        }
    }


    // TODO: Should ask the user to confirm that all data will be cleared. - hamza
    private fun clearAllData() {
        // Go to the Main Activity Screen. All data has been cleared.
        // Clear Shared Pref:
        sharedPrefs.edit().remove("accelerometerData").apply()

        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

        // Clear File Contents:
        if (publicDir != null) {
            val file = File(publicDir, dataFileName)
            if (file.exists()) {
                val fileOutputStream = FileOutputStream(file, false) // false overwrited the file
                // write empty strings to file
                fileOutputStream.write(("").toByteArray())
                fileOutputStream.close()
            } else {
                Toast.makeText(this, "File: $file not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(Tag, "External dir : ${publicDir} is not found")
        }

    }

}