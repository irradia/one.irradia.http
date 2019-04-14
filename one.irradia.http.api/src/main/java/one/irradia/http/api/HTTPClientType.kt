package one.irradia.http.api

import one.irradia.mime.api.MIMEType
import java.io.Closeable
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.net.URI

/**
 * An extremely simple HTTP interface designed to be easy to mock.
 */

interface HTTPClientType : Closeable {

  /**
   * Perform a GET request.
   */

  fun get(
    uri: URI,
    authentication: (URI) -> HTTPAuthentication? = { null },
    offset: Long = 0L): HTTPResult<InputStream> =
    this.request(
      uri = uri,
      method = "GET",
      authentication = authentication,
      offset = offset)

  /**
   * Perform a HEAD request.
   */

  fun head(
    uri: URI,
    authentication: (URI) -> HTTPAuthentication? = { null },
    offset: Long = 0L): HTTPResult<InputStream> =
    this.request(
      uri = uri,
      method = "HEAD",
      authentication = authentication,
      offset = offset)

  /**
   * Perform a PUT request.
   */

  fun put(
    uri: URI,
    authentication: (URI) -> HTTPAuthentication? = { null },
    offset: Long = 0L,
    contentType: MIMEType? = null,
    body: ByteArray): HTTPResult<InputStream> =
    this.request(
      uri = uri,
      method = "PUT",
      authentication = authentication,
      offset = offset,
      contentType = contentType,
      body = body)

  /**
   * Perform a POST request.
   */

  fun post(
    uri: URI,
    authentication: (URI) -> HTTPAuthentication? = { null },
    offset: Long = 0L,
    contentType: MIMEType? = null,
    body: ByteArray): HTTPResult<InputStream> =
    this.request(
      uri = uri,
      method = "POST",
      authentication = authentication,
      offset = offset,
      contentType = contentType,
      body = body)

  /**
   * Perform a generic request.
   *
   * @throws IllegalArgumentException On invalid combinations of parameters
   */

  @Throws(IllegalArgumentException::class)
  fun request(
    uri: URI,
    method: String,
    authentication: (URI) -> HTTPAuthentication? = { null },
    offset: Long = 0L,
    contentType: MIMEType? = null,
    body: ByteArray? = null): HTTPResult<InputStream>

}