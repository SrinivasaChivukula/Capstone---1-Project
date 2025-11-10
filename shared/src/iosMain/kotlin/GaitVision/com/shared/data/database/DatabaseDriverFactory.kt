package GaitVision.com.shared.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS implementation of DatabaseDriverFactory.
 */
actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = GaitVision.com.shared.database.GaitVisionDatabase.Schema,
            name = "gaitvision_database.db"
        )
    }
}

