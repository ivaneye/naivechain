package com.ivaneye.naivechain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class BlockController {

    @Autowired
    private lateinit var blockService: BlockService

    @Autowired
    private lateinit var p2pService: P2PService

    @RequestMapping(value = "/blocks", method = [(RequestMethod.GET)])
    fun blocks(): ResponseEntity<List<Block>> {
        return ResponseEntity.ok(blockService.getBlockChain())
    }

    @RequestMapping(value = "/mineBlock", method = [(RequestMethod.POST)])
    fun mineBlock(data: String): ResponseEntity<Block> {
        val newBlock = blockService.generateNextBlock(data)
        blockService.addBlock(newBlock)
        p2pService.broatcast(p2pService.responseLatestMsg())
        return ResponseEntity.ok(newBlock)
    }

    @RequestMapping(value = "/peers", method = [(RequestMethod.GET)])
    fun peers(): ResponseEntity<List<String>> {
        val result = p2pService.getSockets().map {
            val remoteSocketAddress = it.remoteSocketAddress
            remoteSocketAddress.hostName + ":" + remoteSocketAddress.port
        }
        return ResponseEntity.ok(result)
    }

    @RequestMapping(value = "/addPeer", method = [(RequestMethod.POST)])
    fun addPeer(peer: String): ResponseEntity<String> {
        p2pService.connectToPeer(peer)
        return ResponseEntity.ok("")
    }
}