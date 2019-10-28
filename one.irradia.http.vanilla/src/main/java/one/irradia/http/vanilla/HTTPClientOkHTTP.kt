package one.irradia.http.vanilla

import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.internal.Version
import okhttp3.internal.http.HttpMethod
import one.irradia.http.api.HTTPAuthentication
import one.irradia.http.api.HTTPClientType
import one.irradia.http.api.HTTPResult
import one.irradia.http.api.HTTPResult.HTTPFailed.HTTPError
import one.irradia.http.api.HTTPResult.HTTPFailed.HTTPFailure
import one.irradia.http.api.HTTPResult.HTTPOK
import one.irradia.mime.api.MIMEType
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean

internal class HTTPClientOkHTTP(
  private val version: String,
  private val userAgent: String?,
  private val client: OkHttpClient) : HTTPClientType {

  private val logger = LoggerFactory.getLogger(HTTPClientOkHTTP::class.java)
  private val closed = AtomicBoolean(false)

  private fun userAgentString(): String {
    return if (this.userAgent != null) {
      "${this.userAgent} one.irradia.http.vanilla/${version} ${Version.userAgent()}"
    } else {
      "one.irradia.http.vanilla/${version} ${Version.userAgent()}"
    }
  }

  override fun close() {
    if (closed.compareAndSet(false, true)) {
      this.client.dispatcher().executorService().shutdown()
      this.client.connectionPool().evictAll()
      this.client.cache()?.close()
    }
  }

  override fun request(
    uri: URI,
    method: String,
    authentication: (URI) -> HTTPAuthentication?,
    offset: Long,
    contentType: MIMEType?,
    body: ByteArray?): HTTPResult<InputStream> {
    this.logger.debug("{} {} (offset {})", method, uri, offset)

    val builder = Request.Builder()
    builder.url(uri.toString())
    builder.header("User-Agent", userAgentString())

    if (offset > 0L) {
      builder.header("Range", "bytes=${offset}-")
    }

    val bodyActual : ByteArray? =
      if (HttpMethod.requiresRequestBody(method) && body == null) {
        ByteArray(0)
      } else {
        body
      }

    builder.method(method, mapBody(contentType, bodyActual))
    return call(builder.build(), authentication, uri)
  }

  private fun mapBody(contentType: MIMEType?, body: ByteArray?): RequestBody? {
    return if (body != null) {
      val mediaType =
        if (contentType != null) {
          MediaType.parse(contentType.fullType)
        } else {
          MediaType.get("application/octet-stream")
        }
      RequestBody.create(mediaType, body)
    } else {
      null
    }
  }

  private fun call(
    request: Request,
    authentication: (URI) -> HTTPAuthentication?,
    uri: URI
  ): HTTPResult<InputStream> {
    return try {
      val response =
        this.client.newBuilder()
          .authenticator({ route, response -> this.authenticator(route, response, authentication) })
          .build()
          .newCall(request)
          .execute()

      val contentLength = contentLengthOf(response)
      this.logger.trace("content-length: {}", contentLength)

      val code = response.code()
      if (code >= 400) {
        HTTPError(
          uri = uri,
          contentLength = contentLength,
          headers = response.headers().toMultimap(),
          message = response.message(),
          statusCode = code,
          result = response.body()?.byteStream() ?: ByteArrayInputStream(ByteArray(0)))
      } else {
        HTTPOK(
          uri = uri,
          contentLength = contentLength,
          headers = response.headers().toMultimap(),
          message = response.message(),
          statusCode = code,
          result = response.body()?.byteStream() ?: ByteArrayInputStream(ByteArray(0)))
      }
    } catch (e: Exception) {
      HTTPFailure(
        uri = uri,
        exception = e)
    }
  }

  private fun contentLengthOf(response: Response): Long? {
    return try {
      val headers = response.headers()
      for (header in headers.names()) {
        if ("CONTENT-LENGTH".equals(header.toUpperCase())) {
          val value = headers.get(header)
          return value?.toLong()
        }
      }
      null
    } catch (e: Exception) {
      this.logger.error("error retrieving content length: ", e)
      null
    }
  }

  private fun authenticator(
    route: Route?,
    response: Response,
    authentication: (URI) -> HTTPAuthentication?): Request? {

    val currentURI = response.request().url().uri()
    if (response.request().header("Authorization") != null) {
      this.logger.debug("{}: already tried to authorize, giving up!", currentURI)
      return null
    }

    return when (val credentials = authentication.invoke(currentURI)) {
      null -> {
        this.logger.debug("{}: no credentials provided, aborting", currentURI)
        null
      }

      is HTTPAuthentication.HTTPAuthenticationBasic -> {
        val challenge =
          response.challenges()
            .find { challenge -> challenge.scheme() == "Basic" }
        if (challenge != null) {
          this.logger.debug("{}: retrying with Basic auth", currentURI)

          val credential = Credentials.basic(credentials.userName, credentials.password)
          return response.request()
            .newBuilder()
            .header("Authorization", credential)
            .build()
        } else {
          this.logger.debug("{}: no credentials can satisfy challenges ({})",
            currentURI,
            response.challenges().joinToString { c -> c.scheme() })
          null
        }
      }

      is HTTPAuthentication.HTTPAuthenticationOAuth -> {
        val challenge =
          response.challenges()
            .find { challenge -> challenge.scheme() == "Bearer" }
        if (challenge != null) {
          this.logger.debug("{}: retrying with Bearer auth", currentURI)

          return response.request()
            .newBuilder()
            .header("Authorization", "Bearer " + credentials.token)
            .build()
        } else {
          this.logger.debug("{}: no credentials can satisfy challenges ({})",
            currentURI,
            response.challenges().joinToString { c -> c.scheme() })
          null
        }
      }
    }
  }
}
