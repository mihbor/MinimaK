package ltd.mbor.minimak

import kotlinx.serialization.json.JsonElement

class SimulatedMDS: MdsApi {
  var payloads: MutableList<JsonElement?> = mutableListOf()
  var iterator = payloads.iterator()
  var capturedCommands = mutableListOf<String>()
  var capturedQueries = mutableListOf<String>()
  
  fun willReturn(payload: String): SimulatedMDS {
    payloads += payload.let(json::parseToJsonElement)
    iterator = payloads.iterator()
    return this
  }
  
  override var logging: Boolean = false
  
  override suspend fun cmd(command: String): JsonElement? {
    capturedCommands += command
    return iterator.next()
  }
  
  override suspend fun sql(query: String): JsonElement? {
    capturedQueries += query
    return iterator.next()
  }
}
