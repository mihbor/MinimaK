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

suspend fun MdsApi.getBlockNumber(): Int {
  val status = cmd("status")!!
  return status.jsonObject["response"]!!.jsonObject["chain"]!!.jsonObject["block"]!!.jsonPrimitive.int
}

suspend fun MdsApi.getBalance(address: String? = null, tokenId: String, confirmations: Int? = null): Balance? {
  val balance = cmd("balance ${address?.let{ "address:$address " } ?:""}tokenid:$tokenId ${confirmations?.let{ "confirmations:$confirmations " } ?:""}")!!
  return json.decodeFromJsonElement<List<Balance>>(balance.jsonObject["response"]!!).firstOrNull()
}

suspend fun MdsApi.getBalances(address: String? = null, tokenId: String? = null, confirmations: Int? = null): List<Balance> {
  val balance = cmd("balance ${address?.let{ "address:$address " } ?:""}${tokenId?.let{ "tokenid:$tokenId " } ?:""}${confirmations?.let{ "confirmations:$confirmations " } ?:""}")!!
  return json.decodeFromJsonElement(balance.jsonObject["response"]!!)
}

suspend fun MdsApi.getTokens(): List<Token> {
  val tokens = cmd("tokens")!!
  return json.decodeFromJsonElement(tokens.jsonObject["response"]!!)
}

suspend fun MdsApi.getScripts(): List<Address> {
  val scripts = cmd("scripts")!!
  return json.decodeFromJsonElement(scripts.jsonObject["response"]!!)
}

suspend fun MdsApi.getScript(address: String): Address {
  val scripts = cmd("scripts address:$address")!!
  return json.decodeFromJsonElement(scripts.jsonObject["response"]!!)
}

suspend fun MdsApi.newScript(text: String, trackAll: Boolean = true): Address {
  val newscript = cmd("""newscript script:"$text" trackall:$trackAll""")!!
  return json.decodeFromJsonElement(newscript.jsonObject["response"]!!)
}

suspend fun MdsApi.getAddress(): Address {
  val getaddress = cmd("getaddress")!!
  return json.decodeFromJsonElement(getaddress.jsonObject["response"]!!)
}

suspend fun MdsApi.newAddress(): Address {
  val newaddress = cmd("newaddress")!!
  return json.decodeFromJsonElement(newaddress.jsonObject["response"]!!)
}

suspend fun MdsApi.checkAddress(address: String): Boolean {
  val checkaddress = cmd("checkaddress address:$address")!!
  return checkaddress.jsonObject["response"]?.jsonBooleanOrNull("relevant") ?: false
}

suspend fun MdsApi.newKey(): Key {
  val keys = cmd("keys action:new")!!
  return json.decodeFromJsonElement(keys.jsonObject["response"]!!)
}

suspend fun MdsApi.getKeys(): List<Key> {
  val keys = cmd("keys")!!
  return json.decodeFromJsonElement<List<Key>>(keys.jsonObject["response"]!!.jsonObject["keys"]!!)
}

suspend fun MdsApi.getKey(key: String): Key? {
  val keys = cmd("keys publickey:$key")!!
  return json.decodeFromJsonElement<List<Key>>(keys.jsonObject["response"]!!.jsonObject["keys"]!!).firstOrNull()
}

