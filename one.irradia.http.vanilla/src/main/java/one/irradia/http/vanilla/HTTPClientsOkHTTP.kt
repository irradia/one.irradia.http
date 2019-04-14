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

    /**
     * The default client creator.
     */

    val defaultClientCreator = { OkHttpClient() }

    @JvmStatic
    @Volatile
    private var clients  = this.defaultClientCreator

    /**
     * Set the function used to create OkHttp clients for this service provider.
     */

    fun setClientCreator(creator: () -> OkHttpClient) {
      this.clients = creator
    }
  }

  override fun createClient(userAgent: String?): HTTPClientType =
    HTTPClientOkHTTP(userAgent, clients.invoke())

}
