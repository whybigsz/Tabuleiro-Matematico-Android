package com.example.amov_2022_2023_tp1

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.amov_2022_2023_tp1.databinding.ActivityGameLocalBinding
import java.util.*
import kotlin.math.abs
import kotlin.random.Random


class GameLocal : AppCompatActivity() {

    lateinit var binding: ActivityGameLocalBinding
    private lateinit var gestureDetector: GestureDetector
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100
    private val TAB_SIZE = 5
    private var nivel = 1
    private var expressoes = 0
    private var pontos = 0
    val entradas = Array(TAB_SIZE) { Array(TAB_SIZE) { "" } }

    var timer: Timer?=null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameLocalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gestureDetector = GestureDetector(this, MyGestureListener())


        startTimer()
        binding.nivel.text = nivel.toString()
        binding.pontos.text = pontos.toString()

        binding.buttonSair.setOnClickListener() {
            val intent = Intent(this@GameLocal, Home::class.java)
            startActivity(intent)
        }

        binding.novoJogo.setOnClickListener(){
            val intent = Intent(this@GameLocal, GameLocal::class.java)
            startActivity(intent)
        }

        geraTabuleiro(nivel)
        //pausaTabuleiro()

        binding.table.setOnTouchListener { v, event ->
            //OnTouch Tabuleiro
            event?.let { gestureDetector.onTouchEvent(it) } ?: false
        };

    }



    override fun onBackPressed() {
            dialogYesOrNo(
                this,
                "O relógio de controlo do nível não deverá ser parado durante a \n" +
                        "visualização desta caixa de diálogo ",
                "Tem a certeza que quer sair?",
                DialogInterface.OnClickListener { dialog, id ->
                    val intent = Intent(this@GameLocal, Home::class.java)
                    startActivity(intent)
                }
            )
        }


    @SuppressLint("ClickableViewAccessibility")
    private fun mudaNivel() {
        timer?.cancel()
        nivel += 1
        var estado = "Proximo nivel ..."
        binding.estado.text = estado
        binding.nivel.isVisible=false
        binding.timer.isVisible=false
        binding.tempo.isVisible=false
        binding.pontos.isVisible=false
        binding.points.isVisible=false
        binding.table.setOnTouchListener(null)

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
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                var count = millisUntilFinished/1000
                binding.symbol4.text = count.toString()
                binding.symbol6.text = count.toString()
                binding.symbol7.text = count.toString()
                binding.symbol9.text = count.toString()
            }

            override fun onFinish() {
                estado = "Acerte 10 expressoes"
                binding.estado.text = estado
                binding.nivel.text = nivel.toString()
                binding.nivel.isVisible=true
                binding.timer.isVisible=true
                binding.tempo.isVisible=true
                binding.pontos.isVisible=true
                binding.points.isVisible=true
                geraTabuleiro(nivel)
                startTimer()
                binding.table.setOnTouchListener { v, event ->
                    //OnTouch Tabuleiro
                    event?.let { gestureDetector.onTouchEvent(it) } ?: false
                };
            }
        }.start()

    }

    private fun geraTabuleiro(nivel: Int) {
        var lista = listOf<String>()
        var limite = 9
        var estado = "Acerte 10 expressões"
        if(nivel == 1){
            lista = listOf("+")
            binding.estado.text = estado
        }
        if(nivel == 2){
            lista = listOf("+","-")
            limite = 20
            binding.estado.text = estado
        }
        if(nivel == 3){
            lista = listOf("+","-","X")
            limite = 9
            binding.estado.text = estado
        }
        if(nivel == 4){
            lista = listOf("+","-","X")
            limite = 20
            estado = "Acerte 15 expressoes"
            binding.estado.text = estado
        }
        if(nivel == 5){
            lista = listOf("+","-","X","/")
            limite = 20
            estado = "Acerte 15 expressoes"
            binding.estado.text = estado
        }
        if(nivel == 6){
            lista = listOf("+","-","X","/")
            limite = 40
            estado = "Acerte 15 expressoes"
            binding.estado.text = estado
        }
        if(nivel == 7){
            lista = listOf("+","-","X","/")
            limite = 50
            estado = "Acerte 20 expressoes"
            binding.estado.text = estado
        }
        if(nivel > 7){
            lista = listOf("+","-","X","/")
            limite = (limite+1)*(nivel-2)
            estado = "Acerte 20 expressoes"
            binding.estado.text = estado
        }


        for (i in 0 until binding.table.childCount) {
            val parentRow: View = binding.table.getChildAt(i)
            if (parentRow is TableRow) {
                for (j in 0 until parentRow.childCount) {
                    val childRow: View = parentRow.getChildAt(j)
                    if (childRow is TextView) {
                        if (childRow.id.toString().contains("21312310")) //contains number
                            childRow.text = Random.nextInt(1, limite).toString()
                        if (childRow.id.toString().contains("21312311")) //contains symbol
                            childRow.text = lista[Random.nextInt(0, lista.size)]
                        if (childRow.id.toString().contains("21312308")) // contains blank
                            childRow.text = ""
                        entradas[i][j] = childRow.text.toString()
                    }
                }
            }
        }
    }


    private fun startTimer(){
        var startTime = 60000L

        if(nivel == 2)
            startTime = 55000L
        if(nivel == 3)
            startTime = 50000L
        if(nivel == 4)
            startTime = 45000L
        if(nivel == 5)
            startTime = 40000L
        if(nivel == 6)
            startTime = 35000L
        if(nivel >= 7)
            startTime = 30000L

        timer = Timer(startTime);
        timer?.start()
    }

    private fun updateTimer(){
        if(timer!=null) {
            var startTime = 60000L
            if(nivel == 2)
                startTime = 55000L
            if(nivel == 3)
                startTime = 50000L
            if(nivel == 4)
                startTime = 45000L
            if(nivel == 5)
                startTime = 40000L
            if(nivel == 6)
                startTime = 35000L
            if(nivel >= 7)
                startTime = 30000L

            var tempo = binding.timer.text.toString()
            if((startTime/1000)-tempo.toInt() > 5){
                startTime = (tempo.toLong()*1000)+5000L
                //Here you need to maintain single instance for previous
                timer?.cancel()
                timer = Timer(startTime);
                timer?.start()
            } else {
                timer?.cancel()
                timer = Timer(startTime);
                timer?.start()
            }
        } else {
            startTimer()
        }
    }


    private fun transpostaArray(entradas: Array<Array<String>>): Array<Array<String>> {
        val transposta = Array(entradas.size) { Array(entradas.size) { "" } }

        for (i in 0 until entradas.size) {
            for (j in 0 until entradas[i].size) {
                transposta[j][i] = entradas[i][j]
            }
        }
        return transposta
    }


    private fun getValoresColuna(entradas: Array<Array<String>>): Array<Float> {

        val transposta = transpostaArray(entradas)
        //dados[0] = maior numero dados[1] = valor c1  dados[2] = valor c2 dados[3] = maior numero valor c3
        var dadosLinhas = Array(5){0f}
        var valorDaLinha = 0f

        here@ for (i in 0 until transposta.size) {
            valorDaLinha = 0f
            for (j in 0 until transposta[i].size) {
                if (!transposta[i][0].contains("[0-9]".toRegex())) {
                    continue@here
                }
                if (j == 1) {
                    println("Valor da linha inicial ${valorDaLinha}")
                    if ((transposta[i][j].equals("+") || transposta[i][j].equals("-") ) && transposta[i][3].equals("X")) {
                        if (transposta[i][j].equals("+")) {
                            valorDaLinha = (transposta[i][2].toFloat() * transposta[i][4].toFloat()) + transposta[i][0].toFloat()
                            println("Valor da linha1 ${valorDaLinha}")
                            if (valorDaLinha > dadosLinhas[0]){
                                dadosLinhas[4]=dadosLinhas[0]
                                dadosLinhas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosLinhas[4] && valorDaLinha != dadosLinhas[0] )
                                dadosLinhas[4] = valorDaLinha
                            if(i == 0) dadosLinhas[1] = valorDaLinha
                            if(i == 2) dadosLinhas[2] = valorDaLinha
                            if(i == 4) dadosLinhas[3] = valorDaLinha
                            continue@here
                        }
                        if (transposta[i][j].equals("-")) {
                            valorDaLinha = (transposta[i][2].toFloat() * transposta[i][4].toFloat()) - transposta[i][0].toFloat()
                            println("Valor da linha2 ${valorDaLinha}")
                            if (valorDaLinha > dadosLinhas[0]){
                                dadosLinhas[4]=dadosLinhas[0]
                                dadosLinhas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosLinhas[4] && valorDaLinha != dadosLinhas[0] )
                                dadosLinhas[4] = valorDaLinha
                            if(i == 0) dadosLinhas[1] = valorDaLinha
                            if(i == 2) dadosLinhas[2] = valorDaLinha
                            if(i == 4) dadosLinhas[3] = valorDaLinha
                            continue@here
                        }
                    }else if ((transposta[i][j].equals("+") || transposta[i][j].equals("-") ) && transposta[i][3].equals("/")) {
                        if (transposta[i][j].equals("+")) {
                            valorDaLinha = (transposta[i][2].toFloat() / transposta[i][4].toFloat()) + transposta[i][0].toFloat()
                            println("Valor da linha3 ${valorDaLinha}")
                            if (valorDaLinha > dadosLinhas[0]){
                                dadosLinhas[4]=dadosLinhas[0]
                                dadosLinhas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosLinhas[4] && valorDaLinha != dadosLinhas[0] )
                                dadosLinhas[4] = valorDaLinha
                            if(i == 0) dadosLinhas[1] = valorDaLinha
                            if(i == 2) dadosLinhas[2] = valorDaLinha
                            if(i == 4) dadosLinhas[3] = valorDaLinha
                            continue@here
                        }
                        if (transposta[i][j].equals("-")) {
                            valorDaLinha = (transposta[i][2].toFloat() / transposta[i][4].toFloat()) - transposta[i][0].toFloat()
                            println("Valor da linha4 ${valorDaLinha}")
                            if (valorDaLinha > dadosLinhas[0]){
                                dadosLinhas[4]=dadosLinhas[0]
                                dadosLinhas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosLinhas[4] && valorDaLinha != dadosLinhas[0] )
                                dadosLinhas[4] = valorDaLinha
                            if(i == 0) dadosLinhas[1] = valorDaLinha
                            if(i == 2) dadosLinhas[2] = valorDaLinha
                            if(i == 4) dadosLinhas[3] = valorDaLinha
                            continue@here
                        }
                    }

                    if (transposta[i][j].equals("X"))
                        valorDaLinha += (transposta[i][0].toFloat() * transposta[i][2].toFloat())
                    if (transposta[i][j].equals("/"))
                        valorDaLinha += (transposta[i][0].toFloat() / transposta[i][2].toFloat())

                    if (transposta[i][j].equals("+"))
                        valorDaLinha += transposta[i][0].toFloat() + transposta[i][2].toFloat()
                    if (transposta[i][j].equals("-"))
                        valorDaLinha += transposta[i][0].toFloat() - transposta[i][2].toFloat()
                }
                if (j == 3) {
                    if (transposta[i][j].equals("X"))
                        valorDaLinha *= transposta[i][4].toFloat()
                    if (transposta[i][j].equals("/"))
                        valorDaLinha /= transposta[i][4].toFloat()
                    if (transposta[i][j].equals("+"))
                        valorDaLinha += transposta[i][4].toFloat()
                    if (transposta[i][j].equals("-"))
                        valorDaLinha -= transposta[i][4].toFloat()
                    println("Valor da linha5 ${valorDaLinha}")
                }

                // println("Value of ${i} and ${j} and ${valorDaLinha}")
            }

            if (valorDaLinha > dadosLinhas[0]){
                dadosLinhas[4]=dadosLinhas[0]
                dadosLinhas[0] = valorDaLinha
            }else if(valorDaLinha>dadosLinhas[4] && valorDaLinha != dadosLinhas[0] )
                dadosLinhas[4] = valorDaLinha
            if(i == 0) dadosLinhas[1] = valorDaLinha
            if(i == 2) dadosLinhas[2] = valorDaLinha
            if(i == 4) dadosLinhas[3] = valorDaLinha
        }

        //println("Value of ${valorDaLinha1} and ${valorDaLinha2} and l1 ${valorDaLinha3}")

        return dadosLinhas

    }

    private fun getValoresLinha(entradas: Array<Array<String>>): Array<Float> {

        var valorDaLinha = 0f
        //dados[0] = maior numero dados[1] = valor l1  dados[2] = valor l2 dados[3] = maior numero valor l3
        var dadosLinhas = Array(5){0f}

        here@ for (i in 0 until entradas.size) {
            valorDaLinha = 0f
            for (j in 0 until entradas[i].size) {
                if (!entradas[i][0].contains("[0-9]".toRegex())) {
                    continue@here
                }
                if (j == 1) {
                    println("Valor da linha inicial ${valorDaLinha}")
                    if ((entradas[i][j].equals("+") || entradas[i][j].equals("-") ) && entradas[i][3].equals("X")) {
                        if (entradas[i][j].equals("+")) {
                            valorDaLinha = (entradas[i][2].toFloat() * entradas[i][4].toFloat()) + entradas[i][0].toFloat()
                            println("Valor da linha1 ${valorDaLinha}")
                            if (valorDaLinha > dadosLinhas[0]){
                                dadosLinhas[4]=dadosLinhas[0]
                                dadosLinhas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosLinhas[4] && valorDaLinha != dadosLinhas[0] )
                                dadosLinhas[4] = valorDaLinha

                            if(i == 0) dadosLinhas[1] = valorDaLinha
                            if(i == 2) dadosLinhas[2] = valorDaLinha
                            if(i == 4) dadosLinhas[3] = valorDaLinha
                            continue@here
                        }
                        if (entradas[i][j].equals("-")) {
                            valorDaLinha = (entradas[i][2].toFloat() * entradas[i][4].toFloat()) - entradas[i][0].toFloat()
                            println("Valor da linha2 ${valorDaLinha}")
                            if (valorDaLinha > dadosLinhas[0]){
                                dadosLinhas[4]=dadosLinhas[0]
                                dadosLinhas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosLinhas[4] && valorDaLinha != dadosLinhas[0] )
                                dadosLinhas[4] = valorDaLinha
                            if(i == 0) dadosLinhas[1] = valorDaLinha
                            if(i == 2) dadosLinhas[2] = valorDaLinha
                            if(i == 4) dadosLinhas[3] = valorDaLinha
                            continue@here
                        }
                    }else if ((entradas[i][j].equals("+") || entradas[i][j].equals("-") ) && entradas[i][3].equals("/")) {
                        if (entradas[i][j].equals("+")) {
                            valorDaLinha = (entradas[i][2].toFloat() / entradas[i][4].toFloat()) + entradas[i][0].toFloat()
                            println("Valor da linha3 ${valorDaLinha}")
                            if (valorDaLinha > dadosLinhas[0]){
                                dadosLinhas[4]=dadosLinhas[0]
                                dadosLinhas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosLinhas[4] && valorDaLinha != dadosLinhas[0] )
                                dadosLinhas[4] = valorDaLinha
                            if(i == 0) dadosLinhas[1] = valorDaLinha
                            if(i == 2) dadosLinhas[2] = valorDaLinha
                            if(i == 4) dadosLinhas[3] = valorDaLinha
                            continue@here
                        }
                        if (entradas[i][j].equals("-")) {
                            valorDaLinha = (entradas[i][2].toFloat() / entradas[i][4].toFloat()) - entradas[i][0].toFloat()
                            println("Valor da linha4 ${valorDaLinha}")
                            if (valorDaLinha > dadosLinhas[0]){
                                dadosLinhas[4]=dadosLinhas[0]
                                dadosLinhas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosLinhas[4] && valorDaLinha != dadosLinhas[0] )
                                dadosLinhas[4] = valorDaLinha
                            if(i == 0) dadosLinhas[1] = valorDaLinha
                            if(i == 2) dadosLinhas[2] = valorDaLinha
                            if(i == 4) dadosLinhas[3] = valorDaLinha
                            continue@here
                        }
                    }

                    if (entradas[i][j].equals("X"))
                        valorDaLinha += (entradas[i][0].toFloat() * entradas[i][2].toFloat())
                    if (entradas[i][j].equals("/"))
                        valorDaLinha += (entradas[i][0].toFloat() / entradas[i][2].toFloat())
                    if (entradas[i][j].equals("+"))
                        valorDaLinha += entradas[i][0].toFloat() + entradas[i][2].toFloat()
                    if (entradas[i][j].equals("-"))
                        valorDaLinha += entradas[i][0].toFloat() - entradas[i][2].toFloat()
                }
                if (j == 3) {
                    if (entradas[i][j].equals("X"))
                        valorDaLinha *= entradas[i][4].toFloat()
                    if (entradas[i][j].equals("/"))
                        valorDaLinha /= entradas[i][4].toFloat()
                    if (entradas[i][j].equals("+"))
                        valorDaLinha += entradas[i][4].toFloat()
                    if (entradas[i][j].equals("-"))
                        valorDaLinha -= entradas[i][4].toFloat()
                    println("Valor da linha5 ${valorDaLinha}")
                }
                // println("Value of ${i} and ${j} and ${valorDaLinha}")
            }

            if (valorDaLinha > dadosLinhas[0]){
                dadosLinhas[4]=dadosLinhas[0]
                dadosLinhas[0] = valorDaLinha
            }else if(valorDaLinha>dadosLinhas[4] && valorDaLinha != dadosLinhas[0] )
                dadosLinhas[4] = valorDaLinha
            if(i == 0) dadosLinhas[1] = valorDaLinha
            if(i == 2) dadosLinhas[2] = valorDaLinha
            if(i == 4) dadosLinhas[3] = valorDaLinha
        }

        //println("Value of ${valorDaLinha1} and ${valorDaLinha2} and l1 ${valorDaLinha3}")

        return dadosLinhas
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
                        Log.i("onFling", NUMBER_HEIGHT.toString())
                        if (e1.y <= NUMBER_HEIGHT && e2.y <= NUMBER_HEIGHT  && e2.x <= binding.table.width + swipeThreshold){
                            Log.i("onFling", "Primeira linha")
                            primeiraLinha()
                        }

                        if (e1.y >= NUMBER_HEIGHT * 2 && e2.y <= NUMBER_HEIGHT * 3 && e2.x <= binding.table.width + swipeThreshold){
                            Log.i("onFling", "Segunda linha")
                            segundaLinha()
                        }


                        if (e1.y >= NUMBER_HEIGHT * 4 && e2.y <= binding.table.height && e2.x <= binding.table.width + swipeThreshold){
                            Log.i("onFling", "Terceira linha")
                            terceiraLinha()
                        }

                    }
                }
                if (abs(diffY) > swipeThreshold && abs(velocityY) > swipeVelocityThreshold) {
                    if (diffY > 0) {
                        if (e1.x <= NUMBER_WIDTH && e2.x <= NUMBER_WIDTH && e2.y < binding.table.height + swipeThreshold){
                            Log.i("onFling", "Coluna 1")
                            primeiraColuna()
                        }
                        if (e1.x >= NUMBER_WIDTH * 2 && e2.x <= NUMBER_WIDTH * 3 && e2.y < binding.table.height + swipeThreshold){
                            Log.i("onFling", "Coluna 2")
                            segundaColuna()
                        }
                        if (e1.x >= NUMBER_WIDTH * 4 && e2.x <= binding.table.width && e2.y < binding.table.height + swipeThreshold){
                            Log.i("onFling", "Coluna 3")
                            terceiraColuna()
                        }

                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Up swipe gesture",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            geraTabuleiro(nivel)
            return true
        }
    }

    private fun terceiraColuna() {
        val valoresColuna = getValoresColuna(entradas)
        val valoresLinha = getValoresLinha(entradas)
        val segMaior = segundoMaior(valoresLinha[0],valoresColuna[0],valoresColuna[4])
        var incrementaPontosMaior = false
        var incrementaPontosSegMaior = false

        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if (valoresColuna[0] == valoresColuna[3]) {
                    incrementaPontosMaior = true
                    binding.number3.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number6.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol5.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol10.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(segMaior == valoresColuna[3]){
                    incrementaPontosSegMaior=true
                    binding.number3.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number6.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol5.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol10.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                }else {
                    binding.number3.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number6.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol5.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol10.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }
            override fun onFinish() {
                if(incrementaPontosSegMaior){
                    pontos+=1
                    binding.pontos.text = pontos.toString()
                }
                if(incrementaPontosMaior){
                    updateTimer()
                    expressoes += 1
                    pontos +=2
                    binding.pontos.text = pontos.toString()
                }
                if(nivel<4){
                    if(expressoes == 10){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel in 4..6){
                    if(expressoes == 15){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel >= 7){
                    if(expressoes == 20){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                binding.number3.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number6.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number9.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol5.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol10.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun segundaColuna() {
        val valoresColuna = getValoresColuna(entradas)
        val valoresLinha = getValoresLinha(entradas)
        val segMaior = segundoMaior(valoresLinha[0],valoresColuna[0],valoresColuna[4])
        var incrementaPontosMaior = false
        var incrementaPontosSegMaior = false

        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if (valoresColuna[0] == valoresColuna[2]) {
                    incrementaPontosMaior = true
                    binding.number2.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number5.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol4.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol9.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(segMaior == valoresColuna[2]){
                    incrementaPontosSegMaior = true
                    binding.number2.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number5.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol4.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol9.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                }else {
                    binding.number2.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number5.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol4.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol9.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }
            override fun onFinish() {
                if(incrementaPontosSegMaior){
                    pontos+=1
                    binding.pontos.text = pontos.toString()
                }
                if(incrementaPontosMaior){
                    updateTimer()
                    expressoes += 1
                    pontos +=2
                    binding.pontos.text = pontos.toString()
                }
                if(nivel<4){
                    if(expressoes == 10){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel in 4..6){
                    if(expressoes == 15){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel >= 7){
                    if(expressoes == 20){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                binding.number2.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number5.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number8.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol4.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol9.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun primeiraColuna() {
        val valoresColuna = getValoresColuna(entradas)
        val valoresLinha = getValoresLinha(entradas)
        val segMaior = segundoMaior(valoresLinha[0],valoresColuna[0],valoresColuna[4])
        var incrementaPontosMaior = false
        var incrementaPontosSegMaior = false

        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if (valoresColuna[0] == valoresColuna[1]) {
                    incrementaPontosMaior = true
                    binding.number1.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number4.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol3.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol8.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(segMaior == valoresColuna[1]){
                    incrementaPontosSegMaior = true
                    binding.number1.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number4.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol3.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol8.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                }else {
                    binding.number1.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number4.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol3.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol8.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }

            override fun onFinish() {
                if(incrementaPontosSegMaior){
                    pontos+=1
                    binding.pontos.text = pontos.toString()
                }
                if(incrementaPontosMaior){
                    updateTimer()
                    expressoes += 1
                    pontos +=2
                    binding.pontos.text = pontos.toString()
                }
                if(nivel<4){
                    if(expressoes == 10){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel in 4..6){
                    if(expressoes == 15){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel >= 7){
                    if(expressoes == 20){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                binding.number1.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number4.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number7.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol3.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol8.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun terceiraLinha() {
        val valoresLinha = getValoresLinha(entradas)
        val valoresColuna = getValoresColuna(entradas)
        val segMaior = segundoMaior(valoresLinha[0],valoresColuna[0],valoresLinha[4])
        var incrementaPontosMaior = false
        var incrementaPontosSegMaior = false

        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if(valoresLinha[0] == valoresLinha[3]) {
                    incrementaPontosMaior = true
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol11.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol12.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(segMaior == valoresLinha[3]){
                    incrementaPontosSegMaior =true
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol11.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                    binding.symbol12.backgroundTintList = ColorStateList.valueOf(Color.YELLOW)
                }else {
                    binding.number7.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number8.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.number9.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol11.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    binding.symbol12.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }

            }
            override fun onFinish() {
                if(incrementaPontosSegMaior){
                    pontos+=1
                    binding.pontos.text = pontos.toString()
                }
                if(incrementaPontosMaior){
                    updateTimer()
                    expressoes += 1
                    pontos +=2
                    binding.pontos.text = pontos.toString()
                }
                if(nivel<4){
                    if(expressoes == 10){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel in 4..6){
                    if(expressoes == 15){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel >= 7){
                    if(expressoes == 20){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                binding.number7.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number8.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number9.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol11.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol12.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun segundaLinha() {
        val valoresLinha = getValoresLinha(entradas)
        val valoresColuna = getValoresColuna(entradas)
        val segMaior = segundoMaior(valoresLinha[0],valoresColuna[0],valoresLinha[4])
        var incrementaPontosMaior = false
        var incrementaPontosSegMaior = false

        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if(valoresLinha[0] == valoresLinha[2]) {
                    incrementaPontosMaior = true
                    binding.number4.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number5.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number6.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol6.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol7.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(segMaior == valoresLinha[2]){
                    incrementaPontosSegMaior=true
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
                if(incrementaPontosSegMaior){
                    pontos+=1
                    binding.pontos.text = pontos.toString()
                }
                if(incrementaPontosMaior){
                    updateTimer()
                    expressoes += 1
                    pontos +=2
                    binding.pontos.text = pontos.toString()
                }
                if(nivel<4){
                    if(expressoes == 10){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel in 4..6){
                    if(expressoes == 15){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel >= 7){
                    if(expressoes == 20){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                binding.number4.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number5.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number6.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol6.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol7.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun primeiraLinha() {
        val valoresLinha = getValoresLinha(entradas)
        val valoresColuna = getValoresColuna(entradas)
        val segMaior = segundoMaior(valoresLinha[0],valoresColuna[0],valoresLinha[4])
        var incrementaPontosMaior = false
        var incrementaPontosSegMaior = false

        object : CountDownTimer(500, 1000) {
            override fun onTick(arg0: Long) {
                if(valoresLinha[0] == valoresLinha[1]) {
                    incrementaPontosMaior = true
                    binding.number1.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number2.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.number3.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol1.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    binding.symbol2.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                }else if(segMaior == valoresLinha[1]){
                    incrementaPontosSegMaior=true
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
                if(incrementaPontosSegMaior){
                    pontos+=1
                    binding.pontos.text = pontos.toString()
                }
                if(incrementaPontosMaior){
                    updateTimer()
                    expressoes += 1
                    pontos +=2
                    binding.pontos.text = pontos.toString()
                }
                if(nivel<4){
                    if(expressoes == 10){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel in 4..6){
                    if(expressoes == 15){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                if(nivel >= 7){
                    if(expressoes == 20){
                        expressoes = 0
                        mudaNivel()
                    }
                }
                binding.number1.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number2.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.number3.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#B2DFDB"))
                binding.symbol1.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
                binding.symbol2.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#009688"))
            }
        }.start()
    }

    private fun segundoMaior(valor1: Float, valor2: Float, valor3: Float) : Float {
        var segMaior = 0f
        if(valor1 > valor2)     segMaior = valor2
        else if(valor2> valor1) segMaior=valor1
        else                    segMaior = valor3

        return segMaior
    }

    inner class Timer(miliis:Long) : CountDownTimer(miliis,1000){

        override fun onTick(millisUntilFinished: Long) {
            var count = millisUntilFinished/1000
            binding.timer.text = count.toString()
        }
        override fun onFinish() {
            var estado = "Bom Jogo!"
            binding.level.text = estado
            //Pedreiro Style
            estado = "Obteve $pontos pontos"
            binding.estado.text = estado
            binding.estado.textAlignment = View.TEXT_ALIGNMENT_GRAVITY
            binding.nivel.isVisible=false
            binding.table.isVisible=false
            binding.timer.isVisible=false
            binding.tempo.isVisible=false
            binding.pontos.isVisible=false
            binding.points.isVisible=false

            // Firebase update score
            updateHighscore(pontos)
        }
    }

    fun dialogYesOrNo(
        activity: Activity,
        title: String,
        message: String,
        listener: DialogInterface.OnClickListener
    ) {
        val builder = AlertDialog.Builder(activity)
        builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
            dialog.dismiss()
            listener.onClick(dialog, id)
        })
        builder.setNegativeButton("No", null)
        val alert = builder.create()
        alert.setTitle(title)
        alert.setMessage(message)
        alert.show()
    }
}


