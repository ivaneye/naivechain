package com.ivaneye.naivechain

import java.security.MessageDigest

object CryptoUtil {

    fun getSHA256(str: String): String {
        val messageDigest: MessageDigest
        var encodeStr = ""
        try {
            messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(str.toByteArray(charset("UTF-8")))
            encodeStr = byte2Hex(messageDigest.digest())
        } catch (e: Exception) {
            println("getSHA256 is error" + e.message)
        }

        return encodeStr
    }

    private fun byte2Hex(bytes: ByteArray): String {
        val builder = StringBuilder()
        var temp: String
        for (b in bytes) {
            temp = Integer.toHexString(b.toInt() and 0xFF)
            if (temp.length == 1) {
                builder.append("0")
            }
            builder.append(temp)
        }
        return builder.toString()
    }
}