package one.irradia.http.api

/**
 * The various types of HTTP authentication.
 */

sealed class HTTPAuthentication {

  /**
   * Basic authentication.
   */

  data class HTTPAuthenticationBasic(
    val userName: String,
    val password: String)
    : HTTPAuthentication()

  /**
   * Token-based authentication.
   */

  data class HTTPAuthenticationOAuth(
    val token: String)
    : HTTPAuthentication()

}