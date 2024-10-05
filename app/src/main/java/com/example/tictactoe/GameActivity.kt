package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictactoe.data.PointType
import com.example.tictactoe.enums.DifficultyLevel

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

    var currentPlayer by remember { mutableStateOf(PointType.X) }
    val difficulty = DifficultyLevel.EASY

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        PlayerVsDisplay(isAi, currentPlayer, difficulty)

        Spacer(modifier = Modifier.height(100.dp))

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

fun checkWinner(board: List<List<PointType>>): PointType? {
    val lines = listOf(
        // Rows
        listOf(board[0][0], board[0][1], board[0][2]),
        listOf(board[1][0], board[1][1], board[1][2]),
        listOf(board[2][0], board[2][1], board[2][2]),
        // Columns
        listOf(board[0][0], board[1][0], board[2][0]),
        listOf(board[0][1], board[1][1], board[2][1]),
        listOf(board[0][2], board[1][2], board[2][2]),
        // Diagonals
        listOf(board[0][0], board[1][1], board[2][2]),
        listOf(board[0][2], board[1][1], board[2][0])
    )
    return lines.firstOrNull { it[0] != PointType.Empty && it[0] == it[1] && it[1] == it[2] }?.first()
}


@Composable
fun PlayerVsDisplay(isAI: Boolean, currentPlayer: PointType, difficulty: DifficultyLevel) {
    val player1Color = if (currentPlayer == PointType.X) Color.Red else Color.Gray
    val player2Color = if (currentPlayer == PointType.O) Color.Green else Color.Blue

//    val backgroundColor = if (!isAI) Color.Gray else when (difficulty) {
//        DifficultyLevel.EASY -> Color.Green
//        DifficultyLevel.MEDIUM -> Color.Cyan
//        DifficultyLevel.HARD -> Color.Red
//    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            //.background(backgroundColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Player 1
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Player 1", color = Color.Black)
            Image(
                painter = painterResource(id = R.drawable.ic_tic_tac_toe_x),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .background(player1Color, shape = CircleShape)
                    .padding(16.dp)
            )
        }

        Text(
            text = "VS",
            color = Color.Black,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = if (isAI) "Computer" else "Player 2", color = Color.Black)
            Image(
                painter = painterResource(id = R.drawable.ic_tic_tac_toe_o),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .background(player2Color, shape = CircleShape)
                    .padding(16.dp)
            )
        }
    }
}