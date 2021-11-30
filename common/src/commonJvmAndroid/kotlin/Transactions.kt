import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.digitalbits.sdk.responses.operations.PaymentOperationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.rahim.common.PlaceholderImageResource
import me.rahim.common.Sizes
import me.rahim.common.XdbImageResource
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

/**
 * @param address The users address
 */
@Composable
fun Transactions(
    transactions: List<PaymentOperationResponse>,
    address: String,
    wallet: Wallet,
    loadFun: suspend () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isRefreshing = rememberSwipeRefreshState(false)
    Card {
        SwipeRefresh(
            isRefreshing,
            onRefresh = { scope.launch(Dispatchers.Default) {
                isRefreshing.isRefreshing = true
                loadFun()
                isRefreshing.isRefreshing = false
            }
            }
        ) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(Sizes.TransactionWidthPercentage).padding(10.dp)
            ) {
                if (transactions.isEmpty()) {
                    item {
                        Text("No transactions found")
                    }
                }
                items(wallet.transactions) {
                    val txAsset =
                        Asset.fromSdkAsset(it.asset) // we shouldn't do this anymore since we tore the image in the asset. Hence the find in wallet.assets
                    val foundAsset = wallet.assets.find { it == txAsset }
                    val type = when (it.from == address) {
                        true -> TransactionType.Sent(foundAsset ?: txAsset, it.to, it.amount)
                        false -> TransactionType.Received(foundAsset ?: txAsset, it.from, it.amount)
                    }
                    Transactionitem(type)
                }
            }
        }
    }

}

sealed class TransactionType(val asset: Asset, val amount: String) {
    class Sent(asset: Asset, val recipient: String, amount: String) : TransactionType(asset, amount)
    class Received(asset: Asset, val sender: String, amount: String) :
        TransactionType(asset, amount)
}

fun shortenAddress(address: String): String {
    return address.take(4) + "..." + address.takeLast(4)
}

@Composable
fun Transactionitem(type: TransactionType, modifier: Modifier = Modifier.padding(5.dp)) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(Sizes.TransactionWidthPercentage)
    ) {
        when (type.asset) {
            is Asset.Native -> {
                Image(XdbImageResource(), null, Modifier.focusTarget().size(50.dp))
            }
            is Asset.Custom -> {
                Image(
                    type.asset.image ?: PlaceholderImageResource(),
                    null,
                    Modifier.focusTarget().size(50.dp)
                )
            }
        }
        val split = type.amount.split(".")
        val beforeDecimal = split[0]
        Spacer(Modifier.width(5.dp))
        val afterDecimal = if (split[1].length > 1) split[1].substring(0, 2) else split[1]
        Column {
            when (type) {
                is TransactionType.Received -> {
                    Row {
                        Text("from ")
                        Text(shortenAddress(type.sender), fontWeight = FontWeight.Medium)
                    }
                    Text("received", fontWeight = FontWeight.Light)
                }
                is TransactionType.Sent -> {
                    Row {
                        Text("to ")
                        Text(shortenAddress(type.recipient), fontWeight = FontWeight.Medium)
                    }
                    Text("sent", fontWeight = FontWeight.Light)
                }
            }
        }
        Box(Modifier.focusTarget().fillMaxWidth(), Alignment.CenterEnd) {
            val sign = when (type) {
                is TransactionType.Received -> "+"
                is TransactionType.Sent -> "-"
            }
            Text(
                "${sign}${beforeDecimal}.${afterDecimal} ${type.asset.code}",
                modifier = Modifier.padding(5.dp)
            )
        }

    }
}