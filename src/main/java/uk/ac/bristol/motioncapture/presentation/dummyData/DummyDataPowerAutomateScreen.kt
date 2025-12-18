package uk.ac.bristol.motioncapture.presentation.dummyData

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import uk.ac.bristol.motioncapture.R
import uk.ac.bristol.motioncapture.presentation.dummyData.DummyDataPowerAutomate

class DummyDataPowerAutomateScreen : AppCompatActivity() {
    private lateinit var Tag :String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dummy_data_power_automate_screen)


        val test_button : Button = findViewById(R.id.button_sendDummyData)

        test_button.setOnClickListener {
            sendHTTP()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun sendHTTP() {
        val dummyDataPowerAutomate = DummyDataPowerAutomate(this)
        dummyDataPowerAutomate.testRetrieveData()
    }
}