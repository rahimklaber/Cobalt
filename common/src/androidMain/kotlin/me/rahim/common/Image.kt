package me.rahim.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import shadow.com.google.common.io.ByteSource

//@Composable
////actual  fun imageResource(fileName: String) : Painter {
////    return painterResource(fileName)
////}

@Composable
actual fun XdbImageResource(): Painter {
    return painterResource(R.drawable.xdb_logo)
}
@Composable
actual fun PlaceholderImageResource() : Painter{
    return painterResource(R.drawable.placeholder)
}
actual fun QrBitmapFromContent(content: String,width: Int, height:Int) : ImageBitmap{
    val qrCode = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE,width,height)
    val pixels = IntArray(width * height)
    val white = -0x1
    val black = -0x1000000
    for (y in 0 until height) {
        val offset: Int = y * width
        for (x in 0 until width) {
            pixels[offset + x] = if (qrCode.get(
                    x,
                    y
                )
            ) black else white
        }
    }
    val bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels,0,width,0,0,width,height)
    return bitmap.asImageBitmap()
}

actual fun RemoteImage(bytes: ByteArray) : ImageBitmap{
    return BitmapFactory.decodeByteArray(bytes,0,bytes.size).asImageBitmap()

}