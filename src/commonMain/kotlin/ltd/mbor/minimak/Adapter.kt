@file:OptIn(ExperimentalSerializationApi::class)

package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

data class Result(
  val isSuccessful: Boolean,
  val message: String?
){
  companion object {
    val empty = Result(false, "")
  }
}

suspend fun MDS.getBlockNumber(): Int {
  val status = cmd("status")!!
  return status.jsonObject["response"]!!.jsonObject["chain"]!!.jsonObject["block"]!!.jsonPrimitive.int
}

suspend fun MDS.getBalances(address: String? = null): List<Balance> {
  val balance = cmd("balance ${address?.let{ "address:$address " } ?:""}")!!
  return json.decodeFromJsonElement(balance.jsonObject["response"]!!)
}

suspend fun MDS.getTokens(): List<Token> {
  val tokens = cmd("tokens")!!
  return json.decodeFromJsonElement(tokens.jsonObject["response"]!!)
}

suspend fun MDS.getScripts(): Map<String, String> {
  val scripts = cmd("scripts")!!
  val addresses = scripts.jsonObject["response"]!!.jsonArray
  return addresses.associate{ it.jsonString("address")!! to it.jsonString("script")!! }
}
suspend fun MDS.getAddress(): String {
  val getaddress = cmd("getaddress")!!
  return getaddress.jsonObject["response"]!!.jsonString("miniaddress")!!
}

suspend fun MDS.newAddress(): String {
  val newaddress = cmd("newaddress")!!
  return newaddress.jsonObject["response"]!!.jsonString("miniaddress")!!
}

suspend fun MDS.newKey(): String {
  val keys = cmd("keys action:new")!!
  return keys.jsonObject["response"]!!.jsonString("publickey")!!
}

suspend fun MDS.deployScript(text: String): String {
  val newscript = cmd("""newscript script:"$text" trackall:true""")!!
  return newscript.jsonObject["response"]!!.jsonString("address")!!
}

suspend fun MDS.getCoins(tokenId: String? = null, address: String? = null, coinId: String? = null, sendable: Boolean = false): List<Coin> {
  val coinSimple = cmd("coins ${tokenId?.let{"tokenid:$tokenId "} ?:""}${address?.let{"address:$address "} ?:""}${coinId?.let{" coinid:$it"} ?:""} sendable:$sendable")!!
  val coins = json.decodeFromJsonElement<List<Coin>>(coinSimple.jsonObject["response"]!!)
  return coins.sortedBy { it.amount }
}

suspend fun MDS.createToken(name: String, supply: BigDecimal, decimals: Int, imageUrl: String): Result {
  val result = cmd("""tokencreate name:{"name":"$name", "url":"$imageUrl"} amount:${supply.toPlainString()}${if (decimals > 0) ".$decimals" else ""}""")!!
  return Result(result.jsonBoolean("status")!!, result.jsonString("message"))
}

suspend fun MDS.signTx(txnId: Int, key: String): JsonElement? {
  val txncreator = "txnsign id:$txnId publickey:$key;"
  val result = cmd(txncreator)
  if (logging) log("import ${result?.jsonBoolean("status")}")
  return result
}

suspend fun MDS.post(txnId: Int): JsonElement? {
  val txncreator = "txnpost id:$txnId auto:true;"
  val result = cmd(txncreator)!!
  return result.jsonObject["response"]
}

suspend fun MDS.exportTx(txnId: Int): String {
  val txncreator = "txnexport id:$txnId;"
  val result = cmd(txncreator)!!
  return result.jsonObject["response"]!!.jsonString("data")!!
}

suspend fun MDS.importTx(txnId: Int, data: String): JsonObject {
  val txncreator = "txncreate id:$txnId;" +
    "txnimport id:$txnId data:$data;"
  val result = cmd(txncreator)!!.jsonArray
  val txnimport = result.find{ it.jsonString("command") == "txnimport" }!!
  if (logging) log("import ${txnimport.jsonBoolean("status")}")
  return json.decodeFromJsonElement(txnimport.jsonObject["response"]!!.jsonObject["transaction"]!!)
}

suspend fun MDS.exportCoin(coinId: String): String {
  val coinexport = cmd("coinexport coinid:$coinId")!!
  return coinexport.jsonString("response")!!
}

suspend fun MDS.importCoin(data: String) {
  val coinimport = cmd("coinimport data:$data")
}

suspend fun MDS.getContacts(): List<Contact> {
  val maxcontacts = cmd("maxcontacts")!!
  return json.decodeFromJsonElement(maxcontacts.jsonObject["response"]!!.jsonObject["contacts"]!!.jsonArray)
}

suspend fun MDS.addContact(maxiAddress: String): Contact? {
  val maxcontacts = cmd("maxcontacts action:add contact:$maxiAddress")!!
  return if (maxcontacts.jsonBoolean("status") == true)
    getContacts().first{ it.currentAddress == maxiAddress }
  else null
}

suspend fun MDS.sendMessage(app: String, publicKey: String, text: String): Boolean {
  val hex = "0x" + text.encodeToByteArray().toHex()
  val maxima = cmd("maxima action:send application:$app publickey:$publicKey data:$hex")!!
  log("sent: $text")
  return maxima.jsonBoolean("status") == true && maxima.jsonObject["response"]!!.jsonBoolean("delivered") == true
}
