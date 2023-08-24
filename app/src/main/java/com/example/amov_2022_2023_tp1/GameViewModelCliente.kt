package com.example.amov_2022_2023_tp1

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.json.JSONTokener
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.net.Socket
import kotlin.concurrent.thread

//1
class GameViewModelCliente : ViewModel() {
    companion object {
        const val SERVER_PORT = 9999

    }

    var nrjogada = 1
    var nrexpressoes = -1
    var expressoesJogo = 0
    var lastTimer = 0
    var nivel = 1
    var t = 60
    var startTimer = 60
    var tab = ""
    val user = runBlocking {
        getUsername().await()
    }
    var pontos = 0

    var id = -1

    enum class State {
        STARTING, PLAYING, PLAYING_ME, PLAYER_FINISH,PLAYER_OUT, ROUND_ENDED, GAME_OVER
    }

    enum class ConnectionState {
        SETTING_PARAMETERS, SERVER_CONNECTING, CLIENT_CONNECTING, CONNECTION_ESTABLISHED,
        CONNECTION_ERROR, CONNECTION_ENDED
    }

    var tabuleiro = MutableLiveData<String>()
    var infojogo = MutableLiveData<String>()
    var infojogada = MutableLiveData<String>()
    var estado = MutableLiveData(0)
    var jogadores = MutableLiveData<String>()

    var tempo = liveData {
        while (t >= 0) {
            if(nrexpressoes==expressoesJogo){
                termineiNivel("terminei")
            }
            emit(t)
            delay(1000)
            if (_state.value == State.PLAYING)
                t--
            if(t==0) _state.value = State.PLAYER_OUT

            lastTimer = t
        }

    }


    private val _state = MutableLiveData(State.STARTING)
    val state: LiveData<State>
        get() = _state

    private val _connectionState = MutableLiveData(ConnectionState.SETTING_PARAMETERS)
    val connectionState: LiveData<ConnectionState>
        get() = _connectionState


    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()


    private var threadComm: Thread? = null

    fun startGame() {
        _state.postValue(State.PLAYING)
    }


    fun termineiNivel(jogada: String) {
        Log.i("linha1", socket.toString())
        if(_state.value!=State.PLAYER_FINISH) {
            socketO?.run {
                thread {
                    try {
                        val printStream = PrintStream(this)
                        printStream.println("{\"jogada\":\"$jogada\",\"nrjogada\":\"$nrjogada\",\"id\":\"$id\",\"user\":\"$user\",\"pontos\":\"$pontos\"}")
                        printStream.flush()
                    } catch (_: Exception) {
                        stopGame()
                    }
                }
            }
        }
        _state.value = State.PLAYER_FINISH
    }

    fun jogada(jogada: String) {
        Log.i("linha1", socket.toString())
        socketO?.run {
            thread {
                try {
                    val printStream = PrintStream(this)
                    printStream.println("{\"jogada\":\"$jogada\",\"nrjogada\":\"$nrjogada\",\"id\":\"$id\",\"user\":\"$user\",\"pontos\":\"$pontos\"}")
                    printStream.flush()
                    nrjogada++
                } catch (_: Exception) {
                    stopGame()
                }
            }
        }
    }


    fun startClient(serverIP: String, serverPort: Int = GameViewModelServidor.SERVER_PORT) {

        if (socket != null )
            return

        thread {

            try {
                val newsocket = Socket(serverIP, serverPort)
//                val newsocket = Socket()
//                newsocket.connect(InetSocketAddress(serverIP, serverPort), 5000)
                startCommCliente(newsocket)
            } catch (_: Exception) {
                stopGame()
            }
        }
    }

    private fun startCommCliente(newsocket: Socket) {
        if (threadComm != null)
            return
        socket = newsocket
        Log.i("cliente", newsocket.toString())

        threadComm = thread {
            try {
                if (socketI == null)
                    return@thread

                _connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)


                val bufI = socketI!!.bufferedReader()
                val message = bufI.readLine()
                val jsonObject = JSONObject(message)
                val tab = jsonObject.getString("tabuleiro")
                val nivel = jsonObject.getString("nivel")
                val pontos = jsonObject.getString("pontos")
                val tempo = jsonObject.getString("tempo")
                val expressoes = jsonObject.getString("expressoes")
                val id = jsonObject.getString("id")


                nrexpressoes = expressoes.toInt()
                this.id = id.toInt()
                estado.postValue(nivel.toInt())
                iniciaCliente(tab, nivel, pontos,tempo)

                while (_state.value != State.GAME_OVER) {
                    val message = bufI.readLine()
                    println(message)
                    val jsonObject = JSONObject(message)
                    val jogada = jsonObject.getString("jogada")
                    val tab = jsonObject.getString("tabuleiro")
                    val pontos = jsonObject.getString("pontos")
                    val tempo = jsonObject.getString("tempo")
                    val nivel = jsonObject.getString("nivel")
                    val expressoes = jsonObject.getString("expressoes")
                    val jogadores = jsonObject.getString("jogadores")
//                    val jsonArray = jsonObject.getJSONArray("lista")
//                    val gson = Gson()
//                    val type = object : TypeToken<MutableList<Jogador>>() {}.type
                   // val list = gson.fromJson<MutableList<Jogador>>(jsonArray.toString(), type)
                    if(_state.value == State.PLAYER_FINISH)
                        this.jogadores.postValue(jogadores)
                    expressoesJogo = expressoes.toInt()
                    socket = newsocket
                    gereCliente(tab, pontos, jogada,tempo.toInt(),nivel.toInt())
                    if(this.nivel!=nivel.toInt()){
                        println("MUDEI DE NIVEL")
                        _state.postValue(State.ROUND_ENDED)
                        this.nivel =nivel.toInt()
                        nrexpressoes=expressoes.toInt()
                        expressoesJogo = 0
                        nrjogada=1
                    }
                }
            } catch (_: Exception) {
            } finally {
                stopGame()
            }
        }
    }



    private fun iniciaCliente(tab: String, nivel: String, pontos: String, tempo: String) {
        t = tempo.toInt()
        this.infojogo.postValue("$nivel,$pontos")
        this.tabuleiro.postValue(tab)
    }

    private fun gereCliente(tab: String, pontos: String, jogada: String,tempo: Int,nivel : Int) {
        this.tab = tab
        this.pontos+=pontos.toInt()
        this.infojogo.postValue("$nivel,${this.pontos}")
        this.infojogada.postValue("$jogada,$pontos")
        this.tabuleiro.postValue(tab)
        if(jogada.equals("mudanivel")){
            t=tempo
            startTimer = tempo
        }
        //Update timer
        if(tempo==5){
            if(startTimer-lastTimer>5){
                t+=tempo
            }else{
                t=startTimer
            }
        }

    }



    fun stopGame() {
        try {
            _state.postValue(State.GAME_OVER)
            _connectionState.postValue(ConnectionState.CONNECTION_ERROR)
            socket?.close()
            socket = null
            threadComm?.interrupt()
            threadComm = null
        } catch (_: Exception) {
        }
    }




}