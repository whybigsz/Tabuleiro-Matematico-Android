package com.example.amov_2022_2023_tp1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import java.util.*
import androidx.appcompat.app.AppCompatDelegate
import android.widget.RadioGroup
import android.widget.TextView
import android.view.View




class Theme : AppCompatActivity() {
    // initializing variables for
    // our radio group and text view.
    private lateinit var radioGroup: RadioGroup
    private lateinit var themeTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)

        // initializing all our variables.
        radioGroup = findViewById(R.id.idRGgroup)
        themeTV = findViewById(R.id.idtvTheme)

        // on below line we are setting on check change method for our radio group.
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // on radio button check change
            when (checkedId) {
                R.id.idRBLight -> {
                    // on below line we are checking the radio button with id.
                    // on below line we are setting the text to text view as light mode.
                    themeTV.text = "Light Theme"
                    // on below line we are changing the theme to light mode.
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                R.id.idRBDark -> {

                    // this method is called when dark radio button is selected
                    // on below line we are setting dark theme text to our text view.
                    themeTV.text = "Dark Theme"
                    // on below line we are changing the theme to dark mode.
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
    }
}
