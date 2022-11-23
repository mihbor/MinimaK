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
//  val total: String,
  val confirmed: BigDecimal,
  val unconfirmed: BigDecimal,
  val sendable: BigDecimal,
  @JsonNames("coins")
  val _coins: String
) {
  val tokenName get() = if (_token is JsonPrimitive) _token.jsonPrimitive.content else _token.jsonString("name")
  val tokenUrl get() = _token.jsonString("url")
  val coins get() = _coins.toInt()
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
  @JsonNames("tokenamount")
  val totalAmount: BigDecimal? = null,
  @JsonNames("scale")
  val _scale: JsonPrimitive
) {
  val name get() = if(_name is JsonPrimitive) _name.jsonPrimitive.content else _name.jsonString("name")
  val url get() = _name.jsonString("url")
  val scale get() = if (_scale.isString) _scale.content.toInt() else _scale.int
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
  val created: String,
  val state: List<State>
)

@Serializable
data class State(
  val port: Int,
  val type: Int,
  val data: String
)

data class Output(
  val address: String,
  val amount: BigDecimal,
  val tokenId: String
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
  val lastSeen: Int,
  val date: String,
  @JsonNames("chaintip")
  val chainTip: String,
  @JsonNames("samechain")
  val sameChain: Boolean,
  @JsonNames("extradata")
  val extraData: ExtraData
)

@Serializable
data class ExtraData(
  val name: String,
  @JsonNames("minimaaddress")
  val minimaAddress: String,
  @JsonNames("topblock")
  val topBlock: String,
  @JsonNames("checkblock")
  val checkBlock: String,
  @JsonNames("checkhash")
  val checkHash: String,
  val mls: String
)