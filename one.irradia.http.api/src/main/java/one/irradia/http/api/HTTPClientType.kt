package one.irradia.http.api

import java.io.Closeable
import java.io.InputStream
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
    offset: Long = 0L): HTTPResult<InputStream>

}