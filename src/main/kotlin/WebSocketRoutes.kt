package com.example

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import java.util.concurrent.CopyOnWriteArrayList

object TaskWebSocketManager {
    private val sessions = CopyOnWriteArrayList<DefaultWebSocketServerSession>()

    suspend fun broadcast(message: String) {
        val iterator = sessions.iterator()
        while (iterator.hasNext()) {
            val session = iterator.next()
            try {
                session.send(Frame.Text(message))
            } catch (e: ClosedSendChannelException) {
                sessions.remove(session)
            }
        }
    }

    fun addSession(session: DefaultWebSocketServerSession) {
        sessions.add(session)
    }

    fun removeSession(session: DefaultWebSocketServerSession) {
        sessions.remove(session)
    }
}

fun Route.webSocketRoutes() {
    webSocket("/ws/notifications") {
        TaskWebSocketManager.addSession(this)
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    println("Received: ${frame.readText()}")
                }
            }
        } catch (e: ClosedReceiveChannelException) {
        } finally {
            TaskWebSocketManager.removeSession(this)
        }
    }
}