package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.json.jsonArray
import kotlin.random.Random

suspend fun MDS.transact(inputCoinIds: List<String>, outputs: List<Output>): Result {
  val txId = Random.nextInt(1000000000)
  
  val commands = """
    txncreate id:$txId;
    ${inputCoinIds.joinToString("\n") {
    "txninput id:$txId coinid:$it;"
  }}
    ${outputs.joinToString("\n") {
    "txnoutput id:$txId amount:${it.amount.toPlainString()} address:${it.address} tokenid:${it.tokenId};"
  }}
    txnsign id:$txId publickey:auto;
    txnpost id:$txId auto:true;
    txndelete id:$txId;
  """.trimIndent()
  
  val results = cmd(commands)!!.jsonArray
  val postResult = results.find{ it.jsonString("command") == "txnpost" }
  return Result(postResult?.jsonBoolean("status") == true, postResult?.jsonString("message"))
}


suspend fun MDS.send(toAddress: String, amount: BigDecimal, tokenId: String): Boolean {
  val (inputs, outputs) = inputsWithChange(tokenId, amount)
  return transact(inputs.map{ it.coinId }, listOf(Output(toAddress, amount, tokenId)) + outputs).isSuccessful
}

suspend fun MDS.inputsWithChange(tokenId: String, amount: BigDecimal): Pair<List<Coin>, List<Output>> {
  val inputs = mutableListOf<Coin>()
  val outputs = mutableListOf<Output>()
  val coins = getCoins(tokenId = tokenId, sendable = true).ofAtLeast(amount)
  coins.forEach { inputs.add(it) }
  val change = coins.sumOf { it.tokenAmount } - amount
  if (change > BigDecimal.ZERO) outputs.add(Output(newAddress(), change, tokenId))
  return inputs to outputs
}

fun List<Coin>.ofAtLeast(amount: BigDecimal): List<Coin> {
  return firstOrNull { it.tokenAmount >= amount }
    ?.let{ listOf(it) }
    ?: (listOf(last()) + take(size-1).ofAtLeast(amount - last().tokenAmount))
}

fun <T> Iterable<T>.sumOf(selector: (T) -> BigDecimal) = fold(BigDecimal.ZERO) { acc, item -> acc + selector(item) }
