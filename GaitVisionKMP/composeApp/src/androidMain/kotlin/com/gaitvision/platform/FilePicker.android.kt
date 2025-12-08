package com.gaitvision.platform

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

actual class FilePicker actual constructor(
    private val onFilePicked: (String?) -> Unit
) {
    private var launcher: androidx.activity.result.ActivityResultLauncher<String>? = null

    @Composable
    actual fun register() {
        launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            onFilePicked(uri?.toString())
        }
    }

    actual fun launch() {
        launcher?.launch("video/*")
    }
}
