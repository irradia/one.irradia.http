package one.irradia.http.tests.device

import android.support.test.filters.MediumTest
import android.support.test.runner.AndroidJUnit4
import one.irradia.http.api.HTTPClientProviderType
import one.irradia.http.tests.HTTPClientsContract
import one.irradia.http.vanilla.HTTPClientsOkHTTP
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@RunWith(AndroidJUnit4::class)
@MediumTest
class HTTPClientsOkHTTPTest : HTTPClientsContract() {

  override fun logger(): Logger =
    LoggerFactory.getLogger(HTTPClientsOkHTTPTest::class.java)

  override fun clients(): HTTPClientProviderType =
    HTTPClientsOkHTTP()

}
