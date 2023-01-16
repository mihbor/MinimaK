package ltd.mbor.minimak

import kotlinx.serialization.json.JsonElement

object SimulatedMDS: MDSInterface {
  var payload: JsonElement? = null
  
  fun willReturn(payload: String): SimulatedMDS {
    this.payload = json.parseToJsonElement(payload)
    return this
  }
  override suspend fun cmd(command: String): JsonElement? {
    return payload
  }
  
  override suspend fun sql(command: String): JsonElement? {
    return payload
  }
}
