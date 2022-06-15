package com.example.psusensor.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application): AndroidViewModel(application) {

    val allThemes: LiveData<List<Theme>>
    private val repository: ThemeRepository

    init {
        val themeDao = ThemeDatabase.getDatabase(application).themeDao()
        repository = ThemeRepository(themeDao)
        allThemes = repository.allThemes
    }

    fun addTheme(theme: Theme, ret: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            ret(repository.addTheme(theme))
        }
    }

    fun addThemes(themes: List<Theme>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addThemes(themes)
        }
    }

    fun updateTheme(theme: Theme) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTheme(theme)
        }
    }

    fun deleteTheme(theme: Theme) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTheme(theme)
        }
    }
}