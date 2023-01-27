package ltd.mbor.minimak

import kotlinx.serialization.json.JsonElement

object SimulatedMDS: MdsApi {
  var payload: JsonElement? = null
  
  fun willReturn(payload: String): SimulatedMDS {
    this.payload = json.parseToJsonElement(payload)
    return this
  }
  
  override var logging: Boolean = false
  
  override suspend fun cmd(command: String): JsonElement? {
    return payload
  }
  
  override suspend fun sql(query: String): JsonElement? {
    return payload
  }
}
