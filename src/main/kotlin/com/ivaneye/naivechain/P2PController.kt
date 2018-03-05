package com.ivaneye.naivechain

import com.alibaba.fastjson.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class P2PController {

    @Autowired
    private lateinit var blockService: BlockService

    @MessageMapping("/welcome")
    @SendTo("/topic/getResponse")
    fun say(message: Message): Message {
        return when (message.type) {
            QUERY_LATEST -> responseLatestMsg()
            QUERY_ALL -> responseChainMsg()
            RESPONSE_BLOCKCHAIN -> handleBlockChainResponse(message.data)
            else -> Message()
        }
    }

    private fun handleBlockChainResponse(message: String?): Message {
        val receiveBlocks = JSON.parseArray(message, Block::class.java)
        Collections.sort(receiveBlocks, { o1, o2 -> o1.index - o2.index })

        val latestBlockReceived = receiveBlocks[receiveBlocks.size - 1]
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

    private fun responseLatestMsg(): Message {
        val blocks = arrayOf(blockService.latestBlock)
        return Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blocks))
    }

    private fun responseChainMsg(): Message {
        return Message(RESPONSE_BLOCKCHAIN, JSON.toJSONString(blockService.getBlockChain()))
    }

    companion object {
        private val QUERY_LATEST = 0
        private val QUERY_ALL = 1
        private val RESPONSE_BLOCKCHAIN = 2
    }
}
