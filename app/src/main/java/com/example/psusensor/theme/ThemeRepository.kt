package com.example.psusensor.theme

import androidx.lifecycle.LiveData

class ThemeRepository(private val themeDao: ThemeDao) {

    val allThemes: LiveData<List<Theme>> = themeDao.readAllThemes()

    suspend fun addTheme(theme: Theme): Long {
        return themeDao.addTheme(theme)
    }

    suspend fun addThemes(themes: List<Theme>) {
        themeDao.addThemes(themes)
    }

    suspend fun updateTheme(theme: Theme) {
        themeDao.updateTheme(theme)
    }

    suspend fun deleteTheme(theme: Theme) {
        themeDao.deleteTheme(theme)
    }
}