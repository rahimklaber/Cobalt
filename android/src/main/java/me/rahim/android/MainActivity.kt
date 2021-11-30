package me.rahim.android

import CustomMaterialTheme
import Storage
import Wallet
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.essenty.backpressed.BackPressedDispatcher
import com.arkivanov.essenty.backpressed.backPressedDispatcher
import navigation.Root
import navigation.RootUi

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = Root(
            defaultComponentContext(),
            BackPressedDispatcher(onBackPressedDispatcher))
        setContent {
            backPressedDispatcher()
            CustomMaterialTheme {
                RootUi(root)
            }
        }
    }
}
//@Composable
//private fun root(): Root =
//    // The rememberRootComponent function provides the root ComponentContext and remembers the instance or Root
//    rememberRootComponent { componentContext ->
//        Root(
//            componentContext = componentContext
//        )
//    }