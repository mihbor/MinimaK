package ltd.mbor.minimak

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Date
import kotlin.Result

actual external fun encodeURIComponent(data: String): String

actual fun log(output: String){
  try {
    console.log("Minima @ ${Date().toLocaleString()} : $output")
  } catch (e: Throwable) {
    // can't do anything if console is not defined in the environment
  }
}

actual fun createClient() = HttpClient(Js)

@JsName("MDS")
external object DynamicMDS {
  fun init(callback: (dynamic) -> Unit)
  fun log(output: String)
  fun sql(query: String, callback: (dynamic) -> Unit)
  fun cmd(command: String, callback: (dynamic) -> Unit)
  object net {
    fun GET(url: String, callback: (dynamic) -> Unit)
    fun POST(url: String, data: String, callback: (dynamic) -> Unit)
  }
}

object ServiceMDS: MdsApi {
  override var logging: Boolean = false

  fun init(callback: (JsonElement) -> Unit) = DynamicMDS.init{ msg: dynamic ->
    callback(json.decodeFromString<JsonElement>(JSON.stringify(msg)))
  }

  fun log(output: String) = DynamicMDS.log(output)

  override suspend fun cmd(command: String): JsonElement? {
    return suspendCoroutine { cont ->
      DynamicMDS.cmd(command) { result ->
        cont.resumeWith(Result.success(json.decodeFromString<JsonElement>(JSON.stringify(result))))
      }
    }
  }

  override suspend fun sql(query: String): JsonElement? {
    return suspendCoroutine { cont ->
      DynamicMDS.sql(query) { result ->
        cont.resumeWith(Result.success(json.decodeFromString<JsonElement>(JSON.stringify(result))))
      }
    }
  }

  override suspend fun get(url: String): JsonElement? {
    return suspendCoroutine { cont ->
      DynamicMDS.net.GET(url) { result ->
        cont.resumeWith(Result.success(json.decodeFromString<JsonElement>(JSON.stringify(result))))
      }
    }
  }

  override suspend fun post(url: String, data: String): JsonElement? {
    return suspendCoroutine { cont ->
      DynamicMDS.net.POST(url, data) { result ->
        cont.resumeWith(Result.success(json.decodeFromString<JsonElement>(JSON.stringify(result))))
      }
    }
  }
}
