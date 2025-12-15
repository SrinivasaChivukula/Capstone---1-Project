package com.gaitvision.platform

import androidx.compose.runtime.Composable

expect class FilePicker(onFilePicked: (String?) -> Unit) {
    @Composable
    fun register()
    fun launch()
}
