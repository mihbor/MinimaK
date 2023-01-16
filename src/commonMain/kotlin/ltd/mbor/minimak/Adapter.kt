package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.json.*

data class Result(
  val isSuccessful: Boolean,
  val message: String? = null
) {
  companion object {
    val empty = Result(false, null)
  }
}

suspend fun MDS.getBlockNumber(): Int {
  val status = cmd("status")!!
  return status.jsonObject["response"]!!.jsonObject["chain"]!!.jsonObject["block"]!!.jsonPrimitive.int
}

suspend fun MDS.getBalances(address: String? = null, confirmations: Int? = null): List<Balance> {
  val balance = cmd("balance ${address?.let{ "address:$address " } ?:""}${confirmations?.let{ "confirmations:$confirmations " } ?:""}")!!
  return json.decodeFromJsonElement(balance.jsonObject["response"]!!)
}

suspend fun MDS.getTokens(): List<Token> {
  val tokens = cmd("tokens")!!
  return json.decodeFromJsonElement(tokens.jsonObject["response"]!!)
}

suspend fun MDS.getScripts(): List<Address> {
  val scripts = cmd("scripts")!!
  return json.decodeFromJsonElement(scripts.jsonObject["response"]!!)
}

suspend fun MDS.getScript(address: String): Address {
  val scripts = cmd("scripts address:$address")!!
  return json.decodeFromJsonElement(scripts.jsonObject["response"]!!)
}

suspend fun MDS.newScript(text: String, trackAll: Boolean = true): Address {
  val newscript = cmd("""newscript script:"$text" trackall:$trackAll""")!!
  return json.decodeFromJsonElement(newscript.jsonObject["response"]!!)
}

suspend fun MDS.getAddress(): Address {
  val getaddress = cmd("getaddress")!!
  return json.decodeFromJsonElement(getaddress.jsonObject["response"]!!)
}

suspend fun MDS.newAddress(): Address {
  val newaddress = cmd("newaddress")!!
  return json.decodeFromJsonElement(newaddress.jsonObject["response"]!!)
}

suspend fun MDS.newKey(): String {
  val keys = cmd("keys action:new")!!
  return keys.jsonObject["response"]!!.jsonString("publickey")
}

suspend fun MDS.getCoins(tokenId: String? = null, address: String? = null, coinId: String? = null, sendable: Boolean = false, relevant: Boolean = true): List<Coin> {
  val coinSimple = cmd(buildString {
    append("coins")
    tokenId?.let { append(" tokenid:$tokenId") }
    address?.let { append(" address:$address ") }
    coinId?.let { append(" coinid:$it") }
    append(" sendable:$sendable")
    append(" relevant:$relevant")
  })!!
  val coins = json.decodeFromJsonElement<List<Coin>>(coinSimple.jsonObject["response"]!!)
  return coins.sortedBy { it.amount }
}

suspend fun MDS.createToken(name: String, supply: BigDecimal, decimals: Int, url: String? = null) =
  createToken(supply, decimals, name, url)

suspend fun MDS.createToken(supply: BigDecimal, decimals: Int, name: String, url: String? = null, script: String? = null): Result {
  val nameJson = JsonObject(listOfNotNull("name" to JsonPrimitive(name), url?.let{ "url" to JsonPrimitive(it) }).toMap())
  return createToken(supply, decimals, nameJson, script)
}

suspend fun MDS.createToken(supply: BigDecimal, decimals: Int, name: JsonElement, script: String? = null): Result {
  val tokencreate = buildString {
    append("tokencreate name:$name")
    append(" amount:${supply.toPlainString()}")
    if (decimals > 0) append(".$decimals")
    if (script != null) append(" script:\"$script\"")
  }
  val result = cmd(tokencreate)!!
  return Result(result.jsonBoolean("status"), result.jsonStringOrNull("message"))
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
  return result.jsonObject["response"]!!.jsonString("data")
}

suspend fun MDS.importTx(txnId: Int, data: String): Transaction {
  val txncreator = buildString{
    appendLine("txncreate id:$txnId;")
    append("txnimport id:$txnId data:$data;")
  }
  val txnimport = cmd(txncreator)!!.jsonArray[1]
  if (logging) log("import ${txnimport.jsonBoolean("status")}")
  return json.decodeFromJsonElement(txnimport.jsonObject["response"]!!.jsonObject["transaction"]!!)
}

suspend fun MDS.exportCoin(coinId: String): String {
  val coinexport = cmd("coinexport coinid:$coinId")!!
  return coinexport.jsonString("response")
}

suspend fun MDSInterface.importCoin(data: String): Coin {
  val coinimport = cmd("coinimport data:$data")!!
  if (coinimport.jsonBoolean("status") == true) {
    return json.decodeFromJsonElement(coinimport.jsonObject["response"]!!.jsonObject["coin"]!!)
  } else throw MinimaException(coinimport.jsonStringOrNull("error"))
}

suspend fun MDS.getTxPoWs(address: String): JsonArray? {
  val txnpow = cmd("txpow address:$address")!!
  return txnpow.jsonObject["response"]?.jsonArray
}

suspend fun MDS.getTxPoW(txPoWId: String): JsonElement? {
  val txnpow = cmd("txpow txpowid:$txPoWId")!!
  return txnpow.jsonObject["response"]
}

suspend fun MDS.getTransactions(address: String): List<Transaction>? = getTxPoWs(address)?.map{
  json.decodeFromJsonElement(it.jsonObject["body"]!!.jsonObject["txn"]!!)
}

suspend fun MDS.getTransaction(txPoWId: String): Transaction? = getTxPoW(txPoWId)?.let{
  json.decodeFromJsonElement(it.jsonObject["body"]!!.jsonObject["txn"]!!)
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

suspend fun MDS.hash(data: ByteArray, type: String = "keccak") = hash(data.toHex(), type)

suspend fun MDS.hash(data: String, type: String = "keccak") =
  cmd("hash data:$data type:$type")!!.jsonObject["response"]!!.jsonString("hash")
