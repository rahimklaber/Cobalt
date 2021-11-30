package me.rahim.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
@Composable
actual fun DropDownMenu(expanded : Boolean, onDismissRequest : () -> Unit, modifier: Modifier, content : @Composable ColumnScope.() -> Unit) = DropdownMenu(expanded = expanded,onDismissRequest = onDismissRequest, modifier = modifier, content = content)

@Composable
actual fun DropDownMenuItem(onClick : () -> Unit,content: @Composable RowScope.() -> Unit) = DropdownMenuItem(onClick = onClick, content = content)