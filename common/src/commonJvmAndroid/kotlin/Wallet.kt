import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.painter.BitmapPainter
import arrow.core.Either
import io.digitalbits.sdk.*
import io.digitalbits.sdk.requests.AssetsRequestBuilder
import io.digitalbits.sdk.responses.SubmitTransactionResponse
import io.digitalbits.sdk.responses.operations.PaymentOperationResponse
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import me.rahim.common.*
import shadow.com.moandjiezana.toml.Toml

open class AccountNotCreatedException : Exception()
class Wallet(secret: String, val network: Network = Network.TESTNET, initialBalances : Map<Asset,String> = mapOf()) {
    private val keyPair = KeyPair.fromSecretSeed(secret)

    val accountId: String
        get() = keyPair.accountId
    private val server = Server("https://frontier.testnet.digitalbits.io")
    private val httpClient = HttpClient()
    var assetBalances: Map<Asset, String> by mutableStateOf(initialBalances)
    var assets: List<Asset> by mutableStateOf(listOf())
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val account: Account
      get() = Account(accountId,server.accounts().account(keyPair.accountId).sequenceNumber)

    var transactions: List<PaymentOperationResponse> by mutableStateOf(listOf())

    /**
     * Im doing this because some things depend on a composable fun
     */
    init{
        scope.launch(Dispatchers.Default) {
            delay(300)
            reload()
            while (true){
                delay(60000)
                reload()
            }
        }
    }


    /**
     * query frontier for the account state.
     */
    suspend fun reload(){
        kotlin.runCatching {

            val accountAssets = getAssetsForAccount()
            assetBalances = assetBalances.toMutableMap().also {
                it.clear()
            }
            accountAssets.map { (asset, amount) ->
                assetBalances = assetBalances.toMutableMap().also {
                    it[asset] = amount
                }
            }
            Storage.balances = State(assetBalances[Asset.Native]!!,assetBalances.filter{it.key !is Asset.Native} as Map<Asset.Custom, String>)
            assets = assetBalances.map(Map.Entry<Asset, String>::key)
            getTransactions()
        }
    }

    suspend fun getTransactions() {
        val payments = withContext(Dispatchers.IO) {
            server.payments()
                .forAccount(accountId)
                .limit(200)
                .execute()
        }
        transactions = transactions.toMutableList().apply {
            clear()
            for (payment in payments.records.reversed()) {
                when (payment) {
                    is PaymentOperationResponse -> add(payment)
                }
            }
        }
    }


    suspend fun getAssetsForAccount(accountId: String = this.accountId): List<Pair<Asset, String>> =
        withContext(Dispatchers.IO) {
            server
                .accounts()
                .account(accountId)
                .balances.map {
                    if (it.assetType == "native") {
                        Asset.Native to it.balance
                    } else {
                        val (imageUrl, name) = parseToml(it.assetCode, it.assetIssuer)
                        val foundAsset = assets.filter { it is Asset.Custom }.find { asset ->
                            val custom = (asset as Asset.Custom)
                            custom.name == it.assetCode && asset.issuer == custom.issuer
                        } as Asset.Custom?
                        (foundAsset ?: Asset.Custom(
                            code = it.assetCode,
                            issuer= it.assetIssuer,
                            iconLink = imageUrl,
                            name = name
                        ) {
                            if (imageUrl == null) {
                                null
                            } else {
                                runBlocking {
                                    val bytes = httpClient.get<ByteArray>(imageUrl)
                                    BitmapPainter(RemoteImage(bytes))
                                }
                            }
                        }
                                ) to it.balance
                    }
                }
        }

    data class ParseTomlResult(val imageUrl: String?, val name: String?)
    /**
     * Parse Toml and return the image link and name.
     */
    private suspend fun parseToml(
        assetCode: String,
        assetIssuer: String
    ): ParseTomlResult {

        var homedomain = server
            .accounts()
            .account(assetIssuer)
            .homeDomain ?: return ParseTomlResult(null, null)
        homedomain += "/.well-known/digitalbits.toml"
        homedomain = "https://$homedomain"
        val result = runCatching { httpClient.get<String>(homedomain) }.getOrNull()
        result?.let { tomlString ->
            val toml = Toml().read(tomlString)
            val imageUrl = toml.getTables("CURRENCIES").find { it.getString("code") == assetCode }
                ?.getString("image")
            val name = toml.getTables("CURRENCIES").find { it.getString("code") == assetCode  }?.getString("name")

            return ParseTomlResult(imageUrl,name)
        }
        return ParseTomlResult(null,null)

    }


