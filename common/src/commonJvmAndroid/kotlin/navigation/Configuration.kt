package navigation

import App
import Asset
import AssetsScreen
import BottomBar
import ReceiveScreen
import SendScreen
import SettingsScreen
import Storage
import SwapScreen
import Transactions
import Wallet
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Waves
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import arrow.core.Either
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import com.arkivanov.decompose.pop
import com.arkivanov.decompose.push
import com.arkivanov.decompose.router
import com.arkivanov.essenty.backpressed.BackPressedDispatcher
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import io.digitalbits.sdk.KeyPair
import io.digitalbits.sdk.responses.operations.PaymentOperationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class Configuration : Parcelable {
    @Parcelize
    object App : Configuration()

    @Parcelize
    object Send : Configuration()

    @Parcelize
    object Transactions : Configuration()

    @Parcelize
    object Receive : Configuration()

    @Parcelize
    object Swap : Configuration()

    @Parcelize
    object Assets : Configuration()

    @Parcelize
    object None : Configuration()

    @Parcelize
    object Settings : Configuration()
}
typealias Content = @Composable () -> Unit

fun <T : Any> T.asContent(content: @Composable (T) -> Unit): Content = {
    content(this)
}

class NavigationItem(val title: String, val Icon: ImageVector, val onclick: () -> Unit)

class Root(
    componentContext: ComponentContext, // In Decompose each component has its own ComponentContext
    override val backPressedDispatcher: BackPressedDispatcher = BackPressedDispatcher()
) : ComponentContext by componentContext {
    var headerString by mutableStateOf("Home")
    val storage = Storage
    lateinit var wallet: Wallet
    lateinit var scaffoldState: ScaffoldState

    fun initWallet() {
        println("secret ${storage.secret}")
        println("account created ${storage.accountCreated}")
        val initialBalances = if(Storage.balances.XdbBalance!="0"){
            mutableMapOf<Asset, String>().apply {
                val balances = Storage.balances
                putAll(balances.customBalances)
                put(Asset.Native, balances.XdbBalance)
            }.toMap()
        }else{
            mutableMapOf()
        }
        wallet = Wallet(
            storage.secret ?: throw Error("No wallet found"),
            initialBalances = initialBalances
        )
    }

    private val router =
        router<Configuration, Content>(
            initialConfiguration = Configuration.None, // Starting with List
            childFactory = ::createChild // The Router calls this function, providing the child Configuration and ComponentContext
        )

    val routerState = router.state

    private fun createChild(configuration: Configuration, context: ComponentContext): Content =
        when (configuration) {
            is Configuration.App -> app()
            is Configuration.Send -> send()
            is Configuration.Transactions -> transactions()
            is Configuration.Receive -> receive()
            is Configuration.None -> none()
            is Configuration.Swap -> swap()
            is Configuration.Assets -> assets()
            is Configuration.Settings -> settings()
        } // Configurations are handled exhaustively

    fun settings(): Content {
        return {
            SettingsScreen(wallet)
        }
    }

    fun none(): Content = {}
    fun start() {

        router.push(Configuration.App)
    }

    fun receive(): Content {
        return {
            ReceiveScreen(wallet)
        }
    }

    fun transactions(): Content {
        return Unit.asContent {
            Transactions(wallet.transactions, wallet.accountId, wallet, wallet::reload)
        }
    }

    fun swap(): Content {
        return Unit.asContent {
            SwapScreen(wallet) { router.push(Configuration.App);headerString = "Home" }
        }
    }

    fun assets(): Content {
        return Unit.asContent {
            AssetsScreen(search = wallet::findAssetsWithCode, addAssetFun = {
                when (wallet.addAsset((it))) {
                    is Either.Right -> true
                    is Either.Left -> false
                }
            }, reloadFun = {
                wallet.reload()
            }, checkIfAssetAdded = {
                wallet.assets.contains(it)
            }, assets = wallet.assets, removeAssetFun = {
                when (wallet.removeAsset((it))) {
                    is Either.Right -> true
                    is Either.Left -> false
                }
            })
        }
    }

    fun app(): Content {

        return AppDetails(
            onSendSelected = {
                router.push(Configuration.Send);headerString = "Send"
            },
            onReceiveSelected = { router.push(Configuration.Receive); headerString = "Receive" },
            onSwapSelected = { router.push(Configuration.Swap);headerString = "Swap" },
            onAssetsSelected = { router.push(Configuration.Assets);headerString = "Assets" },
            wallet = wallet
        )
            .asContent {
                App(appDetails = it)
            }
    }

    private fun send(): Content {
        return SendDetails(onFinished = router::pop)
            .asContent {
                backPressedDispatcher.register { router.pop();true }
                SendScreen(wallet, it)

            }
    }

    fun BottomBarDetails(): List<NavigationItem> {
        return listOf(
            NavigationItem(
                "Home",
                Icons.Default.Home
            ) { router.push(Configuration.App); headerString = "Home" },
            NavigationItem(
                "Transactions",
                Icons.Default.Waves
            ) { router.push(Configuration.Transactions); headerString = "Transactions" },
            NavigationItem(
                "Settings",
                Icons.Default.Settings
            ) {
                router.push(Configuration.Settings); headerString = "Settings"
            }
        )
    }

}

class AppDetails(
    val wallet: Wallet,
    val onSendSelected: () -> Unit,
    val onReceiveSelected: () -> Unit,
    val onSwapSelected: () -> Unit,
    val onAssetsSelected: () -> Unit
)

class SendDetails(val onFinished: () -> Unit)
class TransactionsDetails(
    val transactions: List<PaymentOperationResponse>,
    val reloadFun: () -> Unit
)

@Composable
fun RootUi(root: Root) {
//    Storage()
    var loggedIn by remember { mutableStateOf(root.storage.accountCreated) }
    var loading by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()
    root.scaffoldState = scaffoldState
    val scope = rememberCoroutineScope()
    if (loggedIn) {
        root.initWallet()
        root.start()
    }
    if (!loggedIn) {
        Column(
            modifier = Modifier.background(Color(244, 162, 97)).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                "Welcome to Cobalt",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 40.sp
            )
            if(!loading){
                Button({
                    scope.launch(Dispatchers.Default) {
                        loading = true
                        root.storage.secret = String(KeyPair.random().secretSeed)
                        println(root.storage.secret)
                        root.storage.accountCreated = true
                        loading = false
                        loggedIn = true
                    }
                }) {
                    Text("Generate an Account", color = Color.White)
                }
            }else{
                CircularProgressIndicator()
            }
        }
    } else {

        Scaffold(
            scaffoldState = root.scaffoldState,
            bottomBar = { BottomBar(root.BottomBarDetails()) },
            topBar = {
                TopAppBar {
                    Text(
                        root.headerString,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier.focusTarget().fillMaxSize().padding(it),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Children(root.routerState) { child ->
                    child.instance()
                }
            }

        }
    }


}