package com.github.jing332.image_processor.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.github.jing332.image_processor.R
import com.github.jing332.image_processor.help.AppConfig
import com.github.jing332.image_processor.ui.theme.ImageConvTheme
import com.github.jing332.image_processor.ui.widgets.DropMenuTextField
import com.github.jing332.image_processor.utils.ASFUriUtils.getPath
import com.github.jing332.text_searcher.ui.widgets.LabelSlider
import com.github.jing332.text_searcher.ui.widgets.TransparentSystemBars
import com.origeek.imageViewer.viewer.ImageViewer
import com.origeek.imageViewer.viewer.rememberViewerState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImageConvTheme {
                TransparentSystemBars()

                var previewImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
                var showPreviewImage by remember { mutableStateOf(false) }

                val scope = rememberCoroutineScope()
                // 渐入渐出
                Crossfade(
                    targetState = showPreviewImage, animationSpec = tween(500), label = ""
                ) { isShow ->
                    if (isShow) { // 预览大图
                        BackHandler { showPreviewImage = false }
                        val state = rememberViewerState()
                        ImageViewer(
                            state = state,
                            model = previewImageBitmap ?: ImageBitmap(20, 20),
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        var showAboutDialog by remember { mutableStateOf(false) }
                        if (showAboutDialog)
                            AboutDialog { showAboutDialog = false }

                        var showFolderEditDialog by remember { mutableStateOf(false) }
                        if (showFolderEditDialog) {
                            var folderName by remember { mutableStateOf(AppConfig.targetFolderName.value) }
                            AlertDialog(
                                onDismissRequest = { showFolderEditDialog = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        AppConfig.targetFolderName.value = folderName
                                        showFolderEditDialog = false
                                    }) {
                                        Text(stringResource(android.R.string.ok))
                                    }
                                },
                                text = {
                                    OutlinedTextField(
                                        value = folderName,
                                        onValueChange = { folderName = it },
                                        label = { Text(stringResource(id = R.string.edit_target_folder)) }
                                    )
                                },
                                title = {
                                    Text(stringResource(R.string.edit_target_folder))
                                }
                            )
                        }
                        Scaffold(
                            modifier = Modifier.imePadding(),
                            topBar = {
                                TopAppBar(title = { Text(stringResource(id = R.string.app_name)) },
                                    actions = {
                                        var showMoreOptions by remember {
                                            mutableStateOf(
                                                false
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showMoreOptions,
                                            onDismissRequest = {
                                                showMoreOptions = false
                                            }) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(id = R.string.about)) },
                                                onClick = {
                                                    showAboutDialog = true
                                                    showMoreOptions = false
                                                }
                                            )

                                            DropdownMenuItem(
                                                text = { Text(stringResource(id = R.string.edit_target_folder)) },
                                                onClick = {
                                                    showFolderEditDialog = true
                                                    showMoreOptions = false
                                                }
                                            )
                                        }

                                        IconButton(onClick = {
                                            showMoreOptions = true
                                        }) {
                                            Icon(
                                                Icons.Default.MoreVert,
                                                contentDescription = stringResource(R.string.more_options)
                                            )
                                        }
                                    })
                            }
                        ) {
                            ProcessorScreen(
                                Modifier
                                    .padding(it)
                                    .fillMaxHeight()
                                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                                previewImage = {
                                    previewImageBitmap = it
                                    showPreviewImage = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessorScreen(
    modifier: Modifier,
    vm: ProcessorViewModel = viewModel(),
    previewImage: (ImageBitmap) -> Unit
) {
    val context = LocalContext.current
    var lastSrcDir by remember { AppConfig.sourceDirectory }

    val dirSelection =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                vm.srcDir = uri.toString()
                lastSrcDir = vm.srcDir

                vm.loadDir(context, uri)
            }
        }

    Column(modifier = modifier) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = context.getPath(
                vm.srcDir.toUri(), true
            ) ?: vm.srcDir,
            onValueChange = { vm.srcDir = it },
            label = { Text(stringResource(R.string.source_directory)) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { dirSelection.launch(null) }) {
                    Icon(
                        Icons.Filled.FileOpen,
                        contentDescription = stringResource(R.string.select_folder)
                    )
                }
            },
            placeholder = { Text(stringResource(R.string.click_button_select_dir)) }
        )

        if (vm.srcDir.isEmpty() && lastSrcDir.isNotEmpty()) {
            Box(
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple()
                    ) {
                        vm.srcDir = lastSrcDir
                        vm.loadDir(context, lastSrcDir.toUri())
                    },
            ) {
                Text(
                    modifier = Modifier.padding(2.dp),
                    text = stringResource(
                        R.string.last_directory,
                        context.getPath(lastSrcDir.toUri(), true) ?: lastSrcDir
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    overflow = TextOverflow.Visible,
                )
            }
        }


        LazyColumn(Modifier.weight(1f)) {
            items(vm.files, { it.uriString }) { file ->
                FileItemScreen(
                    uri = file.uriString.toUri(),
                    name = file.name,
                    state = file.processState,
                    size = file.size,
                ) {
                    val img = BitmapFactory.decodeStream(
                        context.contentResolver.openInputStream(file.uri)
                    )
                    previewImage(img.asImageBitmap())
                }
                Divider(
                    Modifier
                        .height(0.8.dp)
                        .padding(horizontal = 4.dp)
                )
            }
        }

        Column {
            if (vm.process > 0)
                LinearProgressIndicator(progress = vm.process, modifier = Modifier.fillMaxWidth())

            Row() {
                DropMenuTextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    label = { Text(stringResource(R.string.target_format)) },
                    key = vm.format,
                    keys = Bitmap.CompressFormat.values().map { it.name },
                    values = Bitmap.CompressFormat.values().map { it.name },
                    onKeyChange = {
                        vm.format = it.toString()
                    }
                )

                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    value = vm.width,
                    onValueChange = { vm.width = it },
                    label = { Text(stringResource(R.string.width)) },
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    value = vm.height,
                    onValueChange = {
                        vm.height = it
                    },
                    label = { Text(stringResource(R.string.height)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }

            var quality by remember { mutableIntStateOf(100) }
            if (vm.format != Bitmap.CompressFormat.PNG.name)
                LabelSlider(
                    value = quality.toFloat(),
                    onValueChange = { quality = it.toInt() },
                    valueRange = 0f..100f
                ) {
                    Text(stringResource(R.string.image_quality, quality))
                }

            var showWarnDialog by remember { mutableStateOf(false) }
            if (showWarnDialog)
                ExecuteWarnDialog(onDismissRequest = { showWarnDialog = false }) {
                    vm.executeConv(
                        context,
                        srcUri = vm.srcDir.toUri(),
                        format = Bitmap.CompressFormat.valueOf(vm.format),
                        quality = quality,
                        width = try {
                            vm.width.toInt()
                        } catch (_: Exception) {
                            0
                        },
                        height = try {
                            vm.height.toInt()
                        } catch (_: Exception) {
                            0
                        },
                        folderName = AppConfig.targetFolderName.value,
                    )
                }

            ElevatedButton(
                enabled = !vm.running,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showWarnDialog = true
                }) {
                Text("执行", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExecuteWarnDialog(onDismissRequest: () -> Unit, onStart: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row {
                Icon(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null
                )
                Text(stringResource(R.string.warn))
            }
        },
        text = {
            Text(
                stringResource(R.string.warn_msg, AppConfig.targetFolderName.value),
                style = MaterialTheme.typography.titleMedium
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
                onStart()
            }) {
                Text(stringResource(R.string.start))
            }
        }
    )
}

@Composable
fun FileItemScreen(
    uri: Uri,
    name: String,
    state: ProcessState,
    size: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        AsyncImage(model = uri, contentDescription = "Image", Modifier.size(48.dp))

        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(text = name, style = MaterialTheme.typography.titleMedium)

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (state) {
                        is ProcessState.DONE -> "完成"
                        is ProcessState.ERROR -> {
                            "错误: ${state.t}"
                        }

                        is ProcessState.PROCESSING -> "处理中"

                        else -> "就绪"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = when (state) {
                        is ProcessState.DONE -> MaterialTheme.colorScheme.primary
                        is ProcessState.ERROR -> MaterialTheme.colorScheme.error
                        is ProcessState.PROCESSING -> MaterialTheme.colorScheme.secondary

                        else -> MaterialTheme.colorScheme.tertiary
                    },
                    fontStyle = FontStyle.Italic,
                )

                Text(
                    text = size,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}