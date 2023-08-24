package com.example.amov_2022_2023_tp1

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread


//1
class GameViewModelServidor : ViewModel() {

    companion object {
        const val SERVER_PORT = 9999

    }

    var levelTabuleiros = listOf<Tabuleiro>()
    var sockets = listOf<Socket?>()
    var listaJogadores = mutableListOf<Jogador>()
    var expressoesClientes = mutableListOf<Int>()


    var tempos = mutableListOf(0, 60)
    var expressoes = mutableListOf(0, 10)

    var socketServidor: Socket? = null
    var nrjogada = 1
    var pontos = 0
    var startTimer = 60
    var lastTimer = 0
    var nivel = 1
    var nrexpressoes = -1
    var expressoesCliente = 0
    var nivelCliente = 1
    var jogadorTerminou = false
    var tab = ""


    var t = 60
    val user = runBlocking {
        getUsername().await()
    }
    var esperaClientes = 15


    enum class State {
        STARTING, PLAYING, PLAYING_ME, PLAYER_FINISH,PLAYER_OUT, ROUND_ENDED, GAME_OVER
    }

    enum class ConnectionState {
        SETTING_PARAMETERS, SERVER_CONNECTING, CLIENT_CONNECTING, CONNECTION_ESTABLISHED,
        CONNECTION_ERROR, CONNECTION_ENDED
    }

    //Passar isto para uma classe
    var tabuleiro = MutableLiveData<String>()
    var infojogada = MutableLiveData<String>()
    var infojogo = MutableLiveData<String>()
    var estado = MutableLiveData(0)
    var jogadores = MutableLiveData<String>()

    var tempo = liveData {
        while (t >= 0) {
            if (expressoesCliente == nrexpressoes) {
                termineiNivel("terminei")
            }
            emit(t)
            delay(1000)
            for ((index, value) in expressoesClientes.withIndex()) {
                if (value == expressoes[nivel]) {
                    if (!verificaNivel(expressoesClientes))
                        enviaListaJogadores(index,value)
                }
            }


            if (_state.value == State.STARTING) {
                esperaClientes--
                if (esperaClientes == 0) {
                    estado.postValue(1)
                    enviaTabuleiro(tempos[nivel], expressoes[nivel])
                }
            }

            if (_state.value == State.PLAYING)
                t--
            if(t==0) _state.value = State.PLAYER_OUT

            lastTimer = t

        }
    }

    private fun enviaListaJogadores(id: Int, value: Int) {
        Log.i("enviaListaJogadores", sockets[id].toString())
        var jogadores = ""
        for (jogador in listaJogadores) {
            jogadores += jogador.username + "," + jogador.pontos + "," + jogador.nrQuadro + ":"
        }
        socket = sockets[id]
        socketO?.run {
            thread {
                try {
                    val printStream = PrintStream(this)
                    printStream.println("{\"tabuleiro\":\"0+0+0+ + +0+0+0+ + +0+0+0\",\"nivel\":\"$nivel\",\"tempo\":\"0\",\"pontos\":\"0\",\"jogada\":\"0\",\"expressoes\":\"$value\",\"id\":\"$id\",\"jogadores\":\"$jogadores\"}")
                    printStream.flush()
                } catch (_: Exception) {
                    stopGame()
                }
            }
        }
    }


