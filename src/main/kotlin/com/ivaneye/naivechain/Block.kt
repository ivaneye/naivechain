package com.ivaneye.naivechain

data class Block(
        val index: Int,
        val previousHash: String,
        val timestamp: Long,
        val data: String,
        val hash: String)