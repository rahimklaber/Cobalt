import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.extensions.compose.jetbrains.rememberRootComponent
import navigation.Root
import navigation.RootUi

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Cobalt"
        ) {
        CustomMaterialTheme {
            RootUi(root())
        }
    }
}

@Composable
private fun root(): Root =
    // The rememberRootComponent function provides the root ComponentContext and remembers the instance or Root
    rememberRootComponent { componentContext ->
        Root(
            componentContext = componentContext
        )
    }