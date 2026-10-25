package com.sonicplayer.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds named queues while keeping the existing single queue as "Antrean 1".
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `queues` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `active` INTEGER NOT NULL,
                `lastPosition` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )

        database.execSQL(
            """
            INSERT OR IGNORE INTO `queues` (`id`, `name`, `active`, `lastPosition`, `createdAt`, `updatedAt`)
            SELECT 1, 'Antrean 1', 1,
                COALESCE((SELECT `position` FROM `queue` WHERE `isCurrent` = 1 LIMIT 1), 0),
                CAST(strftime('%s', 'now') AS INTEGER) * 1000,
                CAST(strftime('%s', 'now') AS INTEGER) * 1000
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `queue_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `queueId` INTEGER NOT NULL,
                `songId` INTEGER NOT NULL,
                `position` INTEGER NOT NULL,
                FOREIGN KEY(`queueId`) REFERENCES `queues`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )

        database.execSQL(
            "INSERT INTO `queue_new` (`id`, `queueId`, `songId`, `position`) " +
                "SELECT `id`, 1, `songId`, `position` FROM `queue`"
        )

        database.execSQL("DROP TABLE `queue`")
        database.execSQL("ALTER TABLE `queue_new` RENAME TO `queue`")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_queue_queueId` ON `queue` (`queueId`)")
    }
}
