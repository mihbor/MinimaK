package ltd.mbor.minimak

import io.ktor.client.*
import io.ktor.client.engine.js.*
import kotlin.js.Date

actual external fun encodeURIComponent(data: String): String

/**
 * Log some data with a timestamp in a consistent manner to the console
 */
actual fun log(output: String){
  console.log("Minima @ ${Date().toLocaleString()} : $output")
}

actual fun createClient() = HttpClient(Js)
