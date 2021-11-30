package me.rahim.common

import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
actual fun copyTextFun(data: String) : () -> Unit {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val content = StringSelection(data)
    return {
        clipboard.setContents(content,content)
    }
}