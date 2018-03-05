package com.ivaneye.naivechain

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args != null && (args.size == 2 || args.size == 3)) {
            try {
                val httpPort = Integer.valueOf(args[0])!!
                val p2pPort = Integer.valueOf(args[1])!!
                val blockService = BlockService()
                val p2pService = P2PService(blockService)
                p2pService.initP2PServer(p2pPort)
                if (args.size == 3 && args[2] != null) {
                    p2pService.connectToPeer(args[2])
                }
                val httpService = HTTPService(blockService, p2pService)
                httpService.initHTTPServer(httpPort)
            } catch (e: Exception) {
                println("startup is error:" + e.message)
            }

        } else {
            println("usage: java -jar naivechain.jar 8080 6001")
        }
    }
}