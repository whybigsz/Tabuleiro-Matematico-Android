package com.example.amov_2022_2023_tp1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeOnline: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_online)

        findViewById<Button>(R.id.home_btn_server).setOnClickListener {
            //startActivity(GameOnline.getServerModeIntent(this))
            val intent = Intent(this@HomeOnline, GameOnlineServidor::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.home_btn_client).setOnClickListener {
            //startActivity(GameOnline.getClientModeIntent(this))
            val intent = Intent(this@HomeOnline, GameOnlineCliente::class.java)
            startActivity(intent)
        }
    }
//    1

//    fun onPlayAsServer(view: android.view.View) {
//        onPlayOnline(0)
//    }
//
//    fun onPlayAsClient(view: android.view.View) {
//        onPlayOnline(1)
//    }
//
//    private fun onPlayOnline(mode: Int) {
//        val intent = Intent(this, GameOnline::class.java)
//        intent.putExtra("mode", mode)
//        startActivity(intent)
//    }
}