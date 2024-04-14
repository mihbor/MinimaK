@file:UseContextualSerialization(BigDecimal::class)

package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class Balance(
  @SerialName("tokenid")
  val tokenId: String,
  @SerialName("token")
  val _token: JsonElement,
  val total: BigDecimal,
  val confirmed: BigDecimal,
  val unconfirmed: BigDecimal,
  val sendable: BigDecimal,
  @SerialName("coins")
  val _coins: String
) {
  val tokenName: String? get() = when(_token) {
    is JsonNull -> if(tokenId == "0x00") "Minima" else null
    is JsonPrimitive -> _token.jsonPrimitive.content
    else -> _token.jsonStringOrNull("name")
  }
  val tokenUrl: String? get() = _token.jsonStringOrNull("url")
  val coins: Int get() = _coins.toInt()
}

@Serializable
data class Token(
  @SerialName("tokenid")
  val tokenId: String,
  @SerialName("name")
  val _name: JsonElement,
  val total: BigDecimal,
  val decimals: Int,
  val script: String? = null,
  @SerialName("coinid")
  val coinId: String? = null,
  @SerialName("totalamount")
  val totalAmount: BigDecimal? = null,
  @SerialName("scale")
  val _scale: JsonPrimitive
) {
  val name: String? get() = when(_name) {
    is JsonNull -> if(tokenId == "0x00") "Minima" else null
    is JsonPrimitive -> _name.jsonPrimitive.content
    else -> _name.jsonStringOrNull("name") }
  val url: String? get() = _name.jsonStringOrNull("url")
  val scale: Int get() = if (_scale.isString) _scale.content.toInt() else _scale.int
}

@Serializable
data class Transaction(
  val inputs: List<Coin>,
  val outputs: List<Coin>,
  val state: List<State>,
  @SerialName("transactionid")
  val transactionId: String,
  val header: Header? = null
) {
  @Serializable
  data class Header(
    @SerialName("block")
    val _block: String,
    @SerialName("timemilli")
    val _timemilli: String
  ) {
    val block get() = _block.toInt()
    val timeMillis get() = _timemilli.toLong()
  }
}

@Serializable
data class Block(
  @SerialName("txpowid")
  val id: String,
  @SerialName("superblock")
  val superBlock: Int,
  val size: Int,
  @Contextual
  val burn: BigDecimal,
  val header: Header,
  val body: Body
) {
  @Serializable
  data class Header(
    @SerialName("chainid")
    val chainId: String,
    @SerialName("block")
    val _block: String,
    @SerialName("cascadelevels")
    val cascadeLevels: Int,
    @SerialName("superparents")
    val superParents: List<Parent>,
    @SerialName("timemilli")
    val _timemilli: String,
    val date: String
  ) {
    val block get() = _block.toInt()
    val timeMillis get() = _timemilli.toLong()

    @Serializable
    data class Parent(
      val difficulty: Int,
      val count: Int,
      val parent: String
    )
  }
  @Serializable
  data class Body(
    @SerialName("txnlist")
    val txnList: List<String>
  )
}

@Serializable
data class Coin(
  val address: String,
  @SerialName("miniaddress")
  val miniAddress: String,
  val amount: BigDecimal,
  @SerialName("tokenamount")
  val tokenAmount: BigDecimal = amount,
  @SerialName("coinid")
  val coinId: String,
  @SerialName("storestate")
  val storeState: Boolean,
  @SerialName("tokenid")
  val tokenId: String,
  val token: Token?,
  @SerialName("created")
  val _created: String,
  val state: List<State>
) {
  val created get() = _created.toInt()
}

@Serializable
data class State(
  val port: Int,
  val type: Int,
  val data: String
)

data class Output(
  val address: String,
  val amount: BigDecimal,
  val tokenId: String,
  val storeState: Boolean? = null
)

@Serializable
data class Contact(
  val id: Int,
  @SerialName("publickey")
  val publicKey: String,
  @SerialName("currentaddress")
  val currentAddress: String,
  @SerialName("myaddress")
  val myAddress: String,
  @SerialName("lastseen")
  val lastSeen: Long,
  val date: String,
  @SerialName("chaintip")
  val _chainTip: String,
  @SerialName("samechain")
  val sameChain: Boolean,
  @SerialName("extradata")
  val extraData: ExtraData
) {
  val chainTip = _chainTip.toInt()
}

@Serializable
data class ExtraData(
  val name: String,
  @SerialName("minimaaddress")
  val minimaAddress: String,
  @SerialName("topblock")
  val _topBlock: String,
  @SerialName("checkblock")
  val _checkBlock: String,
  @SerialName("checkhash")
  val checkHash: String,
  val mls: String
) {
  val topBlock = _topBlock.toInt()
  val checkBlock = _checkBlock.toInt()
}

@Serializable
data class MaximaInfo(
  val contact: String,
  @SerialName("localidentity")
  val localIdentity: String,
  val logs: Boolean,
  val mls: String,
  val name: String,
  @SerialName("p2pidentity")
  val p2pIdentity: String,
  val poll: Int,
  @SerialName("publickey")
  val publicKey: String,
  @SerialName("staticmls")
  val staticMls: Boolean
)

@Serializable
data class MaximaMessage(
  val from: String,
  val to: String,
  val time: String,
  @SerialName("timemilli")
  val timeMilli: Long,
  val random: String,
  val application: String,
  val data: String,
  @SerialName("msgid")
  val msgId: String
)

@Serializable
data class Address(
  val script: String,
  val address: String,
  @SerialName("miniaddress")
  val miniAddress: String,
  val simple: Boolean,
  val default: Boolean,
  @SerialName("publickey")
  val publicKey: String,
  val track: Boolean
)

@Serializable
data class Key(
  @SerialName("publickey")
  val publicKey: String,
  val modifier: String,
  val size: Int,
  val depth: Int,
  val uses: Int,
  @SerialName("maxuses")
  val maxUses: Int
)

@Serializable
data class Status(
  val version: String,
  val locked: Boolean,
  val length: Long,
  val weight: BigDecimal,
  val chain: Chain,
) {
  @Serializable
  data class Chain(
    val block: Long,
    val time: String,
    val hash: String,
    val speed: BigDecimal,
    val difficulty: String,
    val size: Int,
    val length: Int,
    val branches: Int,
    val weight: BigDecimal,
    val cascade: Cascade,
  ) {
    @Serializable
    data class Cascade(
      val start: Long,
      val length: Int,
      val weight: BigDecimal
    )
  }
}

@Serializable
data class BurnStats(
  val txns: Int,
  val max: BigDecimal,
  val med: BigDecimal,
  val avg: BigDecimal,
  val min: BigDecimal
)
