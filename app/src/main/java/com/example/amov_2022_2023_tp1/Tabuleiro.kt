package com.example.amov_2022_2023_tp1

import android.view.View
import android.widget.TableRow
import android.widget.TextView
import kotlin.random.Random

class Tabuleiro() {

    val TAB_SIZE = 5
    val entradas = Array(TAB_SIZE) { Array(TAB_SIZE) { "" } }
    var tabString = ""
    //dados[0] = maior numero dados[1] = valor l1  dados[2] = valor l2 dados[3] = maior numero valor l3
    var dadosLinhas = Array(5){0f}
    var dadosColunas = Array(5){0f}


    fun geraTabuleiro(nivel : Int): Array<Array<String>> {
        var lista = listOf<String>()
        var limite = 9

        if(nivel == 1){
            lista = listOf("+")

        }
        if(nivel == 2){
            lista = listOf("+","-")
            limite = 20

        }
        if(nivel == 3){
            lista = listOf("+","-","X")
            limite = 9

        }
        if(nivel == 4){
            lista = listOf("+","-","X")
            limite = 20

        }
        if(nivel == 5){
            lista = listOf("+","-","X","/")
            limite = 20

        }
        if(nivel == 6){
            lista = listOf("+","-","X","/")
            limite = 40

        }
        if(nivel == 7){
            lista = listOf("+","-","X","/")
            limite = 50

        }
        if(nivel > 7){
            lista = listOf("+","-","X","/")
            limite = (limite+1)*(nivel-2)

        }


        for (i in 0 until TAB_SIZE) {
            for (j in 0 until TAB_SIZE) {
                if (i % 2 != 0 && j % 2 != 0) {
                    entradas[i][j] = " "
                } else if (i % 2 == 0 && j % 2 == 0) {
                    entradas[i][j] = Random.nextInt(1, limite).toString()
                } else {
                    entradas[i][j] = lista[Random.nextInt(0, lista.size)]
                }
                tabString += entradas[i][j]
            }
        }
        return entradas
    }

    fun segundoMaior(valor1: Float, valor2: Float, valor3: Float) : Float {
        var segMaior = 0f
        if(valor1 > valor2)     segMaior = valor2
        else if(valor2> valor1) segMaior=valor1
        else                    segMaior = valor3

        return segMaior
    }

    fun transpostaArray(entradas: Array<Array<String>>): Array<Array<String>> {
        val transposta = Array(entradas.size) { Array(entradas.size) { "" } }

        for (i in 0 until entradas.size) {
            for (j in 0 until entradas[i].size) {
                transposta[j][i] = entradas[i][j]
            }
        }
        return transposta
    }


    fun getValoresColuna(): Array<Float> {

        val transposta = transpostaArray(entradas)
        //dados[0] = maior numero dados[1] = valor c1  dados[2] = valor c2 dados[3] = maior numero valor c3
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
                            if (valorDaLinha > dadosColunas[0]){
                                dadosColunas[4]=dadosColunas[0]
                                dadosColunas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosColunas[4] && valorDaLinha != dadosColunas[0] )
                                dadosColunas[4] = valorDaLinha
                            if(i == 0) dadosColunas[1] = valorDaLinha
                            if(i == 2) dadosColunas[2] = valorDaLinha
                            if(i == 4) dadosColunas[3] = valorDaLinha
                            continue@here
                        }
                        if (transposta[i][j].equals("-")) {
                            valorDaLinha = (transposta[i][2].toFloat() * transposta[i][4].toFloat()) - transposta[i][0].toFloat()
                            println("Valor da linha2 ${valorDaLinha}")
                            if (valorDaLinha > dadosColunas[0]){
                                dadosColunas[4]=dadosColunas[0]
                                dadosColunas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosColunas[4] && valorDaLinha != dadosColunas[0] )
                                dadosColunas[4] = valorDaLinha
                            if(i == 0) dadosColunas[1] = valorDaLinha
                            if(i == 2) dadosColunas[2] = valorDaLinha
                            if(i == 4) dadosColunas[3] = valorDaLinha
                            continue@here
                        }
                    }else if ((transposta[i][j].equals("+") || transposta[i][j].equals("-") ) && transposta[i][3].equals("/")) {
                        if (transposta[i][j].equals("+")) {
                            valorDaLinha = (transposta[i][2].toFloat() / transposta[i][4].toFloat()) + transposta[i][0].toFloat()
                            println("Valor da linha3 ${valorDaLinha}")
                            if (valorDaLinha > dadosColunas[0]){
                                dadosColunas[4]=dadosLinhas[0]
                                dadosColunas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosColunas[4] && valorDaLinha != dadosColunas[0] )
                                dadosColunas[4] = valorDaLinha
                            if(i == 0) dadosColunas[1] = valorDaLinha
                            if(i == 2) dadosColunas[2] = valorDaLinha
                            if(i == 4) dadosColunas[3] = valorDaLinha
                            continue@here
                        }
                        if (transposta[i][j].equals("-")) {
                            valorDaLinha = (transposta[i][2].toFloat() / transposta[i][4].toFloat()) - transposta[i][0].toFloat()
                            println("Valor da linha4 ${valorDaLinha}")
                            if (valorDaLinha > dadosColunas[0]){
                                dadosColunas[4]=dadosColunas[0]
                                dadosColunas[0] = valorDaLinha
                            }else if(valorDaLinha>dadosColunas[4] && valorDaLinha != dadosColunas[0] )
                                dadosColunas[4] = valorDaLinha
                            if(i == 0) dadosColunas[1] = valorDaLinha
                            if(i == 2) dadosColunas[2] = valorDaLinha
                            if(i == 4) dadosColunas[3] = valorDaLinha
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

            if (valorDaLinha > dadosColunas[0]){
                dadosColunas[4]=dadosColunas[0]
                dadosColunas[0] = valorDaLinha
            }else if(valorDaLinha>dadosColunas[4] && valorDaLinha != dadosColunas[0] )
                dadosColunas[4] = valorDaLinha
            if(i == 0) dadosColunas[1] = valorDaLinha
            if(i == 2) dadosColunas[2] = valorDaLinha
            if(i == 4) dadosColunas[3] = valorDaLinha
        }

        //println("Value of ${valorDaLinha1} and ${valorDaLinha2} and l1 ${valorDaLinha3}")

        return dadosColunas

    }

    fun getValoresLinha(): Array<Float> {

        var valorDaLinha = 0f
        //dados[0] = maior numero dados[1] = valor l1  dados[2] = valor l2 dados[3] = maior numero valor l3

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

}
