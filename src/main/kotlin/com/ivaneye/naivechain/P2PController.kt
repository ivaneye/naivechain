package com.ivaneye.naivechain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class P2PController {

    @Autowired
    private lateinit var p2pService: P2PService

    @MessageMapping("/send")
    @SendTo("/topic/peer")
    fun conn(message: Message): Message {
        return p2pService.handle(message)
    }
}
