package uk.ac.bristol.motioncapture.presentation.collectAccelerometerData

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.text.DateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import uk.ac.bristol.motioncapture.R
import uk.ac.bristol.motioncapture.presentation.saveOrClearScreen.SaveOrRestartActivity
import uk.ac.bristol.motioncapture.presentation.usefulFunctions.UsefulFunctions
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import androidx.lifecycle.lifecycleScope


class CollectAccelerometerData : AppCompatActivity(), SensorEventListener {
    
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var accelerometerData = mutableListOf<HashMap<String, String>>()
    private lateinit var timerTextView: TextView
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var sharedPrefs : SharedPreferences
    private lateinit var userPrefs: SharedPreferences
    private lateinit var selectedActivity : String
    private lateinit var userID : String
    private var Tag : String = "CollectAccelData"
    private val file : String = "Data7.csv"
    private val samplingPeriod = 50000 // Samples one data point every second. Should be 50000 (for 20 samples per second ) / 20 samples: 10000000
    private lateinit var deviceId : String
    private lateinit var dateString : String
    private var timestamp : Long = 0
    private var sessionID : Int = 0
    private val gson = Gson()
    private val usefulFunctions = UsefulFunctions()
    private lateinit var vibrator : Vibrator
    private var timestamp2 : String = ""  // CJS 23/10/25 - added this
    private var sampleCount : Int = 0 // CJS 08/12/25 - added this
    private val test_file : String = "test_file.txt"  // CJS 08/12/25 - added this, but not used

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect_accelerometer_data)

        // clear the previous accelerometer data :
        accelerometerData.clear()

        // initialise views
        timerTextView = findViewById(R.id.timer_TextView)
        circularProgressBar = findViewById(R.id.circular_ProgressBar)

        // Generate device ID and timestamp
        deviceId = Build.ID
        val currentDate = Calendar.getInstance()
        dateString = DateFormat.getDateInstance(DateFormat.LONG).format(currentDate.time)
        timestamp = System.currentTimeMillis()

        // CJS 23/10/25 - create additional timestamp in understandable format, for referencing participant sessions
        val logTimeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss")
        timestamp2 = logTimeFormat.format(Date())



        // Initialise SharedPreferences
        sharedPrefs = getSharedPreferences("accelerometerData", Context.MODE_PRIVATE)
        userPrefs = getSharedPreferences("userPreferences", Context.MODE_PRIVATE)


        // Increment the session counter and save it
        sessionID = sharedPrefs.getInt("sessionCounter", 0) + 1
        sharedPrefs.edit().putInt("sessionCounter", sessionID).apply()

        // Initialise other variables
        selectedActivity = intent.getStringExtra("selectedActivity") ?: "UnknownActivity"
        //userID = intent.getStringExtra("userID") ?: "Unknown User"
        userID = userPrefs.getString("userID", "CA: Unknown User") ?: "CA: Unknown User"


        // Initialise sensor Manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


        // Log session ID and other data
        Log.d(Tag, "Start of Collection")
        Log.d(Tag, "Selected Activity is $selectedActivity")
        Log.d(Tag, "CA User ID is : $userID")

        // Flag to keep watch active while app is in use:
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Start collecting data when app is launched
        startDataCollection()
    }

    private fun startDataCollection() {
        // register Listener if accelerometer data is not null
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, samplingPeriod) // 20Hz collection frequency is 50,000 microseoncds
            startTimer()
        } else {
            Toast.makeText(this, "No Accelerometer data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTimer(){
        //val activityTime = 10000L // 10 seconds (use for quick testing)
        val activityTime = 120000L // 2 minutes - CJS 20/08/25
        //val activityTime = 20000L // 20 seconds
        //val activityTime = 30000L // 30 seconds
        val countDownTimer = object : CountDownTimer(activityTime, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(milisUntilFinished: Long) {
                val secondsLeft = milisUntilFinished / 1000
                timerTextView.text = "Time remaining: $secondsLeft s"
                // update the circular progress bar
                val progress = ((activityTime - milisUntilFinished).toFloat() / activityTime)*100
                circularProgressBar.setProgressWithAnimation(progress, 1000)
                Log.d("CJS", "Time remaining: $secondsLeft")
            }

            @SuppressLint("SetTextI18n")
            //override suspend fun onFinish() { //CJS 16/10/25 couldn't make this a suspend function but added lifecycleScope section (below)
            override fun onFinish() {
                // unregister listener:
                sensorManager.unregisterListener(this@CollectAccelerometerData)
                timerTextView.text = "Finished"
                Log.d("CJS", "Finished")
                circularProgressBar.setProgressWithAnimation(100f)
                // vibrate
                vibrator.vibrate(200)

                //CJS 16/10/25 - added lifecycleScope section here. Dispatchers.Default is suggested for CPU bound workload
                lifecycleScope.launch(Dispatchers.Default) {
                    val accelData : List<String> = usefulFunctions.retrieveAccelDataAsListString(this@CollectAccelerometerData, "x-axis")
                    Log.d(Tag, "CollectAccelerometerData Screen:  $accelData")
                    navigateToSaveOrRestart()
                }
                //Log.d(Tag, accelerometerData.toString()) // shows data is in accelerometerData mutable list
                //CJS 16/10/25 - three code lines below moved into lifecycleScope.launch section above
                //val accelData : List<String> = usefulFunctions.retrieveAccelDataAsListString(this@CollectAccelerometerData, "x-axis")
                //Log.d(Tag, "CollectAccelerometerData Screen:  $accelData")
                //navigateToSaveOrRestart()
            }

        }

            countDownTimer.start()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val xAxis = event.values[0].toString()
            val yAxis = event.values[1].toString()
            val zAxis = event.values[2].toString()
            // Extract the last three digits of the timestamp
            val lastThreeDigits = (timestamp % 1000).toInt()
            // Combine the last three digits with the session counter for the session ID
            val fullSessionID = "$lastThreeDigits-$sessionID"


            // HasMap to hold accelerometer Data:
            // CJS 23/10/25 - new timestamp2 added in here, HTTP and flow similarly updated
            // CJS 09/12/25 - the use of this HashMap has been taken out, as using it causes app to fail for data collection duration of >20 seconds, but it's still set up here
            val readingMap = hashMapOf(
                "SessionID" to fullSessionID,
                "deviceID" to deviceId,
                "userID" to userID,
                "date" to dateString,
                "timeStamp2" to timestamp2,
                "timeStamp" to timestamp.toString(),
                "x-axis" to xAxis,
                "y-axis" to yAxis,
                "z-axis" to zAxis,
                "activity" to selectedActivity
            )

            // CJS 09/12/25 *** adding HashMap contents to accelerometerData is taken out, as it causes app to fail for data collection duration of >20 seconds
            // Add the reading map to accelerometerData
            //accelerometerData.add(readingMap)


            // convert the data to a JSON format - CJS 09/12/25 taken out as it is a dependency of adding HashMap contents to accelerometerData
            //val dataAsJSON = gson.toJson(accelerometerData)

            // Add the data to sharedPrefs - CJS 09/12/25 taken out as it is a dependency of adding HashMap contents to accelerometerData
            //val editor = sharedPrefs.edit()
            //editor.putString("accelerometerData", dataAsJSON)
            //editor.apply()

            // CJS 08/12/25 - add the data to a text file - an attempt for testing which is not now used
            //FileWriter(test_file,true).use { out->
            //    out.write(("Testing " + fileCount +"\r\n"))
            //}

            // CJS 09/12/25 - writing data to file is now placed here to record data as it's captured by the accelerometer, as HashMap is no longer used
            try {
                // Get public directory for Documents
                val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                if (!publicDir.exists()) {
                    publicDir.mkdirs() // Create the directory if it doesn't exist
                }

                // File object to represent the public storage
                val publicFile = File(publicDir, file)
                val addHeaders = publicFile.length() == 0L

                FileOutputStream(publicFile, true).bufferedWriter().use { writer ->
                    if (addHeaders) {
                        // CJS 23/10/25 - new timestamp2 added in here
                        writer.write("SessionID,DeviceID,UserID,Date,Year,TimeStamp2,TimeStamp,X,Y,Z,Activity\n")
                    }
                    writer.write("$fullSessionID, $deviceId, $userID, $dateString, $timestamp2, $timestamp, $xAxis, $yAxis, $zAxis, $selectedActivity, \n")

                    // Log successful writing
                    Log.d(Tag, "Data Saved to file: $file at ${publicFile.absolutePath}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(Tag, "Error writing data to file ${e.message}")
            }
            sampleCount++
            Log.d("CJS", "Sample number: $sampleCount  Gyro output: X: $xAxis Y: $yAxis Z: $zAxis")
        }
    }

    /* Goes to Start or Restart Screen */
    //private fun navigateToSaveOrRestart() {
    //CJS 16/10/25 - this function made a suspend function
    //private fun navigateToSaveOrRestart() {
    private suspend fun navigateToSaveOrRestart() {
        // Save data to file before clearing SharedPreferences
        //writeDataToFile(accelerometerData, file) - CJS 09/12/25 file writing now moved

        // Navigate to the next screen
        val intent = Intent(this, SaveOrRestartActivity::class.java)
        startActivity(intent)
    }


    //private fun writeDataToFile(data: List<Map<String, String>>, fileName: String) {
    //CJS 16/10/25 - this function made a suspend function
    //CJS 09/12/25 - writing data to file now moved up into OnSensorChanged to record data as it's captured by the accelerometer, as HashMap is no longer used
    //private fun writeDataToFile(data: List<Map<String, String>>, fileName: String) {
    private suspend fun writeDataToFile(data: List<Map<String, String>>, fileName: String) {
        try {
            // Get public directory for Documents
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            if (!publicDir.exists()) {
                publicDir.mkdirs() // Create the directory if it doesn't exist
            }

            // File object to represent the public storage
            val publicFile = File(publicDir, fileName)
            val addHeaders = publicFile.length() == 0L

            FileOutputStream(publicFile, true).bufferedWriter().use { writer ->
                if (addHeaders) {
                    // CJS 23/10/25 - new timestamp2 added in here
                    writer.write("SessionID,DeviceID,UserID,Date,Year,TimeStamp2,TimeStamp,X,Y,Z,Activity\n")
                }
                data.forEach { entry ->
                    // CJS 23/10/25 - new timestamp2 added in here
                    val line =
                        "${entry["SessionID"] ?: ""}, " +
                                "${entry["deviceID"] ?: ""}, " +
                                "${entry["userID"] ?: ""}, " +
                                "${entry["date"] ?: ""}, " +
                                "${entry["timeStamp2"] ?: ""}, " +
                                "${entry["timeStamp"] ?: ""}, " +
                                "${entry["x-axis"] ?: ""}, " +
                                "${entry["y-axis"] ?: ""}, " +
                                "${entry["z-axis"] ?: ""}, " +
                                "${entry["activity"] ?: ""}, "
                    writer.write(line + "\n")
                }
                // Log successful writing
                Log.d(Tag, "Data Saved to file: $fileName at ${publicFile.absolutePath}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(Tag, "Error writing data to file ${e.message}")
        }
    }


    // Function to send accelerometer data from csv file rather than SharedPreferences


    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this@CollectAccelerometerData)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //
    }


}