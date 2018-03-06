package com.ivaneye.naivechain

data class Block(
        val index: Int = 0,
        val previousHash: String = "",
        val timestamp: Long = 0,
        val data: String = "",
        val hash: String = "")