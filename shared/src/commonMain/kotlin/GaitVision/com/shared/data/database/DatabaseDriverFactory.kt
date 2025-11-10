package GaitVision.com.shared.data.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating platform-specific SQLDelight drivers.
 * Implementations are provided in androidMain and iosMain.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

