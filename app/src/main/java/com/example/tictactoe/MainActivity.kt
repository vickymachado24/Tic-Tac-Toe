package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
<<<<<<< Updated upstream
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
=======
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
>>>>>>> Stashed changes
import com.example.tictactoe.ui.theme.TicTacToeTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeTheme {
<<<<<<< Updated upstream
                CenterAlignedTopAppBar(
                    title = {
                        Text("Tic Tac Toe")
                    }
                )
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                             val intent = Intent(this@MainActivity, GameActivity::class.java)
                             startActivity(intent)
                        },
                        content = {
                            Text("Play vs AI")
                        },
                    )
                    Button(
                        onClick = {
                             val intent = Intent(this@MainActivity, OnlineGameActivity::class.java)
                             startActivity(intent)
                        },
                        content = {
                            Text("Play vs Player")
                        },
                    )
                    Button(
                        onClick = {
                             val intent = Intent(this@MainActivity, HistoryActivity::class.java)
                             startActivity(intent)
                        },
                        content = {
                            Text("Past Games")
                        },
                    )
                    Button(
                        onClick = {
                             val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                             startActivity(intent)
                        },
                        content = {
                            Text("Settings")
                        },
=======
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
>>>>>>> Stashed changes
                    )
                }
            }
        }
    }
<<<<<<< Updated upstream
}
=======
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current // Obtain the current context

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Aligns content to the center
    ) {
        Button(onClick = {
            // Intent to start the com.example.tictactoe.HistoryActivity
            context.startActivity(Intent(context, HistoryActivity::class.java))
        }) {
            Text(text = "History")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    TicTacToeTheme {
        MainScreen()
    }
}
>>>>>>> Stashed changes
