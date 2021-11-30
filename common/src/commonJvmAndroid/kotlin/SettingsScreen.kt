import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(wallet: Wallet){
    Card(Modifier.fillMaxSize(),shape = RectangleShape){
    SettingsGroup("Secret") {
        SettingsItem("Delete account"){
            println("HI")
        }
        SettingsItem("Switch network"){
            println("HI")
        }
    }
    }
}

@Composable
fun SettingsGroup(text : String = "",content :  LazyListScope.() -> Unit){
    Column {
        Text(text,fontSize = 20.sp,fontWeight = FontWeight.Medium)
        LazyColumn(horizontalAlignment = Alignment.CenterHorizontally,modifier = Modifier.background(MaterialTheme.colors.background)){
            content()
        }
    }
}


fun LazyListScope.SettingsItem(text: String,modifier : Modifier = Modifier, action : () -> Unit){
    item {
        Text(text, modifier = modifier.focusTarget().clickable {
            action()
        })
    }
}
