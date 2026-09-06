package com.example.data.local

import androidx.room.*
import com.example.data.model.Person
import com.example.data.model.PersonPosition
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons ORDER BY isMonitored DESC, nameAr ASC")
    fun getAllPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons")
    suspend fun getAllPersonsList(): List<Person>

    @Query("SELECT * FROM persons WHERE isMonitored = 1 ORDER BY stanceShiftCount DESC, nameAr ASC")
    fun getMonitoredPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE countryCode = :countryCode")
    fun getPersonsByCountry(countryCode: String): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE id = :id LIMIT 1")
    suspend fun getPersonById(id: Long): Person?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPersons(persons: List<Person>)

    @Update
    suspend fun updatePerson(person: Person)

    @Delete
    suspend fun deletePerson(person: Person)

    // Positions & Stance History
    @Query("SELECT * FROM person_positions WHERE personId = :personId ORDER BY changeDateTimestamp DESC")
    fun getPositionsForPerson(personId: Long): Flow<List<PersonPosition>>

    @Query("SELECT * FROM person_positions ORDER BY changeDateTimestamp DESC LIMIT 20")
    fun getRecentStanceShifts(): Flow<List<PersonPosition>>

    @Query("SELECT * FROM person_positions ORDER BY changeDateTimestamp DESC")
    suspend fun getAllPositionsList(): List<PersonPosition>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: PersonPosition): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPositions(positions: List<PersonPosition>)
}
