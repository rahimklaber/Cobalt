import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import arrow.core.Either
import arrow.core.right
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.rahim.common.Sizes

@Composable
fun SwapScreen(wallet: Wallet,onFinished : () -> Unit) {
    var fromAsset : Asset by remember { mutableStateOf(Asset.Native) }
    var toAsset : Asset by remember { mutableStateOf(Asset.Native) }
    var job : Job by remember{ mutableStateOf(Job())}
    /**
     * Note: this is the amount of the source asset.
     */
    var amount : String by remember { mutableStateOf("") }
    var destAmount : String by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope { Dispatchers.Default }
    if(!submitted) {
        Card(Modifier.fillMaxWidth(Sizes.AssetsScreenPercentage).padding(5.dp)) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        amount,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.focusTarget().fillMaxWidth(0.75F),
                        onValueChange = { newVal ->
                            when (newVal.toDoubleOrNull()) {
                                null -> Unit
                                else -> {
                                    if (job.isActive) {
                                        job.cancel()
                                    }
                                    job = scope.launch {
                                        destAmount =
                                            wallet.estimateSwapAsync(fromAsset, toAsset, amount)
                                    }

                                    amount = newVal
                                }
                            }
                        },
                        label = { Text("Amount") },
                        singleLine = true
                    )
                    AssetChooser(
                        assets = wallet.assets,
                        onSelectedAssetChanged = { fromAsset = it })
                }
                Icon(Icons.Default.ArrowDownward, null)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        destAmount,
                        {},
                        modifier = Modifier.fillMaxWidth(0.75f),
                        enabled = false,
                        label = { Text("Amount") })
                    AssetChooser(assets = wallet.assets, onSelectedAssetChanged = { toAsset = it })
                }
                if (submitting) {
                    CircularProgressIndicator(Modifier.padding(5.dp))
                } else {
                    Button({
                        scope.launch {
                            submitting = true
                            val res =
                                wallet.swapAsync(
                                    fromAsset,
                                    toAsset,
                                    amount
                                ).await()

                            when (res) {
                                is Either.Left -> {
                                    submitting = false
                                    success = false
                                }
                                is Either.Right -> {
                                    submitting = false
                                    success = res.value.isSuccess
                                }
                            }
                            submitted = true

                        }
                    }, modifier = Modifier.padding(15.dp)) {
                        Text("Swap")
                    }
                }
            }
        }
    }else{
        Card(modifier = Modifier.padding(25.dp), elevation = 5.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (success) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint = Color.Green,
                        modifier = Modifier.size(100.dp).padding(10.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Error,
                        null,
                        tint = Color.Red,
                        modifier = Modifier.size(100.dp).padding(10.dp)
                    )
                }
                Text(
                    if (success) "Swap succeeded!" else "Something went wrong",
                    modifier = Modifier.padding(5.dp)
                )
                Button({ onFinished() }, modifier = Modifier.padding(5.dp)) {
                    Text("Ok")
                }
            }
        }
    }

}