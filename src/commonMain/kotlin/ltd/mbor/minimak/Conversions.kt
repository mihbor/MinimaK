package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.serialization.kotlinx.bigdecimal.bigDecimalHumanReadableSerializerModule
import kotlinx.serialization.json.*

val json = Json {
  ignoreUnknownKeys = true
  serializersModule = bigDecimalHumanReadableSerializerModule
}

fun JsonElement.jsonString(field: String): String = checkNotNull(jsonObject[field]).jsonPrimitive.content
fun JsonElement.jsonStringOrNull(field: String): String? = ((this as? JsonObject)?.get(field) as? JsonPrimitive)?.content

fun JsonElement.jsonBoolean(field: String): Boolean = checkNotNull(jsonObject[field]).jsonPrimitive.boolean
fun JsonElement.jsonBooleanOrNull(field: String): Boolean? = ((this as? JsonObject)?.get(field) as? JsonPrimitive)?.boolean

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toHex(): String = asUByteArray().joinToString("") { it.toString(radix = 16).padStart(2, '0') }
fun String.decodeHex(): ByteArray {
  check(length % 2 == 0) { "Must have an even length" }
  
  return chunked(2)
    .map { it.toInt(16).toByte() }
    .toByteArray()
}
