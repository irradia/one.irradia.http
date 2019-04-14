package one.irradia.http.tests

import fi.iki.elonen.NanoHTTPD
import one.irradia.http.api.HTTPAuthentication
import one.irradia.http.api.HTTPAuthentication.*
import one.irradia.http.api.HTTPClientProviderType
import one.irradia.http.api.HTTPResult
import org.hamcrest.core.IsInstanceOf
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.slf4j.Logger
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI

abstract class HTTPClientsContract {

  protected abstract fun clients(): HTTPClientProviderType

  protected abstract fun logger(): Logger

  private lateinit var clients: HTTPClientProviderType
  private lateinit var logger: Logger
  private var httpd: NanoHTTPD? = null

  @Before
  fun testSetup() {
    this.clients = this.clients()
    this.logger = this.logger()
  }

  @After
  fun testTeardown() {
    this.httpd?.stop()
  }

  @Test
  fun testCreate() {
    val client = this.clients.createClient()
    client.close()
  }

  @Test
  fun testGET() {
    val responses =
      mapOf<URI, MutableList<ServerResponse>>(
        Pair(
          URI.create("/file.txt"),
          mutableListOf(ServerResponse(
            code = 200,
            type = "text/plain",
            data = ByteArrayInputStream("Hello".toByteArray()),
            size = 5L))))

    this.clients.createClient().use { client ->
      this.httpd = createHTTPD(this.logger, responses)

      val response =
        client.get(URI.create("http://127.0.0.1:30000/file.txt"))

      dumpResponse(response)

      val success = response as HTTPResult.HTTPOK
      Assert.assertEquals("Hello", String(success.result.readBytes()))
      Assert.assertEquals("text/plain", success.contentTypeOrDefault)
    }
  }

  @Test
  fun testGETAuthenticatedBasic() {
    val responses =
      mapOf<URI, MutableList<ServerResponse>>(
        Pair(
          URI.create("/file.txt"),
          mutableListOf(

            ServerResponse(
              code = 401,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L,
              headers = mapOf(
                Pair("WWW-Authenticate", "Basic realm=\"Whatever\"")
              )),

            ServerResponse(
              checkHeaders = { headers ->
                val authHeader = headers["authorization"]
                Assert.assertNotNull("Auth header provided", authHeader)
                Assert.assertEquals("Correct header value", "Basic cGVyc29uOnBhc3N3b3Jk", authHeader)
              },
              code = 200,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L))))

    this.clients.createClient().use { client ->
      this.httpd = createHTTPD(this.logger, responses)

      val response =
        client.get(URI.create("http://127.0.0.1:30000/file.txt"),
          authentication = {
            HTTPAuthenticationBasic(
              userName = "person",
              password = "password")
          })

      dumpResponse(response)

      val success = response as HTTPResult.HTTPOK
      Assert.assertEquals("Hello", String(success.result.readBytes()))
    }
  }

  @Test
  fun testGETAuthenticatedBasicButWrong() {
    val responses =
      mapOf<URI, MutableList<ServerResponse>>(
        Pair(
          URI.create("/file.txt"),
          mutableListOf(
            ServerResponse(
              code = 401,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L,
              headers = mapOf(
                Pair("WWW-Authenticate", "Basic realm=\"Whatever\"")
              )),

            ServerResponse(
              checkHeaders = { headers ->
                val authHeader = headers["authorization"]
                Assert.assertNotNull("Auth header provided", authHeader)
                Assert.assertEquals("Correct header value", "Basic cGVyc29uOnBhc3N3b3Jk", authHeader)
              },
              code = 401,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L),

            ServerResponse(
              checkHeaders = { headers ->
                val authHeader = headers["authorization"]
                Assert.assertNotNull("Auth header provided", authHeader)
                Assert.assertEquals("Correct header value", "Basic cGVyc29uOnBhc3N3b3Jk", authHeader)
              },
              code = 401,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L))))

    this.clients.createClient().use { client ->
      this.httpd = createHTTPD(this.logger, responses)

      val response =
        client.get(URI.create("http://127.0.0.1:30000/file.txt"),
          authentication = {
            HTTPAuthenticationBasic(
              userName = "person",
              password = "password")
          })

      dumpResponse(response)

      val failure = response as HTTPResult.HTTPFailed.HTTPError
      Assert.assertEquals(401, failure.statusCode)
      Assert.assertEquals("Hello", String(failure.result.readBytes()))
    }
  }

  @Test
  fun testGETAuthenticatedToken() {
    val responses =
      mapOf<URI, MutableList<ServerResponse>>(
        Pair(
          URI.create("/file.txt"),
          mutableListOf(

            ServerResponse(
              code = 401,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L,
              headers = mapOf(
                Pair("WWW-Authenticate", "Bearer realm=\"Whatever\"")
              )),

            ServerResponse(
              checkHeaders = { headers ->
                val authHeader = headers["authorization"]
                Assert.assertNotNull("Auth header provided", authHeader)
                Assert.assertEquals("Correct header value", "Bearer abcd1234", authHeader)
              },
              code = 200,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L))))

    this.clients.createClient().use { client ->
      this.httpd = createHTTPD(this.logger, responses)

      val response =
        client.get(URI.create("http://127.0.0.1:30000/file.txt"),
          authentication = {
            HTTPAuthenticationOAuth(token = "abcd1234")
          })

      dumpResponse(response)

      val success = response as HTTPResult.HTTPOK
      Assert.assertEquals("Hello", String(success.result.readBytes()))
    }
  }

