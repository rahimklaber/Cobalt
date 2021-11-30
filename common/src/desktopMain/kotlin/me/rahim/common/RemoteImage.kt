package me.rahim.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import shadow.com.google.common.io.ByteSource
import java.awt.image.BufferedImage
import java.io.InputStream
import java.io.InputStreamReader
import javax.imageio.ImageIO


@Composable
actual fun XdbImageResource(): Painter {
    return painterResource("xdb_logo.png")
}
@Composable
actual fun PlaceholderImageResource() : Painter{
    return painterResource("placeholder.jpg")
}

actual fun QrBitmapFromContent(content: String,width: Int, height:Int) : ImageBitmap{
    val qrCode = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE,width,height)
   return MatrixToImageWriter.toBufferedImage(qrCode).toComposeBitmap()
}

actual fun RemoteImage(bytes: ByteArray) : ImageBitmap{
    return ImageIO.read(ByteSource.wrap(bytes).openStream()).toComposeBitmap()
}