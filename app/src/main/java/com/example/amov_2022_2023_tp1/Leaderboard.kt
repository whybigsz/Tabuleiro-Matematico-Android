package com.example.amov_2022_2023_tp1

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Leaderboard : AppCompatActivity() {

    private val TAG : String = "LOG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Leaderboard.onCreate()")

        // Draw activity_settings.xml
        setContentView(R.layout.activity_leaderboard)

        // Leaderboard
        val top1 = findViewById<TextView>(R.id.leaderboard_top1)
        val top2 = findViewById<TextView>(R.id.leaderboard_top2)
        val top3 = findViewById<TextView>(R.id.leaderboard_top3)

        getHighestHighscore { top1.text = it }
        getSecondHighestHighscore { top2.text = it }
        getThirdHighestHighscore { top3.text = it }



    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Leaderboard.onPause()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Leaderboard.onResume()")
    }
}