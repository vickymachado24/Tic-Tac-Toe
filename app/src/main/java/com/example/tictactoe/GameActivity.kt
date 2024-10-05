package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.tictactoe.data.PointType

class GameActivity : ComponentActivity() {
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
            ) {
                GameScreen()
            }
        }
    }
}

fun generateEmptyBoard(): List<List<PointType>> {
    return List(3) { List(3) { PointType.Empty } }
}

@Composable
fun GameScreen() {
    var board by remember { mutableStateOf(generateEmptyBoard()) }
    val isAi = false
    var gameOver by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf<PointType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

    }

    if (gameOver) {
        val message : String = if(!isAi) {
            when (winner) {
                PointType.X -> "Player 1 won!"
                PointType.O -> "Player 2 won!"
                else -> "It's a draw!"
            }
        }
        else{
            when (winner) {
                PointType.X -> "Player 1 won!"
                PointType.O -> "Computer won!"
                else -> "It's a draw!"
            }
        }
        AlertDialog(
            onDismissRequest = { gameOver = false },
            title = { Text("Game Over") },
            text = {
                Text(
                    text = message
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        board = generateEmptyBoard()
                        gameOver = false
                    }
                ) {
                    Text("Play Again?")
                }
            }
        )
    }
}
