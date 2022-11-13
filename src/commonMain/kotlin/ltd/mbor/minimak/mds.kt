package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.serialization.kotlinx.bigdecimal.bigDecimalHumanReadableSerializerModule
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import kotlin.coroutines.coroutineContext

typealias Callback = (suspend (JsonElement) -> Unit)?

val json = Json {
  ignoreUnknownKeys = true
  serializersModule = bigDecimalHumanReadableSerializerModule
}

expect fun encodeURIComponent(data: String): String

var MDS_MAIN_CALLBACK: Callback = null

expect fun createClient(): HttpClient

/**
 * Log some data with a timestamp in a consistent manner to the console
 */
expect fun log(output: String)

object MDS {
  
  //RPC Host for Minima
  var mainhost = ""
  
  //The MiniDAPP UID
  var minidappuid: String? = null
  
  //Is logging RPC enabled
  var logging = false
  
  val client = createClient()
  /**
   * Minima Startup - with the callback function used for all Minima messages
   */
  suspend fun init(minidappuid: String, host: String, port: Int, callback: Callback = null) {
    this.minidappuid = minidappuid
    log("Initialising MDS [$minidappuid]")
    
    mainhost 	= "https://$host:$port/"
    log("MDS MAINHOST : "+ mainhost)
    
    //Store this for poll messages
    MDS_MAIN_CALLBACK = callback
    
    //Start the Long Poll listener
    with(CoroutineScope(coroutineContext)) {
      launch {
        PollListener()
      }
    }
    
    log("MDS init complete")
    MDSPostMessage(JsonObject(mapOf("event" to JsonPrimitive("inited"))))
  }
  
  /**
   * Runs a function on the Minima Command Line - same format as Minima
   */
  suspend fun cmd(command: String) =
    httpPostAsync("${mainhost}cmd?uid=$minidappuid", command)
  
  /**
   * Runs a SQL command on this MiniDAPPs SQL Database
   */
  suspend fun sql(command: String) =
    httpPostAsync("${mainhost}sql?uid=$minidappuid", command)
      ?: if(command.startsWith("SELECT", ignoreCase = true)) httpPostAsync("${mainhost}sql?uid=$minidappuid", command)
      else null
  
  
  /**
   * Post a message to the Minima Event Listeners
   */
  private suspend fun MDSPostMessage(data: JsonElement){
    MDS_MAIN_CALLBACK?.invoke(data)
  }
  
  var PollCounter = 0
  var PollSeries  = ""

//@Serializable
//data class Msg(
//  val series: String,
//  val counter: Int,
//  val status: Boolean,
//  val message: dynamic? = null,
//  val response: Msg? = null
//)
  
  private suspend fun PollListener() {
    
    val pollhost = "${MDS.mainhost}poll?uid=${MDS.minidappuid}"
    val polldata = "series=$PollSeries&counter=$PollCounter"
    
    httpPostAsyncPoll(pollhost, polldata) { msg: JsonElement ->
      //Are we on the right Series
      if (PollSeries != msg.jsonObject["series"]?.jsonPrimitive?.content) {
        
        //Reset to the right series.
        PollSeries = msg.jsonObject["series"]!!.jsonPrimitive.content
        PollCounter = msg.jsonObject["counter"]!!.jsonPrimitive.int
        
      } else {
        
        //Is there a message ?
        val response = msg.jsonObject.get("response")?.jsonObject
        if (msg.jsonObject["status"]!!.jsonPrimitive.boolean == true && response?.get("message") != null) {
          
          //Get the current counter
          PollCounter = (response.get("counter")?.jsonPrimitive?.int?:0) + 1
          
          MDSPostMessage(response.get("message")!!)
        }
      }
      
      //And around we go again
      PollListener()
    }
  }
  
  /**
   * Utility function for GET request
   *
   * @param theUrl
   * @param callback
   * @param params
   * @returns
   */
  private suspend fun httpPostAsync(theUrl: String, params: String): JsonElement? {
    log("POST_RPC:$theUrl PARAMS:$params")
    
    val response = try {
      client.post(theUrl) {
        headers {
          append(HttpHeaders.Connection, "close")
          //      append(HttpHeaders.ContentType, "text/plain; charset=UTF-8")
        }
        setBody(encodeURIComponent(params))
      }
    } catch (e: HttpRequestTimeoutException) {
      log("timeout: $e")
      null
    }
    return response?.takeIf { it.status.isSuccess() }?.let {
      log("STATUS: ${response.status}; RESPONSE:${response.bodyAsText()}");
      
      json.parseToJsonElement(response.bodyAsText())
    }
  }
  
  private suspend fun httpPostAsyncPoll(theUrl: String, params: String, callback: Callback){
    if(logging){
      log("POST_POLL_RPC:$theUrl PARAMS:$params")
    }
    
    try {
      val response = client.post(theUrl) {
        headers {
          append(HttpHeaders.Connection, "close")
//        append("X-PINGOTHER", "pingpong")
//        append(HttpHeaders.ContentType, "text/plain; charset=UTF-8")
        }
        setBody(encodeURIComponent(params))
      }
      if (response.status.isSuccess()) {
        if(logging){
          log("STATUS: ${response.status}; RESPONSE:${response.bodyAsText()}")
        }
        callback?.invoke(json.parseToJsonElement(response.bodyAsText()))
      } else {
        log("STATUS: ${response.status}; RESPONSE:${response.bodyAsText()}")
        log("Error Polling $theUrl - reconnect in 10s")
        delay(10000)
        PollListener()
      }
    } catch (e: Exception) {
      log("Error Polling - reconnect in 10s")
      delay(10000)
      PollListener()
    }
  }
}
