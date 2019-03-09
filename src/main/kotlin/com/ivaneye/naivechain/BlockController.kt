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

    @RequestMapping(value = "/block", method = [(RequestMethod.GET)])
    fun blocks(): ResponseEntity<List<Block>> {
        return ResponseEntity.ok(blockService.getBlockChain())
    }

    @RequestMapping(value = "/block", method = [(RequestMethod.POST)])
    fun mineBlock(data: String): ResponseEntity<Block> {
        // data就是交易数据，放入「未确认交易池」
        // todo 先计算值
        // 从「未确认交易池」取出交易，创建区块
        val newBlock = blockService.generateNextBlock(data)
        // 加入到自身区块链中
        blockService.addBlock(newBlock)
        // 通知其它节点记录该区块
        p2pService.broatcast(p2pService.responseLatestMsg())
        return ResponseEntity.ok(newBlock)
    }

    @RequestMapping(value = "/peer", method = [(RequestMethod.GET)])
    fun peers(): ResponseEntity<String> {
        return ResponseEntity.ok(p2pService.sessions.toString())
    }

    @RequestMapping(value = "/peer", method = [(RequestMethod.POST)])
    fun addPeer(peer: String): ResponseEntity<String> {
        p2pService.conn(peer)
        return ResponseEntity.ok("")
    }
}
