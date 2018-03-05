package com.ivaneye.naivechain

import com.alibaba.fastjson.JSON
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class HTTPService(private val blockService: BlockService, private val p2pService: P2PService) {

    fun initHTTPServer(port: Int) {
        try {
            val server = Server(port)
            println("listening http port on: " + port)
            val context = ServletContextHandler(ServletContextHandler.SESSIONS)
            context.contextPath = "/"
            server.setHandler(context)
            context.addServlet(ServletHolder(BlocksServlet()), "/blocks")
            context.addServlet(ServletHolder(MineBlockServlet()), "/mineBlock")
            context.addServlet(ServletHolder(PeersServlet()), "/peers")
            context.addServlet(ServletHolder(AddPeerServlet()), "/addPeer")
            server.start()
            server.join()
        } catch (e: Exception) {
            println("init http server is error:" + e.message)
        }

    }

    private inner class BlocksServlet : HttpServlet() {
        @Throws(ServletException::class, IOException::class)
        override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
            resp.characterEncoding = "UTF-8"
            resp.writer.println(JSON.toJSONString(blockService.getBlockChain()))
        }
    }


    private inner class AddPeerServlet : HttpServlet() {
        @Throws(ServletException::class, IOException::class)
        override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
            this.doPost(req, resp)
        }

        @Throws(ServletException::class, IOException::class)
        override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
            resp.characterEncoding = "UTF-8"
            val peer = req.getParameter("peer")
            p2pService.connectToPeer(peer)
            resp.writer.print("ok")
        }
    }


    private inner class PeersServlet : HttpServlet() {
        @Throws(ServletException::class, IOException::class)
        override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
            resp.characterEncoding = "UTF-8"
            for (socket in p2pService.getSockets()) {
                val remoteSocketAddress = socket.getRemoteSocketAddress()
                resp.writer.print(remoteSocketAddress.getHostName() + ":" + remoteSocketAddress.getPort())
            }
        }
    }


    private inner class MineBlockServlet : HttpServlet() {
        @Throws(ServletException::class, IOException::class)
        override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
            this.doPost(req, resp)
        }

        @Throws(ServletException::class, IOException::class)
        override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
            resp.characterEncoding = "UTF-8"
            val data = req.getParameter("data")
            val newBlock = blockService.generateNextBlock(data)
            blockService.addBlock(newBlock)
            p2pService.broatcast(p2pService.responseLatestMsg())
            val s = JSON.toJSONString(newBlock)
            println("block added: " + s)
            resp.writer.print(s)
        }
    }
}