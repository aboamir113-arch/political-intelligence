package com.example.data.local

import androidx.room.*
import com.example.data.model.Topic
import com.example.data.model.TopicCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY isFollowed DESC, nameAr ASC")
    fun getAllTopics(): Flow<List<Topic>>

    @Query("SELECT * FROM topics")
    suspend fun getAllTopicsList(): List<Topic>

    @Query("SELECT * FROM topics WHERE category = :category")
    fun getTopicsByCategory(category: TopicCategory): Flow<List<Topic>>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: Long): Topic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: Topic): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTopics(topics: List<Topic>)

    @Update
    suspend fun updateTopic(topic: Topic)

    @Delete
    suspend fun deleteTopic(topic: Topic)
}
