package com.example.tictactoe.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictactoe.data.AppDatabase
import com.example.tictactoe.data.GameHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val db: AppDatabase) : ViewModel() {
    private val _historyData = MutableStateFlow<List<GameHistory>>(emptyList())
    val historyData = _historyData.asStateFlow()

    init {
        refreshHistoryData() // Initial data load
    }

    private fun refreshHistoryData() {
        viewModelScope.launch {
            try {
                db.getLatestGames().collect {
                    _historyData.value = it
                    Log.d("HistoryViewModel", "Fetched ${it.size} records from the database")
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error fetching game history: ${e.message}")
            }
        }
    }

    fun insertGameHistory(gameHistory: GameHistory) {
        viewModelScope.launch {
            try {
                db.insertGameHistory(gameHistory)
                Log.d("HistoryViewModel", "Inserted game history successfully")
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error inserting game history: ${e.message}")
            }
        }
    }
}