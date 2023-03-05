package ltd.mbor.minimak

import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.ZERO
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ltd.mbor.minimak.resources.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class AdapterTest {

  @Test
  fun zero_balance() = runTest {
    //given
    val mds = SimulatedMDS().willReturn(balance.zero)
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
    val mds = SimulatedMDS().willReturn(coinimport.error)
    //when
    val result = assertFails{ mds.importCoin("0x123") }
    //then
    assertEquals(MinimaException("""Cannot invoke "org.minima.database.txpowtree.TxPoWTreeNode.isRelevantEntry(org.minima.database.mmr.MMREntryNumber)" because "node" is null"""), result)
  }

  @Test
  fun importCoin_returns_success() = runTest {
    //given
    val mds = SimulatedMDS().willReturn(coinimport.success)
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
  fun exportCoin() = runTest {
    //given
    val mds = SimulatedMDS().willReturn(coinexport.coinexport)
    //when
    val result = mds.exportCoin("0xCFFDBD5B01ECEE15EDD71196AEA8102BBFDDE64F03F47C06EBDF2EDC1764D87B")
    //then
    assertEquals("0x00000020CFFDBD5B01ECEE15EDD71196AEA8102BBFDDE64F03F47C06EBDF2EDC1764D87B00000020F58C2C4326611D5B0181F364579E4826392059A8279F04160C635F5ACB19EC5F00010200000001000100010000000003034E1C000003057C920001016302000104000003060C3B0001070000000020A0304A7FABBFA3FFAD57C48F46838D884DCF06D84B975575AE79838260FE715E0101020000000020C13C741AB06400E2E5D822968C75CCBF680B0077E6CCB6EFE48134270309862F00010501000000209E081034C7ED070BED6C85179CC816F969024F91DD2E625BF1D9DA92602447A3010103010000002048F608D60969B005114C70A4AC4C30DADB8B241E7A4A8D2F2D5F0E33B3944D702C1331536176FA988FC6768CB00C3FB016106B83300100000020CF576E5117A56FE2364BC23E9674F878D0B527FE5F75EF267208DBCF04D1A9A92C140365ED1403CF3CD066595420D806200A802B62FF0000000020CAA7C1ED0C36CDF918020DB656461F8A380B2F0A8D19124FECEC96973AB3B4A02C1403B1B553A5A8AE2F9E3E026284719FE4BAE91EFF0000000020594CED4A037D926D90C010C876F7A17D288155A837A783C960BEDB69ACB1B3282C1403223720D72CC3D32E59806260CFA2DE1C0FA5CD", result)
  }

  @Test
  fun txpow_with_address() = runTest {
    //given
    val mds = SimulatedMDS().willReturn(txpow.byAddress)
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
    val mds = SimulatedMDS().willReturn(txnimport.createAndImportSignedTx)
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

  @Test
  fun getAddress() = runTest {
    //given
    val mds = SimulatedMDS().willReturn(address.getAddress)
    //when
    val result = mds.getAddress()
    //then
    assertEquals(
      Address(
        script = "RETURN SIGNEDBY(0xADF476577F854345C4D9225948FA0A26CCA5F6DD40556A4EBFC5A8958EE7BF5C)",
        address = "0xB4A680430A9808AFA98D9F7E3398750AA71DD88E1A815D87C4FCC2A48C0A57D8",
        miniAddress = "MxG085KKQ0462KZ12NQJ3CVFZPPGT8AKSETH3GQG5EZFH7SZAW8Z2WNR2HBV922",
        simple = true,
        default = true,
        publicKey = "0xADF476577F854345C4D9225948FA0A26CCA5F6DD40556A4EBFC5A8958EE7BF5C",
        track = true
      ),
      result
    )
  }

  @Test
  fun get_109_scripts() = runTest {
    //given
    val mds = SimulatedMDS().willReturn(scripts.scripts109)
    //when
    val result = mds.getScripts()
    //then
    assertEquals(109, result.size)
    assertEquals(
      Address(
        script = "RETURN SIGNEDBY(0xB3C8B10782B90C1820111CBE33E207BDF2B7D4C4FF004E5EF38F301847EB2ED8)",
        address = "0x3DD886A5FF072F44A576829B32019DB4DB98BF8E37FD819CA59798A3C2FDDBE9",
        miniAddress = "MxG081TR23ABVZ75T2AATK2JCP037DKRECBV3HNVM0PP9CNJ2HS5VERT4GUC94G",
        simple = true,
        default = true,
        publicKey = "0xB3C8B10782B90C1820111CBE33E207BDF2B7D4C4FF004E5EF38F301847EB2ED8",
        track = true
      ),
      result.first()
    )
  }
}