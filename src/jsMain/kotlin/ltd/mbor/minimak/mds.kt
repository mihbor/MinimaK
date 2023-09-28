package ltd.mbor.minimak

import io.ktor.client.*
import io.ktor.client.engine.js.*
import kotlin.js.Date

actual external fun encodeURIComponent(data: String): String

actual fun log(output: String){
  try {
    console.log("Minima @ ${Date().toLocaleString()} : $output")
  } catch (e: Throwable) {
    // can't do anything if console is not defined in the environment
  }
}

actual fun createClient() = HttpClient(Js)
