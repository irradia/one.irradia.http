package one.irradia.http.tests

import one.irradia.http.vanilla.HTTPClientsOkHTTP
import java.net.URI

object HTTPDemo {

  @JvmStatic
  fun main(args: Array<String>) {
    val clients = HTTPClientsOkHTTP()
    val client = clients.createClient()

    val response =
      client.get(URI.create("https://www.io7m.com/license/pd.txt"), offset = 10L)

    System.out.printf("response: %s", response)
  }
}