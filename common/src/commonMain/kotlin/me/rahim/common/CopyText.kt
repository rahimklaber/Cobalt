package me.rahim.common

import androidx.compose.runtime.Composable

@Composable
expect fun copyTextFun(data : String) : () -> Unit