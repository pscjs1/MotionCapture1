package uk.ac.bristol.motioncapture.presentation.usefulFunctions

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class UsefulFunctions() {
    private val gson : Gson = Gson()

    fun retrieveAccelDataAsListString(context: Context, key: String? = null) : List<String> {
        // Initialise shared Preferences to get accelerometer Data
        val sharedPrefs : SharedPreferences = context.getSharedPreferences("accelerometerData", Context.MODE_PRIVATE)
        val accelerometerDataJson = sharedPrefs.getString("accelerometerData", null)

        // get the accelerometer data as a list of strings from the json file:
        val accelerometerDataList : List<Map<String, String>> = gson.fromJson(
            accelerometerDataJson,
            object : TypeToken<List<Map<String, String>>>() {}.type
        ) ?: emptyList() // if null return an empty list

        // if key files is not null, return the specific part of accelerometer data being accessed e.g. x-axis, ID, etc.
        return if (key != null) {
            // Extract the specific key value from each HashMap if a key is provided
            accelerometerDataList.mapNotNull { it[key] }
        } else {
            accelerometerDataList.map { gson.toJson(it) }
        }

    }


}