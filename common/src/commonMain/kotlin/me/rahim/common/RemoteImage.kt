package me.rahim.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.Placeholder

/**
 * From : https://dev.to/gerardpaligot/sharing-compose-components-between-android-and-desktop-17kg
 */

//expect fun imageResource(fileName: String) : Painter
@Composable
expect fun XdbImageResource(): Painter
@Composable
expect fun PlaceholderImageResource() : Painter

expect fun QrBitmapFromContent(content: String,width: Int, height:Int) : ImageBitmap

expect fun RemoteImage(bytes: ByteArray) : ImageBitmap