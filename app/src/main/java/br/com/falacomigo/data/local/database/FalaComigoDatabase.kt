package br.com.falacomigo.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.falacomigo.data.local.dao.BoardDao
import br.com.falacomigo.data.local.dao.RoutineDao
import br.com.falacomigo.data.local.dao.SymbolDao
import br.com.falacomigo.data.local.entities.BoardEntity
import br.com.falacomigo.data.local.entities.BoardSymbolEntity
import br.com.falacomigo.data.local.entities.RoutineEntity
import br.com.falacomigo.data.local.entities.RoutineBoardEntity
import br.com.falacomigo.data.local.entities.SymbolEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import br.com.falacomigo.data.local.dao.CachedPictogramDao
import br.com.falacomigo.data.local.entities.CachedPictogram

@Database(
    entities = [
        SymbolEntity::class,
        BoardEntity::class,
        BoardSymbolEntity::class,
        RoutineEntity::class,
        RoutineBoardEntity::class,
        CachedPictogram::class
    ],
    version = 31,
    exportSchema = true
)
abstract class FalaComigoDatabase : RoomDatabase() {
    abstract fun symbolDao(): SymbolDao
    abstract fun boardDao(): BoardDao
    abstract fun routineDao(): RoutineDao
    abstract fun cachedPictogramDao(): CachedPictogramDao

    companion object {
        const val DATABASE_NAME = "fala_comigo_db"

        val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE symbols ADD COLUMN localImagePath TEXT")
                database.execSQL("ALTER TABLE symbols ADD COLUMN thumbnailPath TEXT")
                database.execSQL(
                    "ALTER TABLE symbols ADD COLUMN imageDownloadStatus TEXT NOT NULL DEFAULT 'PENDING'"
                )
                database.execSQL("ALTER TABLE symbols ADD COLUMN isEmergency INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun create(context: Context): FalaComigoDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FalaComigoDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_30_31)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            // Seed será populado via repository
                        }
                    }
                })
                .build()
        }
    }
}
