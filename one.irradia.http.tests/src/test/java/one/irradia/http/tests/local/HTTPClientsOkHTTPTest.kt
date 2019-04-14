package one.irradia.http.tests.local

import one.irradia.http.api.HTTPClientProviderType
import one.irradia.http.tests.HTTPClientsContract
import one.irradia.http.vanilla.HTTPClientsOkHTTP
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HTTPClientsOkHTTPTest : HTTPClientsContract() {

  override fun logger(): Logger =
    LoggerFactory.getLogger(HTTPClientsOkHTTPTest::class.java)

  override fun clients(): HTTPClientProviderType {
    HTTPClientsOkHTTP.setClientCreator(HTTPClientsOkHTTP.defaultClientCreator)
    return HTTPClientsOkHTTP()
  }

  @Test
  fun testCrashyClients() {
    val clients = HTTPClientsOkHTTP()
    HTTPClientsOkHTTP.setClientCreator { throw UnsupportedOperationException() }

    this.expectedException.expect(UnsupportedOperationException::class.java)
    clients.createClient()
  }
}
