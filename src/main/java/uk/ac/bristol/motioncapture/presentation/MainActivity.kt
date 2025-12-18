
package uk.ac.bristol.motioncapture.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import uk.ac.bristol.motioncapture.R
import uk.ac.bristol.motioncapture.presentation.activityModel.ConfigureActivitiesList
import android.os.Vibrator

//---------------------------------------------------------------------------------------------------------------------
// MotionCapture smartwatch app for collection of labelled accelerometer data for lifestyle health behaviour activities
// Developed in Android Studio Meerkat 2024.3.1
//
// Originated 2024 by Hamza Ahmed for an MSc dissertation project
// Updated 2025 by Chris Stone for use in research studies
// Translational and Applied Research Group, School of Psychological Science, University of Bristol, UK
//
// Notes:
// 1) Data collection duration is determined by value of variable activityTime, set in CollectAccelerometerData.kt
// 2) Activity selection hard-coded instead of incoming from Power Automate flow.
//    Code to retrieve data from flow is in HTTPRequestManagement.kt and hard-coded list is in ActivitySelectionTwo.kt
// 3) Sending data to cloud was time-limited to ~23 seconds due to use of a hashmap to cache data prior to transfer.
//    Hashmap is now taken out and data is saved directly to file Data7.csv in the smartwatch SDcard\Documents folder.
//    Comments included in CollectAccelerometerData where this code occurs. To implement data transfer to cloud (via
//    Power Automate flow), will need to read data from file prior to transfer in HTTPRequestManagement.kt
// 4) Sampling frequency is determined by value of variable samplingPeriod, set in CollectAccelerometerData.kt
//---------------------------------------------------------------------------------------------------------------------

class MainActivity : AppCompatActivity() {

    private lateinit var vibrator : Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_activity)

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_selection_view)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //  Start button goes to the ActivitySelection Screen
        val startButton: Button = findViewById(R.id.button_initiateDrinking)
        startButton.setOnClickListener {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(200)
            val intent = Intent(this, ConfigureActivitiesList::class.java)
            startActivity(intent)
        }

        // Start button goes to DummyDataPowerAutomate Screen
//        val startButton : Button = findViewById(R.id.button_initiateDrinking)
//        startButton.setOnClickListener {
//            val intent = Intent(this, DummyDataPowerAutomateScreen::class.java)
//            startActivity(intent)
//        }

    }

}



