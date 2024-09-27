package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.tictactoe.ui.theme.TicTacToeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            // val intent = Intent(this@MainActivity, GameActivity::class.java)
                            // startActivity(intent)
                        },
                        content = {
                            Text("Play vs AI")
                        },
                    )
                    Button(
                        onClick = {
                            // val intent = Intent(this@MainActivity, OnlineGameActivity::class.java)
                            // startActivity(intent)
                        },
                        content = {
                            Text("Play vs Player")
                        },
                    )
                    Button(
                        onClick = {
                            // val intent = Intent(this@MainActivity, HistoryActivity::class.java)
                            // startActivity(intent)
                        },
                        content = {
                            Text("Past Games")
                        },
                    )
                    Button(
                        onClick = {
                            // val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                            // startActivity(intent)
                        },
                        content = {
                            Text("Settings")
                        },
                    )
                }
            }
        }
    }
}