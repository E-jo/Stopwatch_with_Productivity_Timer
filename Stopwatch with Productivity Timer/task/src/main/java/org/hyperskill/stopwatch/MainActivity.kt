package org.hyperskill.stopwatch

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.hyperskill.stopwatch.databinding.ActivityMainBinding
import kotlin.random.Random


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var firstStart = true
    private var running = false
    private val handler = Handler(Looper.getMainLooper())
    private var minutes = 0
    private var seconds = 0
    private var upperLimit = Int.MAX_VALUE

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startButton.setOnClickListener {
            running = true
            binding.settingsButton.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            if (firstStart)
                handler.postDelayed(tickClock, 1000)
            firstStart = false
        }

        binding.resetButton.setOnClickListener {
            binding.textView.setTextColor(Color.BLACK)
            running = false
            binding.settingsButton.isEnabled = true
            binding.progressBar.visibility = View.INVISIBLE
            handler.removeCallbacks(tickClock)
            firstStart = true
            minutes = 0
            seconds = 0
            updateTimer()
        }

        binding.settingsButton.setOnClickListener {
            val customLayout: View = layoutInflater.inflate(R.layout.limit_dialog, null)
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Set upper limit in seconds")
                .setView(customLayout)
                .setPositiveButton("ok") { dialog, id ->
                    val upperLimitEditText = customLayout.findViewById<EditText>(R.id.upperLimitEditText)
                    upperLimit = upperLimitEditText.text.toString().toInt()
                }
                .setNegativeButton("cancel") { dialog, id ->
                }
            builder.create().show()
        }
    }

    private fun updateTimer() {
        val timerTextMin = if (minutes <= 9) "0$minutes" else minutes
        val timerTextSec = if (seconds <= 9) "0$seconds" else seconds
        binding.textView.text = "$timerTextMin:$timerTextSec"
    }

    private val tickClock: Runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            seconds++
            if (seconds == 60) {
                minutes++
                seconds = 0
            }
            updateTimer()
            val totalSeconds = minutes * 60 + seconds
            if (upperLimit in 1 until totalSeconds) {
                binding.textView.setTextColor(Color.RED)
                val channelId = "org.hyperskill"
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val name = "Limit notification"
                    val descriptionText = "Stopwatch limit reached/exceeded"
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val channel = NotificationChannel(channelId, name, importance).apply {
                        description = descriptionText
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val intent = Intent(this@MainActivity, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(this@MainActivity, 0, intent, 0)

                val builder = Notification.Builder(this@MainActivity, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Stopwatch limit reached")
                    .setContentText("Stopwatch limit has been reached/exceeded")
                    .setContentIntent(pendingIntent)

                val notification = builder.build()
                notification.flags += Notification.FLAG_INSISTENT + Notification.FLAG_ONLY_ALERT_ONCE

                notificationManager.notify(393939, notification)
            }

            val color = Random.nextInt()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                binding.progressBar.indeterminateTintList = ColorStateList.valueOf(color)
            handler.postDelayed(this, 1000)
        }
    }
}