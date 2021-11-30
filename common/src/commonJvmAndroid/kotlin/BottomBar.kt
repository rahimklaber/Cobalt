import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import navigation.NavigationItem


@Composable
fun BottomBar(items: List<NavigationItem>) {
    BottomNavigation {
        items.forEach { navItem ->
            BottomNavigationItem(
                selected = false,
                icon = { Icon(navItem.Icon, null) },
                label = { Text(navItem.title) },
                onClick = navItem.onclick
            )
        }
    }
}