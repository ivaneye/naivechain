package com.ivaneye.naivechain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.stereotype.Service
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.Transport
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import java.util.*

@Service
class P2PService {

    @Autowired
    private lateinit var blockService: BlockService

    val sessions: MutableList<StompSession> = ArrayList()

    fun handle(message: Message): Message {
        return when (message.type) {
            QUERY_LATEST -> responseLatestMsg()
            QUERY_ALL -> responseChainMsg()
            RESPONSE_BLOCKCHAIN -> handleBlockChainResponse(message.data)
            else -> Message()
        }
    }

    fun conn(url:String){
        val simpleWebSocketClient = StandardWebSocketClient()
        val transports = ArrayList<Transport>(1)
        transports.add(WebSocketTransport(simpleWebSocketClient))
        val sockJsClient = SockJsClient(transports)
        val stompClient = WebSocketStompClient(sockJsClient)
        stompClient.messageConverter = MappingJackson2MessageConverter()
        val sessionHandler = P2PHandler(this)
        val session = stompClient.connect(url, sessionHandler).get()
    }

    fun broatcast(message: Message) {
        for (session in sessions) {
            session.send("/send",message)
        }
    }

    private fun handleBlockChainResponse(receiveBlocks: MutableList<Block>?): Message {
        Collections.sort(receiveBlocks, { o1, o2 -> o1.index - o2.index })

        val latestBlockReceived = receiveBlocks!![receiveBlocks.size - 1]
        val latestBlock = blockService.latestBlock
        return if (latestBlockReceived.index > latestBlock.index) {
            when {
                latestBlock.hash == latestBlockReceived.previousHash -> {
                    println("We can append the received block to our chain")
                    blockService.addBlock(latestBlockReceived)
                    responseLatestMsg()
                }
                receiveBlocks.size == 1 -> {
                    println("We have to query the chain from our peer")
                    queryAllMsg()
                }
                else -> {
                    blockService.replaceChain(receiveBlocks)
                    Message()
                }
            }
        } else {
            println("received blockchain is not longer than received blockchain. Do nothing")
            Message()
        }
    }

    private fun queryAllMsg(): Message {
        return Message(QUERY_ALL)
    }

    fun queryChainLengthMsg(): Message {
        return Message(QUERY_LATEST)
    }

    fun responseLatestMsg(): Message {
        val blocks = listOf(blockService.latestBlock).toMutableList()
        return Message(RESPONSE_BLOCKCHAIN, blocks)
    }

    private fun responseChainMsg(): Message {
        return Message(RESPONSE_BLOCKCHAIN, blockService.getBlockChain().toMutableList())
    }

    companion object {
        private val QUERY_LATEST = 0
        private val QUERY_ALL = 1
        private val RESPONSE_BLOCKCHAIN = 2
    }
}