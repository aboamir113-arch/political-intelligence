package com.example.data.local

import androidx.room.*
import com.example.data.model.Statement
import kotlinx.coroutines.flow.Flow

@Dao
interface StatementDao {
    @Query("SELECT * FROM statements ORDER BY statementDate DESC")
    fun getAllStatements(): Flow<List<Statement>>

    @Query("SELECT * FROM statements ORDER BY statementDate DESC")
    suspend fun getAllStatementsList(): List<Statement>

    @Query("SELECT * FROM statements WHERE eventId = :eventId ORDER BY statementDate DESC")
    fun getStatementsForEvent(eventId: Long): Flow<List<Statement>>

    @Query("SELECT * FROM statements WHERE personId = :personId OR personName LIKE '%' || :personName || '%' ORDER BY statementDate DESC")
    fun getStatementsForPerson(personId: Long?, personName: String): Flow<List<Statement>>

    @Query("SELECT * FROM statements WHERE articleId = :articleId ORDER BY statementDate DESC")
    fun getStatementsForArticle(articleId: Long): Flow<List<Statement>>

    @Query("SELECT * FROM statements WHERE id = :id")
    suspend fun getStatementById(id: Long): Statement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatement(statement: Statement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatements(statements: List<Statement>): List<Long>

    @Update
    suspend fun updateStatement(statement: Statement)

    @Delete
    suspend fun deleteStatement(statement: Statement)

    @Query("SELECT COUNT(*) FROM statements")
    suspend fun getStatementCount(): Int
}
