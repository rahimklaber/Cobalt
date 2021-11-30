package me.rahim.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.getSystemService

@Composable
actual fun copyTextFun(data: String) : () -> Unit {
    val context = LocalContext.current
    val clipboard = getSystemService(context,ClipboardManager::class.java) as ClipboardManager
    val clip: ClipData = ClipData.newPlainText("cobaltCopy", data)
    return {
        clipboard.setPrimaryClip(clip)
    }
}