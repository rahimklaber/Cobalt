import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val colors = lightColors(
    primary=Color(0xFF0047ab),
//    primary = Color(38, 70, 83), // charcoal
//    background = Color.White,
    background = Color(0xFFD6E7FF)
//    background = Color(187, 181, 189), // navajo white
//    background = Color(244, 162, 97), // sandy brown
//    surface = Color(233, 196, 106) // crayola
)

val shapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(15.dp),
    large = RoundedCornerShape(20.dp)
)
@Composable
fun CustomMaterialTheme(content : @Composable () -> Unit){
    MaterialTheme(colors = colors, content = content,shapes = shapes)
}