    private fun verificaNivel(expressoesClientes: MutableList<Int>): Boolean {
        var check = false
        for (exp in expressoesClientes) {
            check = exp == expressoes[nivel]
            if (!check) break
        }

        return check
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


    private var serverSocket: ServerSocket? = null

    private var threadComm: Thread? = null

    fun enviaNivel(nivel: Int, tempo: Int, expressoes: Int) {
        Log.i("enviaNivel", sockets[0].toString())
        val tab = getTabuleiroByDepth(0, nivel)
        var jogadores = ""
        for (jogador in listaJogadores) {
            jogadores += jogador.username + "," + jogador.pontos + "," + jogador.nrQuadro + ":"
        }

        for ((index, value) in sockets.withIndex()) {
            expressoesClientes.set(index, 0)
            socket = value
            socketO?.run {
                thread {
                    try {
                        val printStream = PrintStream(this)
                        printStream.println("{\"tabuleiro\":\"${tab.tabString}\",\"tempo\":\"$tempo\",\"jogada\":\"mudanivel\",\"pontos\":\"0\",\"nivel\":\"$nivel\",\"expressoes\":\"$expressoes\",\"id\":\"$index\",\"jogadores\":\"$jogadores\"}")
                        printStream.flush()
                    } catch (_: Exception) {
                        stopGame()
                    }
                }
            }
        }

    }


    fun startGame() {
        _state.postValue(State.PLAYING)
    }

    fun termineiNivel(jogada: String) {
        Log.i("terminanivel", socket.toString())
        if (_state.value != State.PLAYER_FINISH) {
            socket = socketServidor
            socketO?.run {
                thread {
                    try {
                        val printStream = PrintStream(this)
                        printStream.println("{\"jogada\":\"$jogada\",\"nrjogada\":\"$nrjogada\",\"id\":\"0\",\"user\":\"$user\",\"pontos\":\"$pontos\"}")
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
        socket = socketServidor
        socketO?.run {
            thread {
                try {
                    val printStream = PrintStream(this)

                    printStream.println("{\"jogada\":\"$jogada\",\"nrjogada\":\"$nrjogada\",\"id\":\"0\",\"user\":\"$user\",\"pontos\":\"$pontos\"}")
                    printStream.flush()
                    nrjogada++
                } catch (e: Exception) {
                    println(e.printStackTrace())
                    stopGame()
                }
            }
        }
    }

    fun enviaTabuleiro(
        tempo: Int,
        expressoes: Int,
    ) {
        val tab = getTabuleiroByDepth(0, nivel)
        println("Envia tabuleiro")
        Log.i("enviatab", socket.toString())
        val jsonArray = JSONArray()

        for ((index, value) in sockets.withIndex()) {
            socket = value
            socketO?.run {
                thread {
                    try {
                        val printStream = PrintStream(this)
                        printStream.println("{\"tabuleiro\":\"${tab.tabString}\",\"tempo\":\"$tempo\",\"nivel\":\"$nivel\",\"pontos\":\"$pontos\",\"expressoes\":\"$expressoes\",\"id\":\"$index\"}")
                        printStream.flush()
                    } catch (_: Exception) {
                        stopGame()
                    }
                }
            }
        }

    }


    fun enviaInfoJogo(
        stringTab: String,
        pontos: Int,
        jogada: String,
        tempo: Int,
        nivel: Int,
        expressoes: Int,
        id: Int,
    ) {
        Log.i("enviainfojogo", sockets[id].toString())
        var jogadores = ""
        for (jogador in listaJogadores) {
            jogadores += jogador.username + "," + jogador.pontos + "," + jogador.nrQuadro + ":"
        }

        socket = sockets[id]
        socketO?.run {
            thread {
                try {
                    val printStream = PrintStream(this)
                    printStream.println("{\"tabuleiro\":\"$stringTab\",\"nivel\":\"$nivel\",\"tempo\":\"$tempo\",\"pontos\":\"$pontos\",\"jogada\":\"$jogada\",\"expressoes\":\"$expressoes\",\"id\":\"$id\",\"jogadores\":\"$jogadores\"}")
                    printStream.flush()
                } catch (_: Exception) {
                    stopGame()
                }
            }
        }
    }


    fun startServer(strIPAddress: String) {
        if (serverSocket != null || socket != null ||
            _connectionState.value != ConnectionState.SETTING_PARAMETERS
        )
            return

        println(strIPAddress)
        startClient(strIPAddress, SERVER_PORT)
        _connectionState.postValue(ConnectionState.SERVER_CONNECTING)

        thread {
            while (esperaClientes > 0) {
                serverSocket = ServerSocket(SERVER_PORT)
                serverSocket?.run {
                    try {
                        val socketClient = serverSocket!!.accept()
                        startCommServidor(socketClient)
                        // nclients++
                    } catch (_: Exception) {
                        _connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                    } finally {
                        serverSocket?.close()
                        serverSocket = null
                    }
                }
            }
        }
    }

    private fun startCommServidor(socketClient: Socket?) {
//        if (threadComm != null)
//            return
        socket = socketClient

        expressoesClientes.add(sockets.size, 0)
        sockets += socketClient
        println(sockets)
        threadComm = thread {
            try {
                if (socketI == null)
                    return@thread

                _connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
                println("Entrei Servidor")
                val bufI = socketI!!.bufferedReader()

                while (_state.value != State.GAME_OVER) {
                    val message = bufI.readLine()
                    println(message + "servidor")

                    val jsonObject = JSONTokener(message).nextValue() as JSONObject
                    val jogada = jsonObject.getString("jogada")
                    val nrjogada = jsonObject.getString("nrjogada")
                    val id = jsonObject.getString("id")
                    val user = jsonObject.getString("user")
                    val pontos = jsonObject.getString("pontos")

                    verificaJogada(jogada, nrjogada, id.toInt(), nivel,pontos.toInt(),user)
                    if (verificaNivel(expressoesClientes)) {
                        nivel += 1
                        tempos.add(tempos.last() - 5)
                        expressoes.add(expressoes.last() + 2)
                        levelTabuleiros = emptyList()
                        jogadorTerminou = false
                        //Envia nova informacao do jogo ->
                        // Novo tabuleiro, novo nivel, novo tempo,novas expressoes, envia os pontos que tem
                        enviaNivel(nivel, tempos[nivel], expressoes[nivel])

                    }

                }
            } catch (_: Exception) {
            } finally {
                stopGame()
            }
        }
    }

    private fun gereListaJogadores(id: Int, user: String, pontos: Int, nrjogada: Int) {
        val j = Jogador(id, user, pontos = pontos.toInt(), nrQuadro = nrjogada.toInt())
        if ((listaJogadores.any { it.username == user })) {
            val jogador = listaJogadores.find { it.username == user }
            jogador?.nrQuadro = nrjogada
            jogador?.pontos = pontos
        } else {
            listaJogadores.add(j)
        }


        for (j in listaJogadores)
            println(j.username + j.pontos + j.nrQuadro)
    }

    fun startClient(serverIP: String, serverPort: Int = SERVER_PORT) {

        if (socket != null)
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

        socketServidor = newsocket
        val inStream = BufferedReader(InputStreamReader(newsocket.getInputStream()))
        Log.i("cliente", newsocket.toString())
        println("Entrei no cliente")
        threadComm = thread {
            try {
//                if (inStream == null)
//                    return@thread

                _connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
                var mess = inStream.readLine()
                println(mess)
                val jsonObject = JSONObject(mess)
                val tab = jsonObject.getString("tabuleiro")
                val nivel = jsonObject.getString("nivel")
                val pontos = jsonObject.getString("pontos")
                val tempo = jsonObject.getString("tempo")
                val expressoes = jsonObject.getString("expressoes")


                nrexpressoes = expressoes.toInt()
                iniciaCliente(tab, nivel, pontos, tempo)
                socket = newsocket
                //val bufI = socketI!!.bufferedReader()
                //val mess = inStream.readLine()
                //println(mess)
                while (_state.value != State.GAME_OVER) {
                    mess = inStream.readLine()
                    println(mess)
//                    val message = bufI.readLine()
//                    println(message)
                    val jsonObject = JSONObject(mess)
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
                    expressoesCliente = expressoes.toInt()
                    gereCliente(tab, pontos, jogada, tempo.toInt())
                    if (nivel.toInt() != this.nivelCliente) {
                        println("MUDEI DE NIVEL")
                        _state.postValue(State.ROUND_ENDED)
                        this.nivelCliente = nivel.toInt()
                        nrexpressoes = expressoes.toInt()
                        expressoesCliente = 0
                        nrjogada = 1
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

    private fun gereCliente(tab: String, pontos: String, jogada: String, tempo: Int) {
        this.tab = tab
        this.pontos += pontos.toInt()
        this.infojogo.postValue("$nivel,${this.pontos}")
        this.infojogada.postValue("$jogada,$pontos")
        this.tabuleiro.postValue(tab)
        if (jogada.equals("mudanivel")) {
            t = tempo
            startTimer = tempo
        }
        //Update timer
        if (tempo == 5) {
            if (startTimer - lastTimer > 5) {
                t += tempo
            } else {
                t = startTimer
            }
        }

    }

    private fun enviaJogada(
        jogada: String,
        TabString: String,
        maior: Float,
        valorLinha: Float,
        segMaior: Float,
        nivel: Int,
        id: Int
    ) : Int {
        var pontos = 0
        if (maior == valorLinha) {
            //if(id == 0) updateTime = true
            expressoesClientes[id] += 1
            pontos = 2
            enviaInfoJogo(TabString, pontos, jogada, 5, nivel, expressoesClientes[id], id)
        } else if (segMaior == valorLinha) {
            pontos = 1
            enviaInfoJogo(TabString, pontos, jogada, 0, nivel, expressoesClientes[id], id)
        } else {
            enviaInfoJogo(TabString, pontos, jogada, 0, nivel, expressoesClientes[id], id)
        }
        return pontos
    }

    private fun verificaJogada(
        play: String,
        nrjogada: String,
        id: Int,
        nivel: Int,
        p: Int,
        user: String
    ) {
        var nplay = nrjogada.toInt()
        var tab = getTabuleiroByDepth(nplay, this.nivel)
        var pontos = p
        val (digits, notDigits) = play.partition { it.isDigit() }
        if (notDigits.equals("terminei")) {
            if (!jogadorTerminou) {
                jogadorTerminou = true
                pontos += 5
                enviaInfoJogo(tab.tabString, 5, "", 0, nivel, expressoesClientes[id], id)
            }
        }
        if (notDigits.equals("L")) {
            val valoresLinha = tab.getValoresLinha()
            val valoresColuna = tab.getValoresColuna()
            val segMaior = tab.segundoMaior(valoresLinha[0], valoresColuna[0], valoresLinha[4])
            tab = getTabuleiroByDepth(++nplay, this.nivel)
            pontos += enviaJogada(
                play,
                tab.tabString,
                valoresLinha[0],
                valoresLinha[digits.toInt()],
                segMaior,
                nivel,
                id
            )
        }
        if (notDigits.equals("C")) {
            val valoresLinha = tab.getValoresLinha()
            val valoresColuna = tab.getValoresColuna()
            val segMaior = tab.segundoMaior(valoresLinha[0], valoresColuna[0], valoresColuna[4])
            tab = getTabuleiroByDepth(++nplay, this.nivel)
            pontos += enviaJogada(
                play,
                tab.tabString,
                valoresColuna[0],
                valoresColuna[digits.toInt()],
                segMaior,
                nivel,
                id
            )
        }
        gereListaJogadores(id, user, pontos, nplay)
    }


    fun getTabuleiroByDepth(d: Int, nivel: Int): Tabuleiro {
        if (levelTabuleiros.size > d) {
            return levelTabuleiros.get(d)
        } else {
            //tab = criaNovoTabuleiro()
            var tab = Tabuleiro()
            tab.geraTabuleiro(nivel)
            levelTabuleiros += tab
            return tab
        }
    }

    fun stopServer() {
        serverSocket?.close()
        _connectionState.postValue(ConnectionState.CONNECTION_ENDED)
        serverSocket = null
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