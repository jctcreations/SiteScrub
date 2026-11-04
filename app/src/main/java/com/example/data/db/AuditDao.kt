package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<AuditHistoryEntity>>

    @Query("SELECT * FROM audit_history WHERE rootUrl = :url ORDER BY timestamp DESC")
    fun getHistoryForUrl(url: String): Flow<List<AuditHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(entity: AuditHistoryEntity): Long

    @Query("DELETE FROM audit_history WHERE id = :id")
    suspend fun deleteAuditById(id: Long)

    @Query("DELETE FROM audit_history")
    suspend fun clearAll()
}
