@file:UseContextualSerialization(BigDecimal::class)
@file:OptIn(ExperimentalSerializationApi::class)

package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.json.*

@Serializable
data class Balance(
  @JsonNames("tokenid")
  val tokenId: String,
  @JsonNames("token")
  val _token: JsonElement,
  val total: BigDecimal,
  val confirmed: BigDecimal,
  val unconfirmed: BigDecimal,
  val sendable: BigDecimal,
  @JsonNames("coins")
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
  @JsonNames("tokenid")
  val tokenId: String,
  @JsonNames("name")
  val _name: JsonElement,
  val total: BigDecimal,
  val decimals: Int,
  val script: String? = null,
  @JsonNames("coinid")
  val coinId: String? = null,
  @JsonNames("totalamount")
  val totalAmount: BigDecimal? = null,
  @JsonNames("scale")
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
  @JsonNames("transactionid")
  val transactionId: String,
  val header: Header? = null
) {
  @Serializable
  data class Header(
    @JsonNames("block")
    val _block: String,
    @JsonNames("timemilli")
    val _timemilli: String
  ) {
    val block get() = _block.toInt()
    val timeMillis get() = _timemilli.toLong()
  }
}

@Serializable
data class Coin(
  val address: String,
  @JsonNames("miniaddress")
  val miniAddress: String,
  val amount: BigDecimal,
  @JsonNames("tokenamount")
  val tokenAmount: BigDecimal = amount,
  @JsonNames("coinid")
  val coinId: String,
  @JsonNames("storestate")
  val storeState: Boolean,
  @JsonNames("tokenid")
  val tokenId: String,
  val token: Token?,
  @JsonNames("created")
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
  @JsonNames("publickey")
  val publicKey: String,
  @JsonNames("currentaddress")
  val currentAddress: String,
  @JsonNames("myaddress")
  val myAddress: String,
  @JsonNames("lastseen")
  val lastSeen: Long,
  val date: String,
  @JsonNames("chaintip")
  val _chainTip: String,
  @JsonNames("samechain")
  val sameChain: Boolean,
  @JsonNames("extradata")
  val extraData: ExtraData
) {
  val chainTip = _chainTip.toInt()
}

@Serializable
data class ExtraData(
  val name: String,
  @JsonNames("minimaaddress")
  val minimaAddress: String,
  @JsonNames("topblock")
  val _topBlock: String,
  @JsonNames("checkblock")
  val _checkBlock: String,
  @JsonNames("checkhash")
  val checkHash: String,
  val mls: String
) {
  val topBlock = _topBlock.toInt()
  val checkBlock = _checkBlock.toInt()
}

@Serializable
data class MaximaInfo(
  val contact: String,
  @JsonNames("localidentity")
  val localIdentity: String,
  val logs: Boolean,
  val mls: String,
  val name: String,
  @JsonNames("p2pidentity")
  val p2pIdentity: String,
  val poll: Int,
  @JsonNames("publickey")
  val publicKey: String,
  @JsonNames("staticmls")
  val staticMls: Boolean
)

@Serializable
data class MaximaMessage(
  val from: String,
  val to: String,
  val time: String,
  @JsonNames("timemilli")
  val timeMilli: Long,
  val random: String,
  val application: String,
  val data: String,
  @JsonNames("msgid")
  val msgId: String
)

@Serializable
data class Address(
  val script: String,
  val address: String,
  @JsonNames("miniaddress")
  val miniAddress: String,
  val simple: Boolean,
  val default: Boolean,
  @JsonNames("publickey")
  val publicKey: String,
  val track: Boolean
)

@Serializable
data class Key(
  @JsonNames("publickey")
  val publicKey: String,
  val modifier: String,
  val size: Int,
  val depth: Int,
  val uses: Int,
  @JsonNames("maxuses")
  val maxUses: Int
)
