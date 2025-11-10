package GaitVision.com.shared.platform

/**
 * Platform-agnostic file management interface.
 * Implementations are provided in androidMain and iosMain.
 */
expect class FileManager {
    /**
     * Load a file from assets/resources.
     * @param fileName The name of the file to load
     * @return ByteArray containing the file contents, or null if not found
     */
    suspend fun loadAssetFile(fileName: String): ByteArray?
    
    /**
     * Get the path to a file in the app's data directory.
     * @param fileName The name of the file
     * @return Full path to the file
     */
    fun getDataFilePath(fileName: String): String
    
    /**
     * Save data to a file in the app's data directory.
     * @param fileName The name of the file
     * @param data The data to save
     * @return true if successful, false otherwise
     */
    suspend fun saveFile(fileName: String, data: ByteArray): Boolean
    
    /**
     * Read a file from the app's data directory.
     * @param fileName The name of the file
     * @return ByteArray containing the file contents, or null if not found
     */
    suspend fun readFile(fileName: String): ByteArray?
}

