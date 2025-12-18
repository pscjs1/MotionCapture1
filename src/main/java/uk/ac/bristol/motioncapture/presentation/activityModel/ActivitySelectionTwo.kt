package uk.ac.bristol.motioncapture.presentation.activityModel

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import uk.ac.bristol.motioncapture.databinding.ActivitySelectionRecyclerviewBinding
import uk.ac.bristol.motioncapture.R
import uk.ac.bristol.motioncapture.presentation.collectAccelerometerData.CollectAccelerometerData
import android.os.Vibrator

class ActivitySelectionTwo : AppCompatActivity() {

    private lateinit var binding: ActivitySelectionRecyclerviewBinding
    private val Tag : String? = null
    private var activitiesList : ArrayList<ActivityModelTwo> = ArrayList()
    private lateinit var activityAdaptorTwo: ActivityAdaptorTwo
    private lateinit var userID : String
    private lateinit var vibrator : Vibrator



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userID = intent.getStringExtra("userID") ?: ": UnknownUser"
        Log.d(Tag, "ActivitySelectionTwo.0: $userID")
        loadData()
        binding = ActivitySelectionRecyclerviewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        activityAdaptorTwo = ActivityAdaptorTwo(activitiesList) { selectedActivity ->
            vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(200)
            val intent = Intent(this, CollectAccelerometerData::class.java)
            intent.putExtra("selectedActivity", selectedActivity)
            Log.d(Tag, "ActivitySelectionTwo.1: $userID")
            intent.putExtra("userID", userID)
            startActivity(intent)
        }

        binding.apply {
            mRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@ActivitySelectionTwo)
                adapter = activityAdaptorTwo
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun loadData() {
        //CJS 18/08/25 - hardcoded relevant activities for selection list, after suggested content by Areta Lee
        activitiesList.add(ActivityModelTwo("Wine glass"))
        activitiesList.add(ActivityModelTwo("Highball glass"))
        activitiesList.add(ActivityModelTwo("Eating: fingers"))
        activitiesList.add(ActivityModelTwo("Eating: cutlery"))
        activitiesList.add(ActivityModelTwo("Smoking"))
        activitiesList.add(ActivityModelTwo("Vaping"))
        //activitiesList.add(ActivityModelTwo("Drinking water"))
        //activitiesList.add(ActivityModelTwo("Eating soup"))
        //activitiesList.add(ActivityModelTwo("Eating steak"))
        //activitiesList.add(ActivityModelTwo("Snacking on chips"))
        //activitiesList.add(ActivityModelTwo("Snacking on chocolate bar"))
        //activitiesList.add(ActivityModelTwo("Walking"))
        //activitiesList.add(ActivityModelTwo("Brushing teeth"))
        //activitiesList.add(ActivityModelTwo("Walking"))
        //activitiesList.add(ActivityModelTwo("Sitting"))
        ////val httpRequestManagement = HTTPRequestManagement(this)
        ////val activityNames : List<String> = httpRequestManagement.getSavedActivities()
        ////for (name in activityNames) {
        ////    activitiesList.add(ActivityModelTwo(name))
        ////}

    }
}