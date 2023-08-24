package com.example.amov_2022_2023_tp1

import android.R.string
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils.split
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

import com.example.amov_2022_2023_tp1.databinding.ActivityGameOnlineBinding
import com.example.amov_2022_2023_tp1.databinding.ActivityGameOnlineServidorBinding
import java.net.Socket
import kotlin.math.abs
import kotlin.random.Random


class GameOnlineServidor : AppCompatActivity() {
    lateinit var binding: ActivityGameOnlineServidorBinding
    private lateinit var model: GameViewModelServidor
    private lateinit var gestureDetector: GestureDetector
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100
    private var dlg: AlertDialog? = null
    private var tab : Tabuleiro? = null
    private var pontos : Int? = 0
    private var infojogo : String? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameOnlineServidorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        model = ViewModelProvider(this).get(GameViewModelServidor::class.java)

        binding.novoJogo.isVisible = false
        model.tabuleiro.observe(this, Observer {
            println(it)
            tab?.tabString = it
            geraTabuleiro(it)
        })
        
        model.infojogada.observe(this, Observer {
            println(it)
            trataJogada(it)
        })
        model.infojogo.observe(this, Observer {
            val tokens = it?.split(",")?.toTypedArray()

            binding.nivel.text = tokens?.get(0)
            binding.pontos.text = tokens?.get(1)
        })

        model.tempo.observe(this, Observer {
            binding.timer.text = it.toString()
        })

        model.estado.observe(this, Observer{
            if(it==1){
                model.startGame()
                dlg?.dismiss()
                dlg = null
            }
        })


        gestureDetector = GestureDetector(this, MyGestureListener())

        binding.table.setOnTouchListener { v, event ->
            //OnTouch Tabuleiro
            println("TOUCH")
            event?.let { gestureDetector.onTouchEvent(it) } ?: false
        }

        model.state.observe(this){state ->

            if(state == GameViewModelServidor.State.PLAYER_FINISH){
                binding.table.setOnTouchListener(null)
                binding.table.isVisible = false
                model.jogadores.observe(this, Observer {string ->
                    mostraJogadores(string)
                })
            }
            if(state == GameViewModelServidor.State.ROUND_ENDED){
                mudaDeNivel()
            }
        }



        model.connectionState.observe(this) { state ->

            if (state != GameViewModelServidor.ConnectionState.SETTING_PARAMETERS &&
                state != GameViewModelServidor.ConnectionState.SERVER_CONNECTING
                && dlg?.isShowing == true && model.esperaClientes==0) {
                model.startGame()
                dlg?.dismiss()
                dlg = null
            }

            if (state == GameViewModelServidor.ConnectionState.CONNECTION_ERROR ||
                state == GameViewModelServidor.ConnectionState.CONNECTION_ENDED ) {
                finish()
            }
        }




