package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ltd.mbor.minimak.resources.balance
import ltd.mbor.minimak.resources.coinimport
import ltd.mbor.minimak.resources.txnimport
import ltd.mbor.minimak.resources.txpow
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class AdapterTest {

  @Test
  fun zero_balance() = runTest {
    //given
    val mds = SimulatedMDS.willReturn(balance.zero)
    //when
    val result = mds.getBalances()
    //then
    assertEquals(
      listOf(Balance(
        tokenId = "0x00",
        _token = JsonPrimitive("Minima"),
        total = "1000000000".toBigDecimal(),
        confirmed = ZERO,
        unconfirmed = ZERO,
        sendable = ZERO,
        _coins = "0"
      )),
      result
    )
    assertEquals("Minima", result.first().tokenName)
    assertNull(result.first().tokenUrl)
    assertEquals(0, result.first().coins)
  }

  @Test
  fun importCoin_returns_error() = runTest{
    //given
    val mds = SimulatedMDS.willReturn(coinimport.error)
    //when
    val result = assertFails{ mds.importCoin("0x123") }
    //then
    assertEquals(MinimaException("""Cannot invoke "org.minima.database.txpowtree.TxPoWTreeNode.isRelevantEntry(org.minima.database.mmr.MMREntryNumber)" because "node" is null"""), result)
  }

  @Test
  fun importCoin_returns_success() = runTest {
    //given
    val mds = SimulatedMDS.willReturn(coinimport.success)
    //when
    val result = mds.importCoin("0x123")
    //then
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

  @Test
  fun txpow_with_address() = runTest {
    //given
    val mds = SimulatedMDS.willReturn(txpow.byAddress)
    //when
    val result = mds.getTransactions("some address")
    //then
    assertNotNull(result)
    assertEquals(2, result.size)
    
    val first = result[0]
    assertEquals("0xD4D1C007D23AC7D5508DA0FE63C79BB43D01D5905A7E7B1708BA02D515080F36", first.transactionId)
    assertEquals(2, first.inputs.size)
    assertEquals(3, first.outputs.size)
    assertNotNull(first.header)
    assertEquals(349299, first.header!!.block)
    
    val second = result[1]
    assertEquals("0xDD526F0D0F59D5E92B98A3925A0429E68A02D01D82926BDF41E3A4D2754BAF1F", second.transactionId)
    assertEquals(1, second.inputs.size)
    assertEquals(2, second.outputs.size)
    assertNotNull(second.header)
    assertEquals(349298, second.header!!.block)
  }

  @Test
  fun create_and_import_signed_tx() = runTest {
    //given
    val mds = SimulatedMDS.willReturn(txnimport.createAndImportSignedTx)
    //when
    val result = mds.importTx(399674040, txnimport.signedTxData)
    //then
    assertNotNull(result)
    assertEquals("0xBDD499ED70C508791A2BCFA6D477A4D570A81DC27B9AF2214B9356E8814C5112", result.transactionId)
    assertEquals(1, result.inputs.size)
    assertEquals(
      Coin(
        address = "0xB13D03D7FAC25552491F8E1C04120FEA67700DFB5AB576CA7DDED59D35F93A96",
        miniAddress = "MxG085H7K1TFUM2AY94W7SE3G2143VACTZ0RUQQMYRCKVEUQMEJBU9QWPPERECZ",
        amount = "40.1".toBigDecimal(),
        tokenAmount = "40.1".toBigDecimal(),
        coinId = "0x01",
        storeState = true,
        tokenId = "0x8EA80F001433D4CB4EB466A7C4C6D3E99D0DC244F73AE2691B2F5AC8438004DB",
        token = null,
        _created = "0",
        state = emptyList()
      ),
      result.inputs.first()
    )
    assertEquals(1, result.outputs.size)
    assertEquals(
      Coin(
        address = "0xB13D03D7FAC25552491F8E1C04120FEA67700DFB5AB576CA7DDED59D35F93A96",
        miniAddress = "MxG085H7K1TFUM2AY94W7SE3G2143VACTZ0RUQQMYRCKVEUQMEJBU9QWPPERECZ",
        amount = "0.0000000000000000000000000000000000401".toBigDecimal(),
        tokenAmount = "40.1".toBigDecimal(),
        coinId = "0x00",
        storeState = true,
        tokenId = "0x8EA80F001433D4CB4EB466A7C4C6D3E99D0DC244F73AE2691B2F5AC8438004DB",
        token = Token(
          tokenId = "0x8EA80F001433D4CB4EB466A7C4C6D3E99D0DC244F73AE2691B2F5AC8438004DB",
          _name = JsonObject(mapOf(
            "name" to JsonPrimitive("PigCoin"),
            "url" to JsonPrimitive("https://uxwing.com/wp-content/themes/uxwing/download/food-and-drinks/pork-icon.png")
          )),
          total = "1000000000".toBigDecimal(),
          decimals = 8,
          script = "RETURN TRUE",
          coinId = "0x07DE61C15BFBD6104C90974EDEB9047C61366CC712D0E890D3DDD101A620CFCC",
          totalAmount = "0.000000000000000000000000001".toBigDecimal(),
          _scale = JsonPrimitive("36")
        ),
        _created = "0",
        state = emptyList()
      ),
      result.outputs.first()
    )
  }
}