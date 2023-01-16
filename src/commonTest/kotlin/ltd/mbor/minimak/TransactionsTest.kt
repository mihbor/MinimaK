package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ONE
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.TEN
import kotlin.test.Test
import kotlin.test.assertEquals

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
}