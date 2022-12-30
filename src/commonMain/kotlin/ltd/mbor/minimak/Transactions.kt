package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.json.jsonArray
import kotlin.random.Random

suspend fun MDS.transact(inputCoinIds: List<String>, outputs: List<Output>, states: List<String> = emptyList()) =
  MDS.transact(inputCoinIds, outputs, states.mapIndexed{ port, value -> port to value}.toMap())

suspend fun MDS.transact(inputCoinIds: List<String>, outputs: List<Output>, states: Map<Int, String> = emptyMap()): Result {
  val txId = Random.nextInt(1000000000)
  
  val commands = buildString {
    appendLine("txncreate id:$txId;")
    inputCoinIds.forEach {
      appendLine("txninput id:$txId coinid:$it;")
    }
    outputs.forEach {
      append("txnoutput id:$txId amount:${it.amount.toPlainString()} address:${it.address} tokenid:${it.tokenId}")
      it.storeState?.also { append(" storestate:$it") }
      appendLine(";")
    }
    states.forEach{ (port, value) ->
      appendLine("txnstate id:$txId port:$port value:$value;")
    }
    appendLine("txnsign id:$txId publickey:auto;")
    appendLine("txnpost id:$txId auto:true;")
    append("txndelete id:$txId;")
  }
  
  val results = cmd(commands)!!.jsonArray
  val postResult = results.find{ it.jsonString("command") == "txnpost" }
  return Result(postResult?.jsonBoolean("status") == true, postResult?.jsonString("message"))
}

suspend fun MDS.send(toAddress: String, amount: BigDecimal, tokenId: String, states: List<String> = emptyList()) =
  MDS.send(toAddress, amount, tokenId, states.mapIndexed{ port, value -> port to value}.toMap()).isSuccessful

suspend fun MDS.send(toAddress: String, amount: BigDecimal, tokenId: String, states: Map<Int, String> = emptyMap()): Result {
  val (inputs, outputs) = inputsWithChange(tokenId, amount)
  return transact(inputs.map{ it.coinId }, listOf(Output(toAddress, amount, tokenId, states.isNotEmpty())) + outputs, states)
}

suspend fun MDS.inputsWithChange(tokenId: String, amount: BigDecimal): Pair<List<Coin>, List<Output>> {
  val inputs = mutableListOf<Coin>()
  val outputs = mutableListOf<Output>()
  val coins = getCoins(tokenId = tokenId, sendable = true).ofAtLeast(amount)
  coins.forEach { inputs.add(it) }
  val change = coins.sumOf { it.tokenAmount } - amount
  if (change > BigDecimal.ZERO) outputs.add(Output(newAddress(), change, tokenId, false))
  return inputs to outputs
}

fun List<Coin>.ofAtLeast(amount: BigDecimal): List<Coin> {
  return firstOrNull { it.tokenAmount >= amount }
    ?.let{ listOf(it) }
    ?: (listOf(last()) + take(size-1).ofAtLeast(amount - last().tokenAmount))
}

fun <T> Iterable<T>.sumOf(selector: (T) -> BigDecimal) = fold(BigDecimal.ZERO) { acc, item -> acc + selector(item) }
