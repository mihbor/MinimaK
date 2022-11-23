@file:OptIn(ExperimentalSerializationApi::class)

package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.random.Random

data class Result(
  val isSuccessful: Boolean,
  val message: String?
){
  companion object {
    val empty = Result(false, "")
  }
}

suspend fun MDS.getBlockNumber(): Int {
  val status = MDS.cmd("status")!!
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
  val getaddress = MDS.cmd("getaddress")!!
  return getaddress.jsonObject["response"]!!.jsonString("miniaddress")!!
}

suspend fun MDS.newAddress(): String {
  val newaddress = cmd("newaddress")!!
  return newaddress.jsonObject["response"]!!.jsonString("miniaddress")!!
}

suspend fun MDS.newKey(): String {
  val keys = MDS.cmd("keys action:new")!!
  return keys.jsonObject["response"]!!.jsonString("publickey")!!
}

suspend fun MDS.deployScript(text: String): String {
  val newscript = MDS.cmd("""newscript script:"$text" trackall:true""")!!
  return newscript.jsonObject["response"]!!.jsonString("address")!!
}

suspend fun MDS.getCoins(tokenId: String? = null, address: String? = null, sendable: Boolean = false): List<Coin> {
  val coinSimple = cmd("coins ${tokenId?.let{"tokenid:$tokenId "} ?:""} ${address?.let{"address:$address "} ?:""}sendable:$sendable")!!
  val coins = json.decodeFromJsonElement<List<Coin>>(coinSimple.jsonObject["response"]!!)
  return coins.sortedBy { it.amount }
}

suspend fun MDS.transact(inputCoinIds: List<String>, outputs: List<Output>): Result {
  val txId = Random.nextInt(1000000000)
  cmd("txncreate id:$txId")
  inputCoinIds.forEach {
    cmd("txninput id:$txId coinid:$it")
  }
  outputs.forEach {
    cmd("txnoutput id:$txId amount:${it.amount.toPlainString()} address:${it.address} tokenid:${it.tokenId}")
  }
  cmd("txnsign id:$txId publickey:auto")
  val result = cmd("txnpost id:$txId auto:true")!!
  cmd("txndelete id:$txId")
  return Result(result.jsonObject["status"]!!.jsonPrimitive.boolean, result.jsonString("message"))
}

suspend fun MDS.createToken(name: String, supply: BigDecimal, decimals: Int, imageUrl: String): Result {
  val result = cmd("""tokencreate name:{"name":"$name", "url":"$imageUrl"} amount:${supply.toPlainString()}${if (decimals > 0) ".$decimals" else ""}""")!!
  return Result(result.jsonObject["status"]!!.jsonPrimitive.boolean, result.jsonString("message"))
}

suspend fun MDS.signTx(txnId: Int, key: String): JsonElement? {
  val txncreator = "txnsign id:$txnId publickey:$key;"
  val result = cmd(txncreator)
  if (logging) log("import ${result?.jsonString("status")}")
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
  if (logging) log("import ${txnimport.jsonString("status")}")
  return json.decodeFromJsonElement(txnimport.jsonObject["response"]!!.jsonObject["transaction"]!!)
}
