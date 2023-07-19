package com.github.jing332.image_processor.help

import com.funny.data_saver.core.DataSaverPreferences
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.github.jing332.image_processor.app

object AppConfig {
    val dataSaverPref = DataSaverPreferences(app.getSharedPreferences("app", 0))

    val sourceDirectory = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "source_directory",
        initialValue = ""
    )

    val targetFolderName = mutableDataSaverStateOf(
        dataSaverInterface = dataSaverPref,
        key = "target_folder_name",
        initialValue = "outputs"
    )
}