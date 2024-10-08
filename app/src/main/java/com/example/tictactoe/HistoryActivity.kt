package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import com.example.tictactoe.data.AppDatabase
import com.example.tictactoe.data.GameHistory
import com.example.tictactoe.ui.theme.TicTacToeTheme

import java.text.SimpleDateFormat
import java.util.*


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
            TicTacToeTheme {
                val db = AppDatabase.getDatabase(this)
                val viewModel: HistoryViewModel = ViewModelProvider(
                    this,
                    HistoryViewModelFactory(db)
                ).get(HistoryViewModel::class.java)
                HistoryScreen(viewModel, onBackPress = {
                    finish()
                })
            }
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
fun HistoryScreen(viewModel: HistoryViewModel, onBackPress: () -> Unit) {
    val historyData by viewModel.historyData.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Tic Tac Toe")
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )},
    content = { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            // Add padding on either side to center the table
        ) {
            // Table
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)) {
                Column(verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    // Table Header
                    Text(
                        text = "Game History",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) // Light green background
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Player 1",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "Player 2",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "Level",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "Status",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
    })
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
            text = gameHistory.player1,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = gameHistory.player2,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        if(gameHistory.difficulty != ""){
            Text(
                text = gameHistory.difficulty,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = gameHistory.winnerStatus, // Use formatted date string here
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
