package ltd.mbor.minimak

import io.ktor.client.*
import io.ktor.client.engine.curl.*
import io.ktor.http.*

actual fun encodeURIComponent(data: String): String {
  return data.encodeURLParameter(spaceToPlus = true)
}

actual fun createClient(): HttpClient {
  return HttpClient(Curl) {
    engine {
      sslVerify = false
    }
  }
}

/**
 * Log some data with a timestamp in a consistent manner to the console
 */
actual fun log(output: String) {
  println(output)
}