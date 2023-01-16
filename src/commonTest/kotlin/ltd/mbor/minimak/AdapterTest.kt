package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import ltd.mbor.minimak.resources.coinimport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

@OptIn(ExperimentalCoroutinesApi::class)
class AdapterTest {
  
  @Test
  fun importCoin_returns_error() = runTest{
    //given:
    val mds = SimulatedMDS.willReturn(coinimport.error)
    //when:
    val result = assertFails{ mds.importCoin("0x123") }
    //then:
    assertEquals(MinimaException("""Cannot invoke "org.minima.database.txpowtree.TxPoWTreeNode.isRelevantEntry(org.minima.database.mmr.MMREntryNumber)" because "node" is null"""), result)
  }

  @Test
  fun importCoin_returns_success() = runTest {
    //given:
    val mds = SimulatedMDS.willReturn(coinimport.success)
    //when:
    val result = mds.importCoin("0x123")
    //then:
    assertEquals(
      Coin(
        coinId="0xF735BCAC6726EB0EC4395BA572F1F9A27F3982BAEC27B738ABD1FD876F1E79D8",
        amount="6.3".toBigDecimal(),
        address="0x2E0F9E9971BEF84D2833082E03575CA72515EB90867E3F50BB510EB00F9D7C4B",
        miniAddress="MxG081E1UF9WSDUV16WGCZ85Z1YEN574KAUN446FZVY1EQH1QZ0V7BS9CPC5FDD",
        tokenId="0x00",
        token=null,
        storeState=true,
        state=emptyList(),
        _created="332312"
      ),
      result
    )
  }
}