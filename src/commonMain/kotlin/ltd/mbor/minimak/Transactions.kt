package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.json.jsonArray
import kotlin.random.Random

suspend fun MdsApi.transact(inputCoinIds: List<String>, outputs: List<Output>, states: List<String>) =
  transact(inputCoinIds, outputs, states.mapIndexed{ port, value -> port to value }.toMap())

suspend fun MdsApi.transact(
  inputCoinIds: List<String>,
  outputs: List<Output>,
  states: Map<Int, String> = emptyMap(),
  publicKey: String = "auto"
): Result {
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
    append("txnsign id:$txId publickey:$publicKey txnpostauto:true txndelete:true;")
  }
  
  val results = cmd(commands)!!.jsonArray
  val postResult = results.find{ it.jsonString("command") == "txnsign" }
  return Result(postResult?.jsonBoolean("status") == true, postResult?.jsonStringOrNull("error"))
}

suspend fun MdsApi.send(toAddress: String, amount: BigDecimal, tokenId: String, states: List<String>) =
  send(toAddress, amount, tokenId, states.mapIndexed{ port, value -> port to value }.toMap())

suspend fun MdsApi.send(toAddress: String, amount: BigDecimal, tokenId: String, states: Map<Int, String> = emptyMap()): Result {
  val (inputs, outputs) = inputsWithChange(tokenId, amount)
  return transact(inputs.map{ it.coinId }, listOf(Output(toAddress, amount, tokenId, states.isNotEmpty())) + outputs, states)
}

suspend fun MdsApi.inputsWithChange(tokenId: String, amount: BigDecimal): Pair<List<Coin>, List<Output>> {
  val inputs = mutableListOf<Coin>()
  val outputs = mutableListOf<Output>()
  val coins = getCoins(tokenId = tokenId, sendable = true).ofAtLeast(amount)
  coins.forEach { inputs.add(it) }
  val change = coins.sumOf { it.tokenAmount } - amount
  if (change > BigDecimal.ZERO) outputs.add(Output(getAddress().address, change, tokenId, false))
  return inputs to outputs
}

fun List<Coin>.ofAtLeast(amount: BigDecimal): List<Coin> {
  return firstOrNull { it.tokenAmount >= amount }
    ?.let{ listOf(it) }
    ?: (listOf(last()) + take(size-1).ofAtLeast(amount - last().tokenAmount))
}

fun <T> Iterable<T>.sumOf(selector: (T) -> BigDecimal) = fold(BigDecimal.ZERO) { acc, item -> acc + selector(item) }
