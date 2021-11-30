import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.rahim.common.*
import navigation.AppDetails
import navigation.SendDetails


@Composable
fun App(appDetails: AppDetails) {
    val wallet =  remember{appDetails.wallet}
    //Todo fix
    val nativeImage = XdbImageResource()
    Asset.Native._image = nativeImage
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OptionsRow(details = appDetails)
        Balances(balances = wallet.assetBalances,loadFun = wallet::reload)
    }

}


@Composable
fun OptionsRow(details: AppDetails) {
    Row {
        Button(details.onSendSelected, Modifier.focusTarget().padding(10.dp)) {
            Text("Send")
        }
        Button(details.onReceiveSelected, Modifier.focusTarget().padding(10.dp)) {
            Text("Receive")
        }
        Button(details.onAssetsSelected, Modifier.focusTarget().padding(10.dp)) {
            Text("Assets")
        }
        Button(details.onSwapSelected, Modifier.focusTarget().padding(10.dp)) {
            Text("Swap")
        }
    }
}

@Composable
fun SendScreen(wallet: Wallet, details: SendDetails) {
    val scope = rememberCoroutineScope()
    var submitting by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    if (!submitted) {
        Card(
            modifier = Modifier.focusTarget().fillMaxWidth(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                var asset : Asset by remember { mutableStateOf(Asset.Native) }
                AssetChooser(Modifier.focusTarget().fillMaxWidth(0.75f).padding(15.dp),wallet.assets) {
                    asset = it
                }

                var recipient by remember { mutableStateOf("") }
                OutlinedTextField(
                    recipient,
                    modifier = Modifier.focusTarget().fillMaxWidth(0.75F).padding(15.dp),
                    onValueChange = { newVal -> recipient = newVal },
                    label = { Text("Recipient") },
                    singleLine = true,
                )
                var amount by remember { mutableStateOf("") }
                OutlinedTextField(
                    amount,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.focusTarget().fillMaxWidth(0.75F).padding(15.dp),
                    onValueChange = { newVal ->
                        when (newVal.toDoubleOrNull()) {
                            null -> Unit
                            else -> amount = newVal
                        }
                    },
                    label = { Text("Amount") },
                    singleLine = true
                )
                Button({
                    scope.launch(Dispatchers.IO) {
                        submitting = true
                        submitted = true
                        val res = kotlin.runCatching {
                            val submitRes = wallet.sendAssetAsync(
                                recipient,
                                asset.toDigitalBitsAsset(),
                                amount
                            ).await()
                            wallet.reload()
                            submitRes
                        }.getOrNull()

                        res?.let {
                            submitting = false
                            success = res.isSuccess
                        } ?: kotlin.run {
                            submitting = false
                            success = false
                        }

                    }
                }, modifier = Modifier.padding(15.dp)) {
                    Text("Send")
                }
            }
        }

    } else {
        if (submitted && submitting) {
            CircularProgressIndicator(Modifier.padding(15.dp))
        } else {
            Card(modifier = Modifier.focusTarget().padding(25.dp), elevation = 5.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (success) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = Color.Green,
                            modifier = Modifier.focusTarget().size(100.dp).padding(10.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Error,
                            null,
                            tint = Color.Red,
                            modifier = Modifier.focusTarget().size(100.dp).padding(10.dp)
                        )
                    }
                    Text(
                        if (success) "Payment succeeded!" else "Something went wrong",
                        modifier = Modifier.padding(5.dp)
                    )
                    Button({ details.onFinished() }, modifier = Modifier.padding(5.dp)) {
                        Text("Ok")
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiveScreen(wallet: Wallet) {
    val scope = rememberCoroutineScope()
    val copiedText = "copied"
    val normalText = "copy"
    var showNormal by remember { mutableStateOf(true) }
    Card {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Address")
            Text(wallet.accountId, fontWeight = FontWeight.Medium)
            val onclick = copyTextFun(wallet.accountId)
            Button({
               onclick()
                scope.launch {
                    showNormal = false
                    delay(1666)
                    showNormal = true
                }
            }) {
                Text(if(showNormal) normalText else copiedText)
            }
            Image(
                BitmapPainter(QrBitmapFromContent(wallet.accountId, 400, 400)),
                null,
                modifier = Modifier.focusTarget().size(300.dp)
            )

        }
    }
}