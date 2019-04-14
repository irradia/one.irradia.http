package one.irradia.http.api

import java.net.URI

/**
 * The result of an HTTP request.
 */

sealed class HTTPResult<T> {

  /**
   * The URI of the request.
   */

  abstract val uri: URI

  /**
   * The request succeeded.
   */

  data class HTTPOK<T>(
    override val uri: URI,

    /**
     * The length of the content in octets.
     */

    val contentLength: Long,

    /**
     * The headers returned by the server.
     */

    val headers: Map<String, List<String>>,

    /**
     * The message returned by the server.
     */

    val message: String,

    /**
     * The status code returned by the server.
     */

    val statusCode: Int,

    /**
     * The value returned by the server (typically an input stream).
     */

    val result: T)
    : HTTPResult<T>() {

    /**
     * Retrieve the content type returned by the server
     */

    val contentType: String?
      get() = this.headers.get("content-type")?.firstOrNull()

    /**
     * Retrieve the content type returned by the server, or `application/octet-stream` if no
     * content type was returned.
     */

    val contentTypeOrDefault: String
      get() = this.contentType ?: "application/octet-stream"

  }

  sealed class HTTPFailed<T> : HTTPResult<T>() {

    /**
     * An error code was returned by the server.
     */

    data class HTTPError<T>(
      override val uri: URI,
      /**
       * The length of the content in octets.
       */

      val contentLength: Long,

      /**
       * The headers returned by the server.
       */

      val headers: Map<String, List<String>>,

      /**
       * The message returned by the server.
       */

      val message: String,

      /**
       * The status code returned by the server.
       */

      val statusCode: Int,

      /**
       * The value returned by the server (typically an input stream).
       */

      val result: T)
      : HTTPFailed<T>() {

      /**
       * Retrieve the content type returned by the server
       */

      val contentType: String?
        get() = this.headers.get("content-type")?.firstOrNull()

      /**
       * Retrieve the content type returned by the server, or `application/octet-stream` if no
       * content type was returned.
       */

      val contentTypeOrDefault: String
        get() = this.contentType ?: "application/octet-stream"
    }

    /**
     * A failure was encountered before a connection could even be opened to the server.
     */

    data class HTTPFailure<T>(
      override val uri: URI,
      val exception: Exception)
      : HTTPFailed<T>()

  }
}