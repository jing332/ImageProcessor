package com.github.jing332.image_processor.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.max


/**
 * 下拉框菜单
 */
@Composable
fun DropMenuTextField(
    modifier: Modifier = Modifier,
    label: @Composable() (() -> Unit),
    key: Any,
    keys: List<Any>,
    values: List<String>,
    onKeyChange: (key: Any) -> Unit,
) {
    var value = values.getOrNull(max(0, keys.indexOf(key))) ?: ""
    var menuExpanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = TextFieldValue(value),
        onValueChange = { value = "" },
        label = label,
        readOnly = true,
        enabled = false,
        colors = TextFieldDefaults.colors(
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = { menuExpanded = true }
            ),
        trailingIcon = {
            Icon(if (menuExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown, null)
        }
    )

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }) {
        values.forEachIndexed { index, s ->
            val checked = key == keys[index]
            DropdownMenuItem(
                text = {
                    Text(
                        s, fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal
                    )
                },
                onClick = {
                    menuExpanded = false
                    value = s
                    onKeyChange.invoke(keys[index])
                }, modifier = Modifier.background(
                    if (checked) MaterialTheme.colorScheme.surfaceVariant
                    else Color.Transparent
                )
            )
        }
    }
}

@Preview
@Composable
fun PreviewDropMenu() {
    var key by remember { mutableIntStateOf(1) }
    DropMenuTextField(
        label = { Text("所属分组") },
        key = key,
        keys = listOf(1, 2, 3),
        values = listOf("1", "2", "3"),
    ) {
        key = it as Int
    }
}