package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.serialization.kotlinx.bigdecimal.bigDecimalHumanReadableSerializerModule
import kotlinx.serialization.json.*

val json = Json {
  ignoreUnknownKeys = true
  serializersModule = bigDecimalHumanReadableSerializerModule
}

fun JsonElement.jsonString(field: String) = (this as? JsonObject)?.get(field)?.jsonPrimitive?.content
fun JsonElement.jsonBoolean(field: String) = (this as? JsonObject)?.get(field)?.jsonPrimitive?.boolean

fun ByteArray.toHex(): String = asUByteArray().joinToString("") { it.toString(radix = 16).padStart(2, '0') }
fun String.decodeHex(): ByteArray {
  check(length % 2 == 0) { "Must have an even length" }
  
  return chunked(2)
    .map { it.toInt(16).toByte() }
    .toByteArray()
}
