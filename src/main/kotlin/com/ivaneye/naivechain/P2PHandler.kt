package com.ivaneye.naivechain

import org.springframework.messaging.simp.stomp.*
import java.lang.reflect.Type


class P2PHandler(private val service: P2PService) : StompSessionHandlerAdapter() {

    override fun handleException(session: StompSession?, command: StompCommand?, headers: StompHeaders?, payload: ByteArray?, exception: Throwable?) {
        println("Exception")
        service.sessions.remove(session!!)
    }

    override fun handleTransportError(session: StompSession?, exception: Throwable?) {
        println("TransportError")
        service.sessions.remove(session!!)
    }

    override fun afterConnected(session: StompSession?, connectedHeaders: StompHeaders?) {
        println("afterConnected")
        service.sessions.add(session!!)
        session!!.subscribe("/topic/peer", object : StompFrameHandler {
            override fun handleFrame(headers: StompHeaders?, payload: Any?) {
                println("Frame")
                if (payload is Message) {
                    service.handle(payload)
                }
            }

            override fun getPayloadType(headers: StompHeaders?): Type {
                return Message::class.java
            }
        })
        session!!.send("/send", service.queryChainLengthMsg())
    }

    override fun getPayloadType(headers: StompHeaders?): Type {
        return Message::class.java
    }
}