package me.juancer.leafalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var alarmManager: AlarmManager
    private lateinit var timePicker: TimePicker
    private lateinit var setAlarmButton: Button
    private lateinit var cancelAlarmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        timePicker = findViewById(R.id.timePicker)
        setAlarmButton = findViewById(R.id.setAlarmButton)
        cancelAlarmButton = findViewById(R.id.cancelAlarmButton)

        // Initialize the AlarmManager
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Set Alarm Button Click
        setAlarmButton.setOnClickListener {
            setAlarm()
        }

        // Cancel Alarm Button Click
        cancelAlarmButton.setOnClickListener {
            cancelAlarm()
        }
    }

    private fun setAlarm() {
        // Check if exact alarms can be scheduled (only necessary for Android 12 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Show a message or request permission to schedule exact alarms
                Toast.makeText(this, "Exact alarm permission required", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Proceed with scheduling the alarm
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
        calendar.set(Calendar.MINUTE, timePicker.minute)
        calendar.set(Calendar.SECOND, 0)

        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE)

        try {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Toast.makeText(
                this,
                "Alarm set for ${timePicker.hour}:${timePicker.minute}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "Permission denied for exact alarms", Toast.LENGTH_LONG).show()
        }
    }

    private fun cancelAlarm() {
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(pendingIntent)

        // Call the turnoff URL when alarm is canceled
        sendHttpRequest("http://192.168.0.34/turnoff")

        Toast.makeText(this, "Alarm canceled", Toast.LENGTH_SHORT).show()
    }

    // Helper function to make the HTTP GET request (same as AlarmReceiver)
    private fun sendHttpRequest(urlString: String) {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            val url = URL(urlString)
            val urlConnection = url.openConnection() as HttpURLConnection

            urlConnection.requestMethod = "GET"
            urlConnection.connectTimeout = 5000
            urlConnection.readTimeout = 5000

            val responseCode = urlConnection.responseCode
            Log.d("HTTP Response", "Response code: $responseCode")

            urlConnection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("HTTP Error", "Error making GET request: ${e.message}")
        }
    }

}