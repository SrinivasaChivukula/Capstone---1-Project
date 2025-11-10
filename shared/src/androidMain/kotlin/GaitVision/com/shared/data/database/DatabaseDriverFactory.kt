package GaitVision.com.shared.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of DatabaseDriverFactory.
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = GaitVision.com.shared.database.GaitVisionDatabase.Schema,
            context = context,
            name = "gaitvision_database.db"
        )
    }
}

