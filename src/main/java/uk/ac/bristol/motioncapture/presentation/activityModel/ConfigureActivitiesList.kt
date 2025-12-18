package uk.ac.bristol.motioncapture.presentation.activityModel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import uk.ac.bristol.motioncapture.R
import uk.ac.bristol.motioncapture.presentation.postRequest.HTTPRequestManagement
import uk.ac.bristol.motioncapture.presentation.watchInputMethod.GetUserID
import android.os.Vibrator

class ConfigureActivitiesList : AppCompatActivity() {

    private lateinit var vibrator : Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_configure_activities_list)

        val configure_button : Button = findViewById(R.id.button_configure)

        configure_button.setOnClickListener {
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(200)
            sendPOSTRequest()
            val intent = Intent(this, GetUserID::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun sendPOSTRequest() {
        val httpRequestManagement = HTTPRequestManagement(this)
        httpRequestManagement.retrieveActivitiesData()
    }
}