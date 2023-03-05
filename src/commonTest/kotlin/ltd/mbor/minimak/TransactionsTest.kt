package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.TEN
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import ltd.mbor.minimak.resources.transact
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsTest {
  @Test
  fun Given_1_coin_ofAtLeast_returns_it() {
    for (given in listOf(coin(ONE), coin(TEN))) {
      val result = listOf(given).ofAtLeast(ONE)
      assertEquals(listOf(given), result)
    }
  }

  @Test
  fun Given_2_coins_ofAtLeast_returns_smallest_sufficient() {
    val given = listOf(coin(ONE), coin(TEN))
    assertEquals(listOf(coin(ONE)), given.ofAtLeast(ONE))
    assertEquals(listOf(coin(TEN)), given.ofAtLeast(ONE + ONE))
  }

  @Test
  fun Given_2_coins_ofAtLeast_returns_both_when_needed() {
    val given = listOf(coin(ONE), coin(TEN))
    assertEquals(setOf(coin(ONE), coin(TEN)), given.ofAtLeast(TEN + ONE).toSet())
  }

  @Test
  fun sendSuccessful() = runTest {
    //given
    val mds = SimulatedMDS().willReturn(transact.coins).willReturn(transact.getaddress).willReturn(transact.response)
    val toAddress = "0xA0A5C71600ED2FA870296501417A1CB8855FE1D89F3769267FA97B2F1D52208C"
    val amount = 1000.toBigDecimal()
    //when
    val result = mds.send(toAddress, amount, "0x00")
    //then
    assertEquals(
      Result(true, null),
      result
    )
// TODO: stub out random transaction id and handle url encoding
//    assertEquals(
//      transact.response,
//      mds.capturedCommands.last()
//    )
  }
}

private fun coin(tokenAmount: BigDecimal) = Coin(
  address = "",
  miniAddress = "",
  amount = tokenAmount,
  coinId = "",
  storeState = false,
  tokenId = "",
  _created = "",
  state = emptyList(),
  token = null
)