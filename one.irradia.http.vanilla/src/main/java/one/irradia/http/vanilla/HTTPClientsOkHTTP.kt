package one.irradia.http.vanilla

import okhttp3.OkHttpClient
import one.irradia.http.api.HTTPClientProviderType
import one.irradia.http.api.HTTPClientType
import java.util.Properties

/**
 * An HTTP client provider based on OkHttp.
 *
 * Note: This class MUST have a no-argument public constructor in order to work correctly with
 * [java.util.ServiceLoader].
 */

class HTTPClientsOkHTTP : HTTPClientProviderType {

  companion object {

    private val version: String = loadVersion()

    private fun loadVersion(): String {
      return HTTPClientsOkHTTP::class.java.getResourceAsStream(
        "/one/irradia/http/vanilla/version.properties")
        .use { stream ->
          val properties = Properties()
          properties.load(stream)
          properties.getProperty("version")
        }
    }

    private val clientLock: Any = Object()
    private var client: OkHttpClient? = null

    /**
     * The default client creator.
     */

    val defaultClientCreator = { OkHttpClient() }

    @JvmStatic
    @Volatile
    private var clients = this.defaultClientCreator

    /**
     * Set the function used to create OkHttp clients for this service provider.
     */

    fun setClientCreator(creator: () -> OkHttpClient) {
      this.clients = creator
    }
  }

  override fun createClient(userAgent: String?): HTTPClientType =
    synchronized(clientLock) {
      val currentClient = client
      HTTPClientOkHTTP(
        version = version,
        userAgent = userAgent,
        client = if (currentClient == null) {
          client = clients.invoke()
          client!!
        } else {
          currentClient
        })
    }
}
