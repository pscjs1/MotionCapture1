package uk.ac.bristol.motioncapture.presentation.parseJSONInput

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.json.Json

class ParseJSON (context : Context) {
    private val sharedPrefActivities : SharedPreferences = context.getSharedPreferences("activitiesList", Context.MODE_PRIVATE)
    private val activities = sharedPrefActivities.getString("activitiesList", null)
    var activitiesList : ArrayList<String> = ArrayList()
        private set
    var userID : String = "Unknown User"
        private set

//    fun processJSON() : List<String> {
//        val incomingData : IncomingData = Json.decodeFromString(activities)
//        activitiesList = ArrayList(incomingData.activities)
//        userID = incomingData.userIDs
//    }
}