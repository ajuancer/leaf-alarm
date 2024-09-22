package me.juancer.leafalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import java.net.HttpURLConnection
import java.net.URL

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Show a toast or notify when the alarm rings
        Toast.makeText(context, "Alarm Ringing: Turning ON", Toast.LENGTH_SHORT).show()

        // Turn ON action (alarm goes off)
        sendHttpRequest("http://192.168.0.34/turnon")

        // Add any additional logic like playing alarm sound, notification, etc.
    }

    // Helper function to make the HTTP GET request
    private fun sendHttpRequest(urlString: String) {
        // Allow network operations on the main thread temporarily for simplicity
        // You can improve this using AsyncTask, Kotlin Coroutines, or WorkManager
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            val url = URL(urlString)
            val urlConnection = url.openConnection() as HttpURLConnection

            // Set up request properties
            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = 5000  // 5 seconds
            urlConnection.readTimeout = 5000  // 5 seconds

            // Connect and fetch the response
            val responseCode = urlConnection.responseCode
            Log.d("HTTP Response", "Response code: $responseCode")

            urlConnection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("HTTP Error", "Error making GET request: ${e.message}")
        }
    }
}
