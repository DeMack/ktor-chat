package com.github.demack

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.features.origin
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import java.time.Duration
import java.util.Collections

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
  install(CallLogging)

  install(WebSockets) {
    pingPeriod = Duration.ofMinutes(1)
    timeout = Duration.ofMinutes(15)
    maxFrameSize = Long.MAX_VALUE
    masking = false
  }

  routing {
    get("/") {
      call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
    }

    val chatConnections = Collections.synchronizedSet(LinkedHashSet<ChatSession>())
    webSocket("/chat") {
      val log = call.application.log

      val session = ChatSession(this, call.request.origin.host)
      chatConnections += session
      log.info("New session connected: ${session.id}")

      // TODO: Custom usernames could be exchanged as part of an initial handshake

      try {
        while (true) {
          when (val frame = session.incoming.receive()) {
            is Frame.Text -> {
              val msg = frame.readText()
              log.info("Message received from ${session.id}: $msg")
              chatConnections.forEach { it.outgoing.send(Frame.Text(msg)) }
            }
            else -> throw IllegalArgumentException("Invalid input type from message")
          }
        }
      } finally {
        chatConnections -= session
      }
    }
  }
}

class ChatSession(session: DefaultWebSocketSession, ip: String) {
  val incoming = session.incoming
  val outgoing = session.outgoing
  val id = "user:$ip"
}
