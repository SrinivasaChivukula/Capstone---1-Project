package GaitVision.com.shared.platform

import platform.Foundation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * iOS implementation of FileManager.
 */
actual class FileManager {
    
    actual suspend fun loadAssetFile(fileName: String): ByteArray? = withContext(Dispatchers.Default) {
        try {
            val bundle = NSBundle.mainBundle
            val path = bundle.pathForResource(fileName, ofType = null)
            if (path != null) {
                val data = NSData.dataWithContentsOfFile(path)
                data?.let {
                    val bytes = ByteArray(it.length.toInt())
                    it.getBytes(bytes, length = it.length)
                    bytes
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun getDataFilePath(fileName: String): String {
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String ?: ""
        return "$documentsPath/$fileName"
    }
    
    actual suspend fun saveFile(fileName: String, data: ByteArray): Boolean = withContext(Dispatchers.Default) {
        try {
            val filePath = getDataFilePath(fileName)
            val nsData = NSData.dataWithBytes(data, length = data.size.toULong())
            nsData?.writeToFile(filePath, atomically = true) == true
        } catch (e: Exception) {
            false
        }
    }
    
    actual suspend fun readFile(fileName: String): ByteArray? = withContext(Dispatchers.Default) {
        try {
            val filePath = getDataFilePath(fileName)
            val data = NSData.dataWithContentsOfFile(filePath)
            data?.let {
                val bytes = ByteArray(it.length.toInt())
                it.getBytes(bytes, length = it.length)
                bytes
            }
        } catch (e: Exception) {
            null
        }
    }
}

