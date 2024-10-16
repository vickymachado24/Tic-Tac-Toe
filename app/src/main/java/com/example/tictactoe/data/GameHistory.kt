package com.example.tictactoe.data

import android.content.ContentValues
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val player1: String,
    val player2:String,
    val difficulty: String,
    val winnerStatus: String,
    val date: Long
)

// 2. Define the Database (No DAO)
@Database(entities = [GameHistory::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: ComponentActivity): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "game_history_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Direct method to insert a game history
    public fun insertGameHistory(gameHistory: GameHistory) {
        try {
            val db = openHelper.writableDatabase
            val contentValues = ContentValues().apply {
                put("player1", gameHistory.player1)
                put("player2", gameHistory.player2)
                put("difficulty", gameHistory.difficulty)
                put("winnerStatus", gameHistory.winnerStatus)
                put("date", gameHistory.date)
            }

            val result = db.insert("game_history", 0, contentValues)
            if (result == -1L) {
                Log.e("AppDatabase", "Failed to insert game history into the database")
            } else {
                Log.d("AppDatabase", "Game history inserted successfully with row ID: $result")
            }
        } catch (e: Exception) {
            Log.e("AppDatabase", "Error inserting game history: ${e.message}")
        }
    }

    // Direct method to fetch latest games
    fun getLatestGames(): Flow<List<GameHistory>> {
        val games = mutableListOf<GameHistory>()
        try {
            val cursor = openHelper.writableDatabase.query(
                "SELECT * FROM game_history ORDER BY date DESC", emptyArray()
            )
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val player1 = cursor.getString(cursor.getColumnIndexOrThrow("player1"))
                    val player2 = cursor.getString(cursor.getColumnIndexOrThrow("player2"))
                    val difficulty = cursor.getString(cursor.getColumnIndexOrThrow("difficulty"))
                    val winnerStatus = cursor.getString(cursor.getColumnIndexOrThrow("winnerStatus"))
                    val date = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                    games.add(GameHistory(id, player1,player2, difficulty, winnerStatus, date))
                } while (cursor.moveToNext())
            }
            cursor.close()
            Log.d("AppDatabase", "Fetched ${games.size} games from the database")
        } catch (e: Exception) {
            Log.e("AppDatabase", "Error fetching game history: ${e.message}")
        }
        return MutableStateFlow(games).asStateFlow()
    }
}
