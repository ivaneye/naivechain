package com.ivaneye.naivechain

import java.security.MessageDigest

fun String.getSHA256(): String {
    return try {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(this.toByteArray(charset("UTF-8")))
        byte2Hex(messageDigest.digest())
    } catch (e: Exception) {
        println("getSHA256 is error" + e.message)
        ""
    }
}

private fun byte2Hex(bytes: ByteArray): String {
    val builder = StringBuilder()
    var temp: String
    bytes.forEach {
        temp = Integer.toHexString(it.toInt() and 0xFF)
        if (temp.length == 1) {
            builder.append("0")
        }
        builder.append(temp)
    }

    return builder.toString()
}