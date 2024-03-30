package ltd.mbor.minimak

import io.ktor.client.*
import io.ktor.client.engine.js.*
import kotlinx.datetime.Clock

actual external fun encodeURIComponent(data: String): String

@JsFun("(output) => console.log(output)")
external fun consoleLog(vararg output: JsAny?)

actual fun log(output: String){
  try {
    consoleLog("Minima @ ${Clock.System.now()} : $output".toJsString())
  } catch (e: Throwable) {
    // can't do anything if console is not defined in the environment
  }
}

actual fun createClient() = HttpClient(Js)
