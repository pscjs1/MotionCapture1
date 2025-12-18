package uk.ac.bristol.motioncapture.presentation.watchInputMethod

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import uk.ac.bristol.motioncapture.R
import uk.ac.bristol.motioncapture.databinding.ActivitySelectionRecyclerviewBinding

class ActivitySelectionScreen : AppCompatActivity() {

    private lateinit var binding : ActivitySelectionRecyclerviewBinding
    private var activitiesList : ArrayList<ActivityModel> = ArrayList()
    private val activityImages : Array<Int> = arrayOf(
        R.drawable.drinking,
        R.drawable.smoking,
        R.drawable.eating
    )
    private lateinit var activityAdaptor: ActivityAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        binding = ActivitySelectionRecyclerviewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        activityAdaptor = ActivityAdaptor(activitiesList) { selectedActivity ->
            // pass the selected activity to GetUserID Screen
            val intent = Intent(this, GetUserID::class.java)
            intent.putExtra("selectedActivity", selectedActivity)
            startActivity(intent)
        }


        binding.apply {
            mRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@ActivitySelectionScreen)
                adapter = activityAdaptor
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun loadData(){
        val activitiesNames : Array<String> = resources.getStringArray(R.array.activitiesNames)
        for (i in activitiesNames.indices){
            activitiesList.add(ActivityModel(activityImages[i], activitiesNames[i]))
        }
    }


}