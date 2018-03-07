package com.ivaneye.naivechain

import org.springframework.stereotype.Service
import java.util.ArrayList

@Service
class BlockService {
    private var blockChain: MutableList<Block> = ArrayList()

    init {
        blockChain.add(this.fristBlock)
    }

    private fun calculateHash(index: Int, previousHash: String?, timestamp: Long, data: String?): String {
        val builder = StringBuilder(index)
        builder.append(previousHash).append(timestamp).append(data)
        return builder.toString().getSHA256()
    }

    val latestBlock: Block
        get() = blockChain!![blockChain!!.size - 1]

    private val fristBlock: Block
        get() =
            Block(1, "0", System.currentTimeMillis(), "EdenBlock", "816534932c2b7154836da6afc367695e6337db8a921823784c14378abed4f7d7")

    fun generateNextBlock(blockData: String): Block {
        val previousBlock = this.latestBlock
        val nextIndex = previousBlock.index + 1
        val nextTimestamp = System.currentTimeMillis()
        val nextHash = calculateHash(nextIndex, previousBlock.hash, nextTimestamp, blockData)
        return Block(nextIndex, previousBlock.hash, nextTimestamp, blockData, nextHash)
    }

    fun addBlock(newBlock: Block) {
        if (isValidNewBlock(newBlock, latestBlock)) {
            blockChain!!.add(newBlock)
        }
    }

    private fun isValidNewBlock(newBlock: Block, previousBlock: Block): Boolean {
        if (previousBlock.index + 1 != newBlock.index) {
            println("invalid index")
            return false
        } else if (previousBlock.hash != newBlock.previousHash) {
            println("invalid previoushash")
            return false
        } else {
            val hash = calculateHash(newBlock.index, newBlock.previousHash, newBlock.timestamp,
                    newBlock.data)
            if (hash != newBlock.hash) {
                println("invalid hash: " + hash + " " + newBlock.hash)
                return false
            }
        }
        return true
    }

    fun replaceChain(newBlocks: MutableList<Block>) {
        if (isValidBlocks(newBlocks) && newBlocks.size > blockChain!!.size) {
            blockChain = newBlocks
        } else {
            println("Received blockchain invalid")
        }
    }

    private fun isValidBlocks(newBlocks: List<Block>): Boolean {
        var fristBlock = newBlocks[0]
        if (fristBlock != this.fristBlock) {
            return false
        }

        for (i in 1 until newBlocks.size) {
            if (isValidNewBlock(newBlocks[i], fristBlock)) {
                fristBlock = newBlocks[i]
            } else {
                return false
            }
        }
        return true
    }

    fun getBlockChain(): List<Block> {
        return blockChain
    }
}