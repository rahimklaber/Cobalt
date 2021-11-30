import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.rahim.common.PlaceholderImageResource
import me.rahim.common.Sizes

@Composable
fun AssetsScreen(
    search: suspend (String) -> List<Asset.Custom>,
    addAssetFun: suspend (Asset.Custom) -> Boolean,
    removeAssetFun: suspend (Asset.Custom) -> Boolean,
    reloadFun: suspend () -> Unit,
    checkIfAssetAdded: (Asset.Custom) -> Boolean,
    assets: List<Asset>
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = Modifier.fillMaxWidth(),

    ) {
        Tab(
            selected = selectedTabIndex == 0,
            onClick = { selectedTabIndex = 0 },
            modifier = Modifier.height(50.dp),
            text = { Text("Add assets") }
        )
        Tab(
            selected = selectedTabIndex == 1,
            onClick = { selectedTabIndex = 1 },
            modifier = Modifier.height(50.dp),
            text = { Text("Manage assets") }
        )
    }
    if (selectedTabIndex == 0) {
        AddAssetsScreen(search, addAssetFun, reloadFun, checkIfAssetAdded)
    } else if (selectedTabIndex == 1) {
        ManageAssetsScreen(
            assets,
            removeAssetFun,
            reloadFun
        )
    }
}

@Composable
fun AddAssetsScreen(
    search: suspend (String) -> List<Asset.Custom>,
    addAssetFun: suspend (Asset.Custom) -> Boolean,
    reloadFun: suspend () -> Unit,
    checkIfAssetAdded: (Asset.Custom) -> Boolean
) {
    val scope = rememberCoroutineScope()
    var assetCode by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    var foundAssets by remember { mutableStateOf(listOf<Asset.Custom>()) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(Modifier.fillMaxWidth(Sizes.AssetsScreenPercentage).padding(5.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = assetCode,
                        onValueChange = { assetCode = it },
                        label = { Text("asset code") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(5.dp))
                    Button({
                        loading = true
                        foundAssets = listOf()
                        scope.launch(Dispatchers.Default) {
                            foundAssets = search(assetCode)
                            loading = false
                        }
                    }) {
                        Icon(Icons.Default.Search, "search")
                    }
                }
            }
        }
        if (!loading) {
            Card(Modifier.fillMaxWidth(Sizes.AssetsScreenPercentage).padding(5.dp)) {
                LazyColumn {
                    items(foundAssets) { asset ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .focusTarget()
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            var addingAsset by remember { mutableStateOf(false) }
                            val image = asset.image ?: PlaceholderImageResource()
                            Image(image, null, Modifier.clip(CircleShape as Shape).size(50.dp))
                            Spacer(Modifier.width(5.dp))
                            Column(
                                modifier = Modifier.focusTarget()
                                    .fillMaxWidth(Sizes.BalanceAssetWidthPercentage)
                            ) {
                                Text(asset.code, fontWeight = FontWeight.Medium)
                                Text(asset.name ?: "N/A", fontWeight = FontWeight.Light)
                            }
                            Box(
                                contentAlignment = Alignment.CenterEnd,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (!addingAsset) {
                                    Button(
                                        {
                                            addingAsset = true
                                            scope.launch(Dispatchers.Default) {
                                                addAssetFun(asset)
                                                reloadFun()
                                                addingAsset = false
                                            }
                                        },
                                        enabled = !checkIfAssetAdded(asset)
                                    ) {
                                        if (checkIfAssetAdded(asset)) {
                                            Text("Already added")
                                        } else {
                                            Text("Add")
                                        }
                                    }
                                } else {
                                    CircularProgressIndicator(Modifier.padding(5.dp))
                                }
                            }
                        }
                    }
                }
            }
        } else {

            CircularProgressIndicator(Modifier.padding(15.dp))

        }
    }

}

@Composable
fun ManageAssetsScreen(
    assets: List<Asset>,
    removeAssetFun: suspend (Asset.Custom) -> Boolean,
    reloadFun: suspend () -> Unit
) {
    val scope = rememberCoroutineScope()


    Card(Modifier.fillMaxWidth(Sizes.AssetsScreenPercentage).padding(5.dp)) {
        LazyColumn {
            items(assets) { asset ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .focusTarget()
                        .padding(10.dp)
                        .fillMaxWidth()
                ) {
                    var removingAsset by remember { mutableStateOf(false) }
                    val image = asset.image ?: PlaceholderImageResource()
                    Image(image, null, Modifier.clip(CircleShape as Shape).size(50.dp))
                    Spacer(Modifier.width(5.dp))
                    Column(
                        modifier = Modifier.focusTarget()
                            .fillMaxWidth(Sizes.BalanceAssetWidthPercentage)
                    ) {
                        Text(asset.code, fontWeight = FontWeight.Medium)
                        Text(asset.name ?: "N/A", fontWeight = FontWeight.Light)
                    }
                    Box(
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!removingAsset) {
                            Button({
                                removingAsset = true
                                scope.launch(Dispatchers.Default) {
                                    removeAssetFun(asset as Asset.Custom)
                                    reloadFun()
                                    removingAsset = false
                                }
                            },enabled = asset !is Asset.Native
                            ) {

                                Text("Remove")

                            }
                        } else {
                            CircularProgressIndicator(Modifier.padding(5.dp))
                        }
                    }
                }
            }
        }
    }

}