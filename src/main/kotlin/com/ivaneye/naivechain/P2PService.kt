package com.ivaneye.naivechain

import org.java_websocket.WebSocket
import com.alibaba.fastjson.JSON
import java.net.URISyntaxException
import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ClientHandshake
import java.net.InetSocketAddress
import org.java_websocket.server.WebSocketServer
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.util.*


@Service
class P2PService : InitializingBean{
    override fun afterPropertiesSet() {
        initP2PServer(p2pPort)
        if(peer != null){
            connectToPeer(peer!!)
        }
    }

    @Value("\${config.p2pPort}")
    private lateinit var p2pPort:String

    @Value("\${config.peer}")
    private val peer:String? = null

    @Autowired
    private lateinit var blockService: BlockService

    private val sockets: MutableList<WebSocket>

    init {
        this.sockets = ArrayList()
    }

    fun initP2PServer(portStr: String) {
        val port = portStr.toInt()
        val socket = object : WebSocketServer(InetSocketAddress(port)) {
            override fun onOpen(webSocket: WebSocket, clientHandshake: ClientHandshake) {
                write(webSocket, queryChainLengthMsg())
                sockets.add(webSocket)
            }

            override fun onClose(webSocket: WebSocket, i: Int, s: String, b: Boolean) {
                println("connection failed to peer:" + webSocket.remoteSocketAddress)
                sockets.remove(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, s: String) {
                handleMessage(webSocket, s)
            }

            override fun onError(webSocket: WebSocket, e: Exception) {
                println("connection failed to peer:" + webSocket.remoteSocketAddress)
                sockets.remove(webSocket)
            }

            override fun onStart() {

            }
        }
        socket.start()
        println("listening websocket p2p port on: " + port)
    }

    private fun handleMessage(webSocket: WebSocket, s: String) {
        try {
            val message = JSON.parseObject(s, Message::class.java)
            println("Received message" + JSON.toJSONString(message))
            when (message.type) {
                QUERY_LATEST -> write(webSocket, responseLatestMsg())
                QUERY_ALL -> write(webSocket, responseChainMsg())
                RESPONSE_BLOCKCHAIN -> handleBlockChainResponse(message.data)
            }
        } catch (e: Exception) {
            println("hanle message is error:" + e.message)
        }

    }

    private fun handleBlockChainResponse(message: String?) {
        val receiveBlocks = JSON.parseArray(message, Block::class.java)
        Collections.sort(receiveBlocks, Comparator<Block> { o1, o2 -> o1.index - o1.index })

        val latestBlockReceived = receiveBlocks[receiveBlocks.size - 1]
        val latestBlock = blockService.latestBlock
        if (latestBlockReceived.index > latestBlock.index) {
            if (latestBlock.hash == latestBlockReceived.previousHash) {
                println("We can append the received block to our chain")
                blockService.addBlock(latestBlockReceived)
                broatcast(responseLatestMsg())
            } else if (receiveBlocks.size == 1) {
                println("We have to query the chain from our peer")
                broatcast(queryAllMsg())
            } else {
                blockService.replaceChain(receiveBlocks)
            }
        } else {
            println("received blockchain is not longer than received blockchain. Do nothing")
        }
    }

    fun connectToPeer(peer: String) {
        try {
            val socket = object : WebSocketClient(URI(peer)) {
                override fun onOpen(serverHandshake: ServerHandshake) {
                    write(this, queryChainLengthMsg())
                    sockets.add(this)
                }

                override fun onMessage(s: String) {
                    handleMessage(this, s)
                }

                override fun onClose(i: Int, s: String, b: Boolean) {
                    println("connection failed")
                    sockets.remove(this)
                }

                override fun onError(e: Exception) {
                    println("connection failed")
                    sockets.remove(this)
                }
            }
            socket.connect()
        } catch (e: URISyntaxException) {
            println("p2p connect is error:" + e.message)
        }

    }

    private fun write(ws: WebSocket, message: String) {
        ws.send(message)
    }

    fun broatcast(message: String) {
        for (socket in sockets) {
            this.write(socket, message)
        }
    }

    private fun queryAllMsg(): String {
        return JSON.toJSONString(Message(QUERY_ALL))
    }

    private fun queryChainLengthMsg(): String {
        return JSON.toJSONString(Message(QUERY_LATEST))
    }

    private fun responseChainMsg(): String {
        return JSON.toJSONString(Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blockService.getBlockChain())))
    }

    fun responseLatestMsg(): String {
        val blocks = arrayOf(blockService.latestBlock)
        return JSON.toJSONString(Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blocks)))
    }

    fun getSockets(): List<WebSocket> {
        return sockets
    }

    companion object {
        private val QUERY_LATEST = 0
        private val QUERY_ALL = 1
        private val RESPONSE_BLOCKCHAIN = 2
    }
}