        startAsServer()
    }


    private fun mostraJogadores(string: String?) {
        val linearLayout = LinearLayout(this)

        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.gravity = Gravity.CENTER

        // Create a TableLayout
        val tableLayout = TableLayout(this)
        tableLayout.gravity = Gravity.CENTER
        tableLayout.setDividerDrawable(resources.getDrawable(R.drawable.divider, null)) // Set divider drawable
        tableLayout.showDividers = TableLayout.SHOW_DIVIDER_MIDDLE

        val tableRow = TableRow(this)
        tableRow.setPadding(16, 16, 16, 16)
        tableRow.background = resources.getDrawable(R.drawable.boarder, null)
        tableRow.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00BFA5"))

        val textView1 = TextView(this)
        textView1.text = "Classificação"
        textView1.textSize = 30f
        textView1.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        textView1.gravity = Gravity.CENTER
        textView1.layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        textView1.setTextColor(Color.WHITE)


        tableRow.addView(textView1)
        tableLayout.addView(tableRow)
        // Create a TableRow
        val jogadores = string?.split(":")?.toTypedArray()
        println(jogadores?.size)
        for (j in jogadores!!){
            val tableRow = TableRow(this)
            tableRow.setPadding(16, 16, 16, 16)
            tableRow.gravity = Gravity.CENTER
            tableRow.background = resources.getDrawable(R.drawable.boarder, null)
            tableRow.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00BFA5"))

            // Create a TextView
            val textView = TextView(this)
            if(!j.equals("")){
                val jog = j.split(",").toTypedArray()
                textView.text = "User:"+ (jog.get(0)) +" Pontos:"+ (jog.get(1)) +" NrQuadro:"+ (jog.get(2))
                textView.textSize = 20f
                textView.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                textView.setTextColor(Color.WHITE)
                textView.gravity = Gravity.CENTER
                tableRow.addView(textView)

                // Add the TableRow to the TableLayout
                tableLayout.addView(tableRow)
            }


            // Add the TextView to the TableRow

        }


        // Add the TableLayout to the LinearLayout
        linearLayout.addView(tableLayout)

        // Set the LinearLayout as the content view
        setContentView(linearLayout)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun mudaDeNivel() {
        setContentView(binding.root)
        for (i in 0 until binding.table.childCount) {
            val parentRow: View = binding.table.getChildAt(i)
            if (parentRow is TableRow) {
                for (j in 0 until parentRow.childCount) {
                    val childRow: View = parentRow.getChildAt(j)
                    if (childRow is TextView) {
                        if (childRow.id.toString().contains("21312310")) //contains number
                            childRow.text = ""
                        if (childRow.id.toString().contains("21312311")) //contains symbol
                            childRow.text = ""
                        if (childRow.id.toString().contains("21312308")) // contains blank
                            childRow.text = ""
                    }
                }
            }
        }
        var estado = "Proximo nivel ..."
        binding.estado.text = estado
        binding.nivel.isVisible=false
        binding.timer.isVisible=false
        binding.tempo.isVisible=false
        binding.pontos.isVisible=false
        binding.points.isVisible=false
        binding.table.isVisible = true

        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var count = millisUntilFinished/1000
                binding.symbol4.text = count.toString()
                binding.symbol6.text = count.toString()
                binding.symbol7.text = count.toString()
                binding.symbol9.text = count.toString()
            }



            override fun onFinish() {

                estado = "Acerte "+model.nrexpressoes+" expressoes"
                binding.estado.text = estado
                binding.nivel.text = model.nivel.toString()
                binding.nivel.isVisible=true
                binding.timer.isVisible=true
                binding.tempo.isVisible=true
                binding.pontos.isVisible=true
                binding.points.isVisible=true
                geraTabuleiro(model.tab)
                binding.table.setOnTouchListener { v, event ->
                    //OnTouch Tabuleiro
                    event?.let { gestureDetector.onTouchEvent(it) } ?: false
                };
                model.startGame()
            }
        }.start()
    }

    private fun trataJogada(it: String?) {
        val tokens = it?.split(",")?.toTypedArray()
        if(tokens?.get(0).equals("1L"))
            primeiraLinha(tokens?.get(1))
        if(tokens?.get(0).equals("2L"))
            segundaLinha(tokens?.get(1))
        if(tokens?.get(0).equals("3L"))
            terceiraLinha(tokens?.get(1))
        if(tokens?.get(0).equals("1C"))
            primeiraColuna(tokens?.get(1))
        if(tokens?.get(0).equals("2C"))
            segundaColuna(tokens?.get(1))
        if(tokens?.get(0).equals("3C"))
            terceiraColuna(tokens?.get(1))


    }

    private fun terceiraColuna(pontos: String?) {
        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if(pontos.equals("2")) {
                    binding.number3.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number6.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol5.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol10.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(pontos.equals("1")){
                    binding.number3.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number6.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol5.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol10.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                }else{
                    binding.number3.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number6.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol5.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol10.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }
            override fun onFinish() {
                binding.number3.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number6.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number9.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol5.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol10.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun segundaColuna(pontos: String?) {
        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if(pontos.equals("2")) {
                    binding.number2.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number5.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol4.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol9.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(pontos.equals("1")){
                    binding.number2.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number5.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol4.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol9.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                }else{
                    binding.number2.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number5.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol4.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol9.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }
            override fun onFinish() {
                binding.number2.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number5.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number8.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol4.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol9.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun primeiraColuna(pontos: String?) {
        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if(pontos.equals("2")) {
                    binding.number1.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number4.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol3.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol8.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(pontos.equals("1")){
                    binding.number1.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number4.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol3.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol8.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                }else{
                    binding.number1.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number4.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol3.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol8.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }
            override fun onFinish() {
                binding.number1.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number4.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number7.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol3.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol8.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun terceiraLinha(pontos: String?) {
        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if(pontos.equals("2")) {
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol11.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol12.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(pontos.equals("1")){
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol11.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol12.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                }else{
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol11.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol12.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }
            override fun onFinish() {

                binding.number7.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number8.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number9.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol11.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol12.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun segundaLinha(pontos: String?) {
        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if(pontos.equals("2")) {
                    binding.number4.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number5.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number6.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol6.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol7.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(pontos.equals("1")){
                    binding.number4.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number5.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number6.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol6.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol7.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                }else{
                    binding.number4.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number5.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number6.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol6.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol7.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }
            override fun onFinish() {
                binding.number4.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number5.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number6.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol6.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol7.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun primeiraLinha(pontos: String?) {
        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if(pontos.equals("2")) {
                    binding.number1.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number2.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number3.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol1.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol2.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(pontos.equals("1")){
                    binding.number1.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number2.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number3.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol1.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol2.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                }else{
                    binding.number1.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number2.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number3.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol1.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol2.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }
            override fun onFinish() {

                binding.number1.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number2.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number3.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol1.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol2.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun geraTabuleiro(it: String?) {
        val numbers = it?.split('/', 'X', '+', '-',' ')?.toTypedArray()

        val (digits, notDigits) = it!!.partition { it.isDigit() }

        binding.number1.text = numbers?.get(0)
        binding.number2.text = numbers?.get(1)
        binding.number3.text = numbers?.get(2)
        binding.number4.text = numbers?.get(7)
        binding.number5.text = numbers?.get(8)
        binding.number6.text = numbers?.get(9)
        binding.number7.text = numbers?.get(14)
        binding.number8.text = numbers?.get(15)
        binding.number9.text = numbers?.get(16)
        binding.symbol1.text = notDigits[0].toString()
        binding.symbol2.text = notDigits[1].toString()
        binding.symbol3.text = notDigits[2].toString()
        binding.symbol4.text = notDigits[4].toString()
        binding.symbol5.text = notDigits[6].toString()
        binding.symbol6.text = notDigits[7].toString()
        binding.symbol7.text = notDigits[8].toString()
        binding.symbol8.text = notDigits[9].toString()
        binding.symbol9.text = notDigits[11].toString()
        binding.symbol10.text = notDigits[13].toString()
        binding.symbol11.text = notDigits[14].toString()
        binding.symbol12.text = notDigits[15].toString()
        binding.blank1.text = " "
        binding.blank2.text = " "
        binding.blank3.text = " "
        binding.blank4.text = " "
    }

    private fun startAsServer() {
        Log.d("DEBUG_TAG", "SERVER")
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip =
            wifiManager.connectionInfo.ipAddress // Deprecated in API Level 31. Suggestion NetworkCallback
        val strIPAddress = String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )

        val ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            this.setPadding(50, 50, 50, 50)
            layoutParams = params
            setBackgroundColor(Color.rgb(240, 224, 208))
            orientation = LinearLayout.HORIZONTAL
            addView(ProgressBar(context).apply {
                isIndeterminate = true
                val paramsPB = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                paramsPB.gravity = Gravity.CENTER_VERTICAL
                layoutParams = paramsPB
                indeterminateTintList = ColorStateList.valueOf(Color.rgb(96, 96, 32))
            })
            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams = paramsTV
                text = String.format(getString(R.string.msg_ip_address), strIPAddress)
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
        }

        dlg = AlertDialog.Builder(this)
            .setTitle(R.string.server_mode)
            .setView(ll)
            .setOnCancelListener {
                model.stopServer()
                finish()
            }
            .create()


        model.startServer(strIPAddress)
        dlg?.show()
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(event: MotionEvent): Boolean {
            Log.d("DEBUG_TAG", "onDown: $event")
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {

            try {
                val NUMBER_HEIGHT = binding.number1.height
                val NUMBER_WIDTH = binding.number1.width
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x

                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        if (e1.y <= NUMBER_HEIGHT && e2.y <= NUMBER_HEIGHT  && e2.x <= binding.table.width + swipeThreshold){
                            model.jogada("1L")
                        }

                        if (e1.y >= NUMBER_HEIGHT * 2 && e2.y <= NUMBER_HEIGHT * 3 && e2.x <= binding.table.width + swipeThreshold){
                            model.jogada("2L")
                        }


                        if (e1.y >= NUMBER_HEIGHT * 4 && e2.y <= binding.table.height && e2.x <= binding.table.width + swipeThreshold){
                            model.jogada("3L")
                        }
                    }
                }
                if (abs(diffY) > swipeThreshold && abs(velocityY) > swipeVelocityThreshold) {
                    if (diffY > 0) {
                        if (e1.x <= NUMBER_WIDTH && e2.x <= NUMBER_WIDTH && e2.y < binding.table.height + swipeThreshold){
                            model.jogada("1C")
                        }
                        if (e1.x >= NUMBER_WIDTH * 2 && e2.x <= NUMBER_WIDTH * 3 && e2.y < binding.table.height + swipeThreshold){
                            model.jogada("2C")
                        }
                        if (e1.x >= NUMBER_WIDTH * 4 && e2.x <= binding.table.width && e2.y < binding.table.height + swipeThreshold){
                            model.jogada("3C")
                        }

                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return true
        }
    }

}