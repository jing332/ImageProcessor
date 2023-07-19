package com.github.jing332.image_processor.ui

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.jing332.image_processor.BuildConfig
import com.github.jing332.image_processor.R
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {
        TextButton(onClick = { onDismissRequest() }) {
            Text(stringResource(id = android.R.string.ok))
        }
    },
        title = { Text(stringResource(id = R.string.about)) },
        text = {
            SelectionContainer {
                Text(
                    stringResource(
                        id = R.string.about_content,
                        "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault()
                        ).format(BuildConfig.BUILD_TIME * 1000)
                    )
                )
            }
        }
    )
}