package one.irradia.http.vanilla

import okhttp3.OkHttpClient
import one.irradia.http.api.HTTPClientProviderType
import one.irradia.http.api.HTTPClientType

/**
 * An HTTP client provider based on OkHttp.
 *
 * Note: This class MUST have a no-argument public constructor in order to work correctly with
 * [java.util.ServiceLoader].
 */

class HTTPClientsOkHTTP : HTTPClientProviderType {

  companion object {

    private val clientLock: Any = Object()
    private var client: HTTPClientOkHTTP? = null

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
      if (currentClient == null) {
        client = HTTPClientOkHTTP(userAgent, clients.invoke())
        client!!
      } else {
        currentClient
      }
    }
}
