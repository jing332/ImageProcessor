package com.github.jing332.image_processor.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.jing332.image_processor.utils.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProcessorViewModel : ViewModel() {
    val files = mutableStateListOf<FileModel>()

    fun loadDir(context: Context, dir: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { files.clear() }

            val tree = DocumentFile.fromTreeUri(context, dir)
            tree!!.listFiles().forEach {
                DocumentFile.fromSingleUri(context, it.uri)!!.let { doc ->
                    if (doc.type?.contains("image") == true) {
                        files.add(
                            FileModel(
                                uri = doc.uri,
                                uriString = doc.uri.toString(),
                                name = doc.name ?: "",
                                processState = ProcessState.IDLE,
                                size = StringUtils.formatFileSize(doc.length()),
                            )
                        )
                    }
                }
            }
        }
    }

    fun executeConv(
        context: Context,
        srcUri: Uri,
        folderName: String = "outputs",
        format: CompressFormat,
        quality: Int,
        width: Int = 0,
        height: Int = 0,
    ) {
        files.toList().forEachIndexed { index, fileModel ->
            files[index] = fileModel.copy(processState = ProcessState.IDLE)
        }

        viewModelScope.launch(Dispatchers.IO) {
            files.toList().forEachIndexed { index, fileModel ->
                files[index] = fileModel.copy(processState = ProcessState.PROCESSING)

                runCatching {
                    val srcDir = DocumentFile.fromTreeUri(context, srcUri)!!
                    val target =
                        srcDir.listFiles().find { it.name == folderName } ?: srcDir.createDirectory(
                            folderName
                        )

                    context.contentResolver.openInputStream(fileModel.uriString.toUri())?.use {
                        val extName = format.name.lowercase().replace(Regex("_.*"), "")
                        val newFileName =
                            fileModel.name.substringBeforeLast(".") + ".${extName}"

                        target!!.findFile(newFileName)?.delete()
                        target.createFile("image/${extName}", newFileName)?.let { file ->
                            file.uri.let { uri ->
                                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    var bitmap = BitmapFactory.decodeStream(it)
                                    if (width > 0 && height > 0)
                                        bitmap = resizeBitmap(bitmap, width, height)

                                    bitmap.compress(format, quality, outputStream)
                                }
                            }
                        }
                    }
                }.onFailure {
                    it.printStackTrace()
                    files[index] = fileModel.copy(processState = ProcessState.ERROR)
                }.onSuccess {
                    files[index] = fileModel.copy(processState = ProcessState.DONE)
                }
            }
        }
    }

    private fun resizeBitmap(bm: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap? {
        val srcWidth = bm.width
        val srcHeight = bm.height
        val widthScale = targetWidth * 1f / srcWidth
        val heightScale = targetHeight * 1f / srcHeight
        val matrix = Matrix()
        matrix.postScale(widthScale, heightScale, 0f, 0f)
        // 如需要可自行设置 Bitmap.Config.RGB_8888 等等
        val bmpRet = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.RGB_565)
        val canvas = Canvas(bmpRet)
        val paint = Paint()
        canvas.drawBitmap(bm, matrix, paint)
        return bmpRet
    }

    data class FileModel(
        val name: String,
        val uri: Uri,
        val uriString: String,
        val processState: Int,
        val size: String = "0 KB",
    )

}

object ProcessState {
    const val IDLE = 0
    const val PROCESSING = 1
    const val DONE = 2
    const val ERROR = 3
}