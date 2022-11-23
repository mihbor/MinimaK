package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.serialization.kotlinx.bigdecimal.bigDecimalHumanReadableSerializerModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

val json = Json {
  ignoreUnknownKeys = true
  serializersModule = bigDecimalHumanReadableSerializerModule
}

fun JsonElement.jsonString(field: String) = (this as? JsonObject)?.get(field)?.jsonPrimitive?.content
