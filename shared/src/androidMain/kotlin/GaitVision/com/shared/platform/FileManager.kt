package GaitVision.com.shared.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Android implementation of FileManager.
 */
actual class FileManager(private val context: Context) {
    
    actual suspend fun loadAssetFile(fileName: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            context.assets.open(fileName).use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun getDataFilePath(fileName: String): String {
        val file = File(context.filesDir, fileName)
        return file.absolutePath
    }
    
    actual suspend fun saveFile(fileName: String, data: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(data)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual suspend fun readFile(fileName: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                file.readBytes()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

