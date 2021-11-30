import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.rahim.common.*
import State

@Composable
fun Balances(modifier: Modifier = Modifier, balances: Map<Asset, String>,loadFun : suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(fontWeight = FontWeight(600), text = "Balance", fontSize = 20.sp)
            var isRefreshing = rememberSwipeRefreshState(false)

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
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (balances.isEmpty()) {
                        item {
                            Text("Your account is not created on the network")
                        }
                    }
                    items(if(balances.isEmpty()) listOf(Asset.Native to "0.0000000") else balances.toList().reversed()) { (asset, assetBalance) ->

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .focusTarget()
                                .padding(10.dp)
                                .fillMaxWidth(Sizes.TransactionWidthPercentage)
                        ) {
                            if (asset is Asset.Native) {
                                Image(XdbImageResource(), null, Modifier.size(50.dp))
                            } else {
                                val image = (asset as Asset.Custom).image ?: PlaceholderImageResource()
                                Image(image, null, Modifier.clip(CircleShape as Shape).size(50.dp))
                            }
                            Spacer(Modifier.width(5.dp))
                            Column(
                                modifier = Modifier.focusTarget()
                                    .fillMaxWidth(Sizes.BalanceAssetWidthPercentage)
                            ) {
                                Text(asset.code, fontWeight = FontWeight.Medium)
                                Text(asset.name ?: "N/A", fontWeight = FontWeight.Light)
                            }
//                            Spacer(Modifier.width(Sizes.BalanceSpacer))
                            val split = assetBalance.split(".")
                            Box(Modifier.focusTarget().fillMaxWidth(), Alignment.CenterEnd) {
                                Text(
                                    text = "$assetBalance",
                                    modifier = Modifier.padding(5.dp)
                                )
                            }
                        }


                    }
                }
            }



        }
    }

}