suspend fun MdsApi.getCoins(tokenId: String? = null, address: String? = null, coinId: String? = null, sendable: Boolean = false, relevant: Boolean = true): List<Coin> {
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

suspend fun MdsApi.createToken(name: String, supply: BigDecimal, decimals: Int, url: String? = null) =
  createToken(supply, decimals, name, url)

suspend fun MdsApi.createToken(supply: BigDecimal, decimals: Int, name: String, url: String? = null, script: String? = null): Result {
  val nameJson = JsonObject(listOfNotNull("name" to JsonPrimitive(name), url?.let{ "url" to JsonPrimitive(it) }).toMap())
  return createToken(supply, decimals, nameJson, script)
}

suspend fun MdsApi.createToken(supply: BigDecimal, decimals: Int, name: JsonElement, script: String? = null): Result {
  val tokencreate = buildString {
    append("tokencreate name:$name")
    append(" amount:${supply.toPlainString()}")
    if (decimals > 0) append(".$decimals")
    if (script != null) append(" script:\"$script\"")
  }
  val result = cmd(tokencreate)!!
  return Result(result.jsonBoolean("status"), result.jsonStringOrNull("message"))
}

suspend fun MdsApi.signTx(txnId: Int, key: String): JsonElement? {
  val txnsign = "txnsign id:$txnId publickey:$key;"
  val result = cmd(txnsign)
  if (logging) log("txnsign ${result?.jsonBoolean("status")}")
  return result
}

suspend fun MdsApi.signData(data: String, key: String): String? {
  val hex = "0x" + data.encodeToByteArray().toHex()
  val sign = "sign data:$hex publickey:$key;"
  val result = cmd(sign)!!
  if (logging) log("sign ${result.jsonBoolean("status")}")
  return result.jsonStringOrNull("response")
}

suspend fun MdsApi.verify(data: String, key: String, signature: String): Boolean {
  val hex = "0x" + data.encodeToByteArray().toHex()
  val verify = "verify data:$hex publickey:$key signature:$signature;"
  val result = cmd(verify)!!
  val status = result.jsonBooleanOrNull("status")
  if (logging) log("verify ${result.jsonBoolean("status")}")
  if (status != true) {
    log("verify: ${result.jsonStringOrNull("message")}")
  }
  return status ?: false
}

suspend fun MdsApi.post(txnId: Int): JsonElement? {
  val txncreator = "txnpost id:$txnId auto:true;"
  val result = cmd(txncreator)!!
  return result.jsonObject["response"]
}

suspend fun MdsApi.exportTx(txnId: Int): String {
  val txncreator = "txnexport id:$txnId;"
  val result = cmd(txncreator)!!
  return result.jsonObject["response"]!!.jsonString("data")
}

suspend fun MdsApi.importTx(txnId: Int, data: String): Transaction {
  val txncreator = buildString{
    appendLine("txncreate id:$txnId;")
    append("txnimport id:$txnId data:$data;")
  }
  val txnimport = cmd(txncreator)!!.jsonArray[1]
  if (logging) log("import ${txnimport.jsonBoolean("status")}")
  return json.decodeFromJsonElement(txnimport.jsonObject["response"]!!.jsonObject["transaction"]!!)
}

suspend fun MdsApi.exportCoin(coinId: String): String {
  val coinexport = cmd("coinexport coinid:$coinId")!!
  return coinexport.jsonString("response")
}

suspend fun MdsApi.importCoin(data: String): Coin {
  val coinimport = cmd("coinimport data:$data")!!
  if (coinimport.jsonBoolean("status") == true) {
    return json.decodeFromJsonElement(coinimport.jsonObject["response"]!!.jsonObject["coin"]!!)
  } else throw MinimaException(coinimport.jsonStringOrNull("error"))
}

suspend fun MdsApi.getTxPoWs(address: String): JsonArray? {
  val txnpow = cmd("txpow address:$address")!!
  return txnpow.jsonObject["response"]?.jsonArray
}

suspend fun MdsApi.getTxPoW(txPoWId: String): JsonElement? {
  val txnpow = cmd("txpow txpowid:$txPoWId")!!
  return txnpow.jsonObject["response"]
}

suspend fun MdsApi.getTransactions(address: String): List<Transaction>? = getTxPoWs(address)?.map{
  it.toTransaction()
}

suspend fun MdsApi.getTransaction(txPoWId: String): Transaction? = getTxPoW(txPoWId)?.toTransaction()

fun JsonElement.toTransaction(): Transaction = json.decodeFromJsonElement<Transaction>(jsonObject["body"]!!.jsonObject["txn"]!!)
  .copy(header = json.decodeFromJsonElement(jsonObject["header"]!!))

suspend fun MdsApi.getContacts(): List<Contact> {
  val maxcontacts = cmd("maxcontacts")!!
  return json.decodeFromJsonElement(maxcontacts.jsonObject["response"]!!.jsonObject["contacts"]!!.jsonArray)
}

suspend fun MdsApi.addContact(maxiAddress: String): Contact? {
  val maxcontacts = cmd("maxcontacts action:add contact:$maxiAddress")!!
  return if (maxcontacts.jsonBoolean("status") == true)
    getContacts().find{ it.currentAddress == maxiAddress }
  else null
}

suspend fun MdsApi.getMaximaInfo(): MaximaInfo {
  val maxima = cmd("maxima")!!
  return json.decodeFromJsonElement(maxima.jsonObject["response"]!!)
}

suspend fun MdsApi.setMaximaName(name: String): String {
  val maxima = cmd("maxima action:setname name:$name")!!
  return maxima.jsonObject["response"]!!.jsonString("name")
}

suspend fun MdsApi.sendMessage(app: String, publicKey: String, text: String): Boolean {
  val hex = "0x" + text.encodeToByteArray().toHex()
  val maxima = cmd("maxima action:send application:$app publickey:$publicKey data:$hex")!!
  log("sent ${text.length} chars: $text")
  return maxima.jsonBoolean("status") == true && maxima.jsonObject["response"]!!.jsonBoolean("delivered") == true
}

suspend fun MdsApi.hash(data: ByteArray, type: String = "keccak") = hash(data.toHex(), type)

suspend fun MdsApi.hash(data: String, type: String = "keccak") =
  cmd("hash data:$data type:$type")!!.jsonObject["response"]!!.jsonString("hash")
