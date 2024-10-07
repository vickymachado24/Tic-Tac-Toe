package com.example.tictactoe

<<<<<<< Updated upstream
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class HistoryActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CenterAlignedTopAppBar(
                title = {
                    Text("Tic Tac Toe")
                },
                navigationIcon = {
                    IconButton(onClick = { finish()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {}
        }
    }
}
=======
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// 1. Define the Entity
@Entity(tableName = "game_history")
data class GameHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameLevel: String,
    val winner: String,
    val date: Long
)

// 2. Define the Database (No DAO)
@Database(entities = [GameHistory::class], version = 1, exportSchema = false)
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
    suspend fun insertGameHistory(gameHistory: GameHistory) {
        try {
            val db = openHelper.writableDatabase
            val contentValues = ContentValues().apply {
                put("gameLevel", gameHistory.gameLevel)
                put("winner", gameHistory.winner)
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
                "SELECT * FROM game_history ORDER BY date DESC LIMIT 10", null
            )
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                    val gameLevel = cursor.getString(cursor.getColumnIndexOrThrow("gameLevel"))
                    val winner = cursor.getString(cursor.getColumnIndexOrThrow("winner"))
                    val date = cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                    games.add(GameHistory(id, gameLevel, winner, date))
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

// 3. Define the ViewModel
class HistoryViewModel(private val db: AppDatabase) : ViewModel() {
    private val _historyData = MutableStateFlow<List<GameHistory>>(emptyList())
    val historyData = _historyData.asStateFlow()

    init {
        refreshHistoryData() // Initial data load
    }

    fun refreshHistoryData() {
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


// ViewModel factory to pass the database instance
class HistoryViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// 4. Define the Activity
class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val db = AppDatabase.getDatabase(this)
            val viewModel: HistoryViewModel = ViewModelProvider(this, HistoryViewModelFactory(db)).get(HistoryViewModel::class.java)
            HistoryScreen(viewModel)
        }
    }
}
//Uncomment below code if you want to see dummy data display (UI check)
//class HistoryActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val db = AppDatabase.getDatabase(this)
//        val viewModel: HistoryViewModel = ViewModelProvider(this, HistoryViewModelFactory(db)).get(HistoryViewModel::class.java)
//
//        // Insert dummy data into the database
//        lifecycleScope.launch {
//            val dataCount = viewModel.historyData.value.size
//            if (dataCount == 0) {
//                viewModel.insertGameHistory(GameHistory(gameLevel = "Easy", winner = "Player 1", date = System.currentTimeMillis()))
//                viewModel.insertGameHistory(GameHistory(gameLevel = "Medium", winner = "Computer", date = System.currentTimeMillis()))
//                viewModel.insertGameHistory(GameHistory(gameLevel = "Hard", winner = "Player 1", date = System.currentTimeMillis()))
//                // Refresh history data after insertion
//                viewModel.refreshHistoryData()
//            }
//        }
//
//        setContent {
//            HistoryScreen(viewModel)
//        }
//    }
//}

// 5. Define the UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val historyData by viewModel.historyData.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Game History") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // Add padding on either side to center the table
        ) {
            // Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) // Light green background
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Serial No.",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Game Level",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Winner",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Date",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Divider between header and content
                    Divider(thickness = 1.dp)

                    // Table Content
                    LazyColumn {
                        if (historyData.isEmpty()) {
                            // Display a single row with "No data available" message
                            item {
                                NoDataRow()
                            }
                        } else {
                            itemsIndexed(historyData) { index, gameHistory ->
                                GameHistoryItem(index + 1, gameHistory) // Pass serial number
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoDataRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant) // Grey background
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No data available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun GameHistoryItem(serialNo: Int, gameHistory: GameHistory) {
    // Format the date using SimpleDateFormat
    val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(gameHistory.date))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant) // Grey background
            .padding(8.dp)
    ) {
        Text(
            text = serialNo.toString(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = gameHistory.gameLevel,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = gameHistory.winner,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = formattedDate, // Use formatted date string here
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
>>>>>>> Stashed changes
