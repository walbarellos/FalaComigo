package br.com.falacomigo.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symbols")
data class SymbolEntity(
    @PrimaryKey
    val id: String,
    val arasaacId: Int? = null,
    val labelPt: String,
    val spokenText: String,
    val imagePath: String? = null,
    val imageUrl: String? = null,
    val localImagePath: String? = null,
    val thumbnailPath: String? = null,
    val imageDownloadStatus: String = "PENDING", // PENDING, READY, FAILED
    val category: String = "general",
    val isCustom: Boolean = false,
    val isEmergency: Boolean = false,
    val cachedAt: Long? = null,
    val lastUsedAt: Long = 0L // Rastro de uso para o filtro Recentes
)

@Entity(tableName = "boards")
data class BoardEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String = "",
    val routineId: String? = null,
    val gridCols: Int = 3,
    val gridRows: Int = 4,
    val isEmergency: Boolean = false,
    val isDefault: Boolean = false,
    val isSeed: Boolean = false,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "board_symbols", primaryKeys = ["boardId", "symbolId"])
data class BoardSymbolEntity(
    val boardId: String,
    val symbolId: String,
    val position: Int,
    val customLabel: String? = null,
    val customSpokenText: String? = null,
    val backgroundColor: String? = null
)

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val icon: String = "history",
    val spokenText: String = "", // CAMPO QUE FALTAVA
    val symbolsJson: String = "[]",
    val displayOrder: Int = 0,
    val isAuto: Boolean = false
)

@Entity(tableName = "routine_boards", primaryKeys = ["routineId", "boardId"])
data class RoutineBoardEntity(
    val routineId: String,
    val boardId: String,
    val displayOrder: Int = 0
)

@Entity(tableName = "symbol_usage")
data class SymbolUsageEntity(
    @PrimaryKey
    val symbolId: String,
    val touchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_pictograms")
data class CachedPictogram(
    @PrimaryKey val id: Int,
    val label: String,
    val imageUrl: String,
    val category: String = "custom",
    val savedAt: Long = System.currentTimeMillis(),
)