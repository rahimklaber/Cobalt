import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.unit.dp
import me.rahim.common.*

@Composable
fun AssetChooser(modifier: Modifier = Modifier,assets: List<Asset>,onSelectedAssetChanged: (Asset) -> Unit){
    DropdownAssetList("Assets", assets,modifier,onSelectedAssetChanged)
}

@Composable
fun DropdownAssetList(text: String, assets: List<Asset>, modifier : Modifier = Modifier, onSelectedAssetChanged: (Asset) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }
    var selectedAsset : Asset by remember { mutableStateOf(Asset.Native) }
    val icon = if (expanded)
        Icons.Filled.ArrowDropUp
    else
        Icons.Filled.ArrowDropDown


    Column(modifier){
            OutlinedTextField(
                value = selectedText,
                onValueChange = { },
//                modifier = Modifier.fillMaxWidth(),
                label = { Text("asset") },
                trailingIcon = {
                    Icon(icon, "contentDescription", Modifier.focusTarget().clickable { expanded = !expanded })
                },
                readOnly = true,
            )


        DropDownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = modifier.fillMaxHeight(0.5f)
        ) {
            assets.forEach { asset ->
                DropDownMenuItem(onClick = {
                    selectedText = asset.code
                    selectedAsset = asset
                    onSelectedAssetChanged(selectedAsset)
                }) {
                    Row{
                        when (asset) {
                            is Asset.Native -> {
                                Image(XdbImageResource(), null, Modifier.focusTarget().size(50.dp))
                            }
                            is Asset.Custom -> {
                                Image(asset.image?:PlaceholderImageResource(), null, Modifier.focusTarget().size(50.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = asset.code)
                    }
                }
            }
        }
    }
}