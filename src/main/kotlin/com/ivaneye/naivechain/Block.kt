package com.ivaneye.naivechain

class Block(
        var index: Int = 0,
        var previousHash: String? = null,
        var timestamp: Long = 0,
        var data: String? = null,
        var hash: String? = null)