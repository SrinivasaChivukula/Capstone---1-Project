package com.gaitvision.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.UIApplication
import platform.Foundation.NSURL
import platform.darwin.NSObject
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.UniformTypeIdentifiers.UTTypeVideo
import platform.UniformTypeIdentifiers.UTTypeMPEG4

actual class FilePicker actual constructor(
    private val onFilePicked: (String?) -> Unit
) {
    private val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            onFilePicked(url?.path)
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            onFilePicked(null)
        }
    }

    @Composable
    actual fun register() {
        // No-op for iOS as we don't need to register a launcher like Android
    }

    actual fun launch() {
        val types = listOf(UTTypeMovie, UTTypeVideo, UTTypeMPEG4)
        val picker = UIDocumentPickerViewController(forOpeningContentTypes = types, asCopy = true)
        picker.delegate = delegate
        
        val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootController?.presentViewController(picker, animated = true, completion = null)
    }
}
