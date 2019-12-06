package com.github.demack

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import java.time.Duration

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

    webSocket("/chat") {
      val log = call.application.log

      while (true) {
        when (val frame = incoming.receive()) {
          is Frame.Text -> {
            val msg = frame.readText()
            log.info("Message received: $msg")
            outgoing.send(Frame.Text(msg))
          }
          else -> throw IllegalArgumentException("Invalid input type from message")
        }
      }
    }
  }
}