  @Test
  fun testGETAuthenticatedNotProvided() {
    val responses =
      mapOf<URI, MutableList<ServerResponse>>(
        Pair(
          URI.create("/file.txt"),
          mutableListOf(

            ServerResponse(
              code = 401,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L,
              headers = mapOf(
                Pair("WWW-Authenticate", "Bearer realm=\"Whatever\"")
              )),

            ServerResponse(
              code = 401,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L))))

    this.clients.createClient().use { client ->
      this.httpd = createHTTPD(this.logger, responses)

      val response =
        client.get(URI.create("http://127.0.0.1:30000/file.txt"))

      dumpResponse(response)

      val failure = response as HTTPResult.HTTPFailed.HTTPError
      Assert.assertEquals(401, failure.statusCode)
      Assert.assertEquals("Hello", String(failure.result.readBytes()))
    }
  }

  @Test
  fun testGETAuthenticatedUnsupportedOnlyBearer() {
    val responses =
      mapOf<URI, MutableList<ServerResponse>>(
        Pair(
          URI.create("/file.txt"),
          mutableListOf(

            ServerResponse(
              code = 401,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L,
              headers = mapOf(
                Pair("WWW-Authenticate", "Magic realm=\"Whatever\"")
              )),

            ServerResponse(
              code = 401,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L))))

    this.clients.createClient().use { client ->
      this.httpd = createHTTPD(this.logger, responses)

      val response =
        client.get(
          URI.create("http://127.0.0.1:30000/file.txt"),
          authentication = {
            HTTPAuthenticationOAuth(token = "abcd1234")
          })

      dumpResponse(response)

      val failure = response as HTTPResult.HTTPFailed.HTTPError
      Assert.assertEquals(401, failure.statusCode)
      Assert.assertEquals("Hello", String(failure.result.readBytes()))
    }
  }

  @Test
  fun testGETAuthenticatedUnsupportedOnlyBasic() {
    val responses =
      mapOf<URI, MutableList<ServerResponse>>(
        Pair(
          URI.create("/file.txt"),
          mutableListOf(

            ServerResponse(
              code = 401,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L,
              headers = mapOf(
                Pair("WWW-Authenticate", "Magic realm=\"Whatever\"")
              )),

            ServerResponse(
              code = 401,
              type = "text/plain",
              data = ByteArrayInputStream("Hello".toByteArray()),
              size = 5L))))

    this.clients.createClient().use { client ->
      this.httpd = createHTTPD(this.logger, responses)

      val response =
        client.get(
          URI.create("http://127.0.0.1:30000/file.txt"),
          authentication = {
            HTTPAuthenticationBasic(
              userName = "person",
              password = "password")
          })

      dumpResponse(response)

      val failure = response as HTTPResult.HTTPFailed.HTTPError
      Assert.assertEquals(401, failure.statusCode)
      Assert.assertEquals("Hello", String(failure.result.readBytes()))
    }
  }

  @Test
  fun testGETFailure() {
    this.clients.createClient().use { client ->
      val response =
        client.get(
          URI.create("http://127.0.0.1:30000/file.txt"),
          authentication = {
            HTTPAuthenticationBasic(
              userName = "person",
              password = "password")
          })

      dumpResponse(response)

      val failure = response as HTTPResult.HTTPFailed.HTTPFailure
      Assert.assertThat(failure.exception, IsInstanceOf(IOException::class.java))
    }
  }

  private fun dumpResponse(response: HTTPResult<InputStream>) {
    System.out.println("response: $response")
    this.logger.debug("{}", response)
  }

  data class ServerResponse(
    val code: Int,
    val type: String,
    val data: InputStream,
    val size: Long,
    val headers: Map<String, String> = mapOf(),
    val checkHeaders: (Map<String, String>) -> Unit = { })

  private fun createHTTPD(
    logger: Logger,
    responses: Map<URI, MutableList<ServerResponse>>): NanoHTTPD? {
    return object : NanoHTTPD("127.0.0.1", 30000) {
      init {
        this.start()
        for (t in 0..20L) {
          if (this.isAlive) {
            break
          }
          Thread.sleep(100L)
        }
        if (!this.isAlive) {
          throw IOException("Could not start server")
        }
      }

      override fun serve(session: IHTTPSession): Response {
        logger.debug("serve: {}", session.uri)

        session.headers.keys.forEach { name ->
          logger.debug("header: {} -> {}", name, session.headers[name])
        }

        val uri = URI.create(session.uri)
        if (responses.containsKey(uri)) {
          val response = responses[uri]!!.removeAt(0)

          response.checkHeaders.invoke(session.headers)

          val serverResponse =
            newFixedLengthResponse(
              Response.Status.lookup(response.code),
              response.type,
              response.data,
              response.size)

          response.headers.keys.forEach { key ->
            serverResponse.addHeader(key, response.headers[key])
          }

          return serverResponse
        }

        throw IOException("No response!")
      }
    }
  }
}