    suspend fun swapAsync(
        from: Asset,
        to: Asset,
        sendAmount: String
    ): Deferred<Either<Exception, SubmitTransactionResponse>> = coroutineScope {
        if (from == to) {
            async { Either.Left(Exception("Assets should not be the same")) }
        } else {
            val transaction = Transaction.Builder(account, network)
                .addOperation(//todo
                    PathPaymentStrictSendOperation.Builder(
                        from.toDigitalBitsAsset(),
                        sendAmount,
                        accountId,
                        to.toDigitalBitsAsset(),
                        "0.01"
                    )
                        .build()
                ).setBaseFee(300)
                .setTimeout(0)
                .build()
            transaction.sign(keyPair)
            async(Dispatchers.IO)
            {
                Either.Right(server.submitTransaction(transaction))
            }
        }
    }

    /**
     * estimate receiving amount
     */
    suspend fun estimateSwapAsync(from: Asset, to : Asset, sendAmount: String): String{
        val estimates = withContext(Dispatchers.IO){
            kotlin.runCatching {
                server.strictSendPaths()
                    .sourceAmount(sendAmount)
                    .sourceAsset(from.toDigitalBitsAsset())
                    .destinationAssets(listOf(to.toDigitalBitsAsset()))
                    .execute()
                    .records
            }
        }
        if(estimates.isFailure || estimates.getOrThrow().isEmpty()){
            return "0"
        }

        return estimates.getOrThrow()[0].destinationAmount


    }

    /**
     * Send an asset
     */
    suspend fun sendAssetAsync(
        recipient: String,
        asset: io.digitalbits.sdk.Asset,
        amount: String
    ): Deferred<SubmitTransactionResponse> = coroutineScope {

        val accountCreated = kotlin.runCatching {
            server.accounts().account(recipient)
            true
        }
        val tx = accountCreated.getOrNull()?.let {
            val txBuilder = Transaction.Builder(
                account,
                network
            ) /*TODO add config somewhere for network*/
                .addOperation(
                    PaymentOperation.Builder(
                        recipient,
                        asset,
                        amount
                    )
                        .build()
                ).setBaseFee(500)
                .setTimeout(180)

            val tx = txBuilder.build()
            tx.sign(keyPair)
            tx
        } ?: kotlin.run {
            if (asset != io.digitalbits.sdk.AssetTypeNative()) {
                throw AccountNotCreatedException()
            }
            if (amount.toDouble() < 1) {
                throw Exception("amount too low")
            }
            val txBuilder = Transaction.Builder(
                account,
                network
            ) /*TODO add config somewhere for network*/
                .addOperation(
                    CreateAccountOperation.Builder(
                        recipient,
                        amount
                    )
                        .build()
                ).setBaseFee(500)
                .setTimeout(180)

            val tx = txBuilder.build()
            tx.sign(keyPair)
            tx
        }
        async(Dispatchers.IO) {
            server.submitTransaction(tx)
        }
    }

    /**
     * Query frontier for all assets with the given asset code.
     */
    suspend fun findAssetsWithCode(code: String): List<Asset.Custom> {
        val assets = withContext(Dispatchers.IO) {
            (server
                .assets()
                .assetCode(code)
                .limit(200) as AssetsRequestBuilder)
                .execute()

        }.records.map {
            val asset = it.asset
            val (imageUrl, name) = parseToml(it.assetCode, it.assetIssuer)

            Asset.Custom(
                it.assetCode,
                it.assetIssuer,
                iconLink = imageUrl,
                name = name
            ) {
                if (imageUrl == null) {
                    null
                } else {
                    runBlocking(Dispatchers.IO) {
                        val bytes = httpClient.get<ByteArray>(imageUrl)
                        BitmapPainter(RemoteImage(bytes))
                    }
                }
            }

        }
        return assets

    }

    suspend fun addAsset(asset: Asset): Either<Exception,SubmitTransactionResponse>{
        val tx = Transaction.Builder(account,network)
            .addOperation(
                ChangeTrustOperation.Builder(asset.toDigitalBitsAsset(),(Int.MAX_VALUE-1).toString()).build()
            )
            .setBaseFee(300)
            .setTimeout(0)
            .build()
        tx.sign(keyPair)

        return try{
            withContext(Dispatchers.IO){
                Either.Right(server.submitTransaction(tx))
            }
        }catch (e: Exception){
            Either.Left(e)
        }

    }
    suspend fun removeAsset(asset: Asset.Custom): Either<Exception,SubmitTransactionResponse>{
        val tx = Transaction.Builder(account,network)
            .addOperation(
                ChangeTrustOperation.Builder(asset.toDigitalBitsAsset(),"0").build()
            )
            .setBaseFee(300)
            .setTimeout(0)
            .build()
        tx.sign(keyPair)

        return try{
            withContext(Dispatchers.IO){
                val res = server.submitTransaction(tx)
                println(res.isSuccess)
                println(res.resultXdr)
                Either.Right(res)
            }
        }catch (e: Exception){
            Either.Left(e)
        }

    }
}