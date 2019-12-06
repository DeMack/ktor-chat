package com.github.demack

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
  @Test
  fun testRoot() {
    withTestApplication({ module(testing = true) }) {
      handleRequest(HttpMethod.Get, "/").apply {
        assertEquals(HttpStatusCode.OK, response.status())
        assertEquals("HELLO WORLD!", response.content)
      }
    }
  }

  @Test
  fun testWebsocket() {
    val testMessage = "TEST"
    withTestApplication({ module(testing = true) }) {
      handleWebSocketConversation("/chat") { incoming, outgoing ->
        outgoing.send(Frame.Text(testMessage))
        assertEquals(testMessage, (incoming.receive() as Frame.Text).readText())
      }
    }
  }
}
