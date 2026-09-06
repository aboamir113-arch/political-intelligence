package com.example.data.local

import androidx.room.*
import com.example.data.model.AiConversation
import com.example.data.model.AiMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface AiConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: AiConversation): Long

    @Update
    suspend fun updateConversation(conversation: AiConversation)

    @Delete
    suspend fun deleteConversation(conversation: AiConversation)

    @Query("SELECT * FROM ai_conversations WHERE userId = :userId AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getConversationsForUser(userId: Long): Flow<List<AiConversation>>

    @Query("SELECT * FROM ai_conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): AiConversation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiMessage): Long

    @Query("SELECT * FROM ai_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<AiMessage>>

    @Query("DELETE FROM ai_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: Long)

    @Query("UPDATE ai_conversations SET updatedAt = :timestamp WHERE id = :conversationId")
    suspend fun touchConversation(conversationId: Long, timestamp: Long)

    @Query("DELETE FROM ai_conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)
}
