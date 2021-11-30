package me.rahim.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
@Composable
expect fun DropDownMenu(expanded : Boolean, onDismissRequest : () -> Unit, modifier: Modifier = Modifier, content : @Composable ColumnScope.() -> Unit)

@Composable
expect fun DropDownMenuItem(onClick : () -> Unit, content: @Composable RowScope.() -> Unit)