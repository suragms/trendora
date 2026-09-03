package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedTrendEntity::class,
        SavedArticleEntity::class,
        SearchHistoryEntity::class,
        RecentlyViewedEntity::class,
        NotificationEntity::class,
        NewsCacheEntity::class,
        AiAnalysisCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trendDao(): TrendDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration strategy: the schema is still pre-1.0 and evolving, so we
         * currently use [fallbackToDestructiveMigration] to avoid crashing on an
         * out-of-date cached database. The user data stored here is all
         * re-derivable (bookmarks/news cache/AI cache) and there is no released
         * production version yet.
         *
         * BEFORE the first production (Play Store / GitHub) release, replace this
         * with explicit `RoomDatabase.Callback`/Migration objects so a future
         * schema change does NOT silently wipe user bookmarks. See RELEASE.md.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "trendora_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
