package com.example.data.local

import androidx.room.*
import com.example.data.model.Country
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {
    @Query("SELECT * FROM countries ORDER BY isStrategicFocus DESC, nameAr ASC")
    fun getAllCountries(): Flow<List<Country>>

    @Query("SELECT * FROM countries")
    suspend fun getAllCountriesList(): List<Country>

    @Query("SELECT * FROM countries WHERE code = :code LIMIT 1")
    suspend fun getCountryByCode(code: String): Country?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountry(country: Country)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCountries(countries: List<Country>)
}
