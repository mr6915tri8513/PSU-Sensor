package com.example.psusensor.theme

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ThemeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTheme(theme: Theme): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addThemes(themes: List<Theme>)

    @Update
    suspend fun updateTheme(theme: Theme)

    @Delete
    suspend fun deleteTheme(theme: Theme)

    @Query("SELECT * FROM theme_table")
    fun readAllThemes(): LiveData<List<Theme>>

}