package one.irradia.http.api

/**
 * A provider of HTTP clients. Applications are encouraged to create one client and reuse it
 * for all HTTP requests.
 */

interface HTTPClientProviderType {

  /**
   * Create a new HTTP client.
   */

  fun createClient(
    userAgent: String? = null): HTTPClientType

}
