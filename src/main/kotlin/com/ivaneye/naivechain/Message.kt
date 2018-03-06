package com.ivaneye.naivechain

import java.io.Serializable


data class Message(val type: Int = 0, val data: MutableList<Block>? = null) : Serializable