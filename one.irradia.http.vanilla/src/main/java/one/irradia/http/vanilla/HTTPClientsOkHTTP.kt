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

  override fun createClient(): HTTPClientType =
    HTTPClientOkHTTP(OkHttpClient())

}
