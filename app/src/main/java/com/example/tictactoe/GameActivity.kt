package com.example.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModelProvider
import com.example.tictactoe.data.AppDatabase
import com.example.tictactoe.data.GameHistory
import com.example.tictactoe.data.PointType
import com.example.tictactoe.enums.DifficultyLevel
import com.example.tictactoe.ui.theme.TicTacToeTheme

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val difficulty: DifficultyLevel = intent.getSerializableExtra("MODE") as DifficultyLevel

        setContent {
            var difficultyState by remember {mutableStateOf(difficulty)}
            val db = AppDatabase.getDatabase(this)
            val viewModel: HistoryViewModel = ViewModelProvider(
                this,
                HistoryViewModelFactory(db)
            ).get(HistoryViewModel::class.java)
            TicTacToeTheme {
                //Adding Scaffold to implement settings icon for difficulty change and back button
                Scaffold(
                    topBar = {
                        TopAppBarWithSettingsIcon(
                            currentDifficulty = difficultyState,
                            onDifficultyChange = { newDifficulty ->
                                difficultyState = newDifficulty
                            },
                            onBackPress = {finish()}
                        )
                    },
                    content = { innerPadding ->
                        GameScreen(viewModel, difficultyState, onBackPress = {
                            finish()
                        })
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                        }
                    }
                )
            }
            }
        }
}

fun generateEmptyBoard(): List<List<PointType>> {
    return List(3) { List(3) { PointType.Empty } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: HistoryViewModel, difficulty: DifficultyLevel, onBackPress: () -> Unit) {
    var board by remember { mutableStateOf(generateEmptyBoard()) }
    var gameOver by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf<PointType?>(null) }
    var currentPlayer by remember { mutableStateOf(PointType.X) }
    var firstMove by remember { mutableStateOf(true) }

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
            )
        }, content = { innerPadding -> Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlayerVsDisplay(currentPlayer, difficulty)

            Spacer(modifier = Modifier.height(100.dp))

            TicTacToeBoard(
                board = board,
                modifier = Modifier.size(300.dp),
                onClick = { row, col ->
                    if (board[row][col] == PointType.Empty && !gameOver) {

                        board = board.mapIndexed { r, rowList ->
                            rowList.mapIndexed { c, pointType ->
                                if (r == row && c == col) currentPlayer else pointType
                            }
                        }

                        winner = checkWinner(board)
                        gameOver = winner != null
                        currentPlayer = if (currentPlayer == PointType.X) PointType.O else PointType.X
                        firstMove = false
                    }
                }
            )
        }
        }
    )


    if (currentPlayer == PointType.O && !gameOver && !firstMove) {
        val aiMove = aiChooseMove(board, currentPlayer, difficulty)

        if (aiMove != null) {
            board = board.mapIndexed { r, rowList ->
                rowList.mapIndexed { c, pointType ->
                    if (r == aiMove.first && c == aiMove.second) currentPlayer else pointType
                }
            }

            winner = checkWinner(board)
            gameOver = winner != null
            currentPlayer = PointType.X
        }
    }
    if (gameOver) {
        val message : String = when (winner) {
            PointType.X -> "Player won!"
            PointType.O -> "Computer won!"
            else -> "It's a draw!"
        }
        val currentTime = System.currentTimeMillis()

        val gameHistory = GameHistory(
            player1 = "Player",
            player2 = "Computer",
            difficulty = difficulty.name,
            winnerStatus = message,
            date = currentTime
        )
        viewModel.insertGameHistory(gameHistory)

        AlertDialog(
            onDismissRequest = { gameOver = false },
            title = { Text("Game Over") },
            text = { Text(text = message) },
            confirmButton = {
                Button(onClick = {
                    board = generateEmptyBoard()
                    gameOver = false
                    currentPlayer = PointType.X
                    firstMove = true
                }) {
                    Text("Play Again?")
                }
            }
        )
    }
}

fun aiChooseMove(board: List<List<PointType>>, currentPlayer: PointType, difficulty: DifficultyLevel): Pair<Int, Int>? {
    return when (difficulty) {
        DifficultyLevel.EASY -> {
            val emptySpots = mutableListOf<Pair<Int, Int>>()
            for (row in board.indices) {
                for (col in board[row].indices) {
                    if (board[row][col] == PointType.Empty) {
                        emptySpots.add(Pair(row, col))
                    }
                }
            }
            if (emptySpots.isNotEmpty()) {
                return emptySpots.random()
            }

            null
        }
        DifficultyLevel.MEDIUM -> {
            val useOptimalMove = (0..1).random() == 1

            return if (useOptimalMove) {
                var bestScore = Int.MIN_VALUE
                var bestMove: Pair<Int, Int>? = null

                for (row in board.indices) {
                    for (col in board[row].indices) {
                        if (board[row][col] == PointType.Empty) {
                            val newBoard = board.mapIndexed { r, rowList ->
                                rowList.mapIndexed { c, pointType ->
                                    if (r == row && c == col) PointType.O else pointType
                                }
                            }
                            val score = minimax(newBoard, 0, false, Int.MIN_VALUE, Int.MAX_VALUE)
                            if (score > bestScore) {
                                bestScore = score
                                bestMove = row to col
                            }
                        }
                    }
                }
                bestMove
            } else {
                val emptySpots = mutableListOf<Pair<Int, Int>>()
                for (row in board.indices) {
                    for (col in board[row].indices) {
                        if (board[row][col] == PointType.Empty) {
                            emptySpots.add(Pair(row, col))
                        }
                    }
                }
                if (emptySpots.isNotEmpty()) {
                    emptySpots.random()
                } else {
                    null
                }
            }
        }
        DifficultyLevel.HARD -> {
            var bestScore = Int.MIN_VALUE
            var bestMove: Pair<Int, Int>? = null

            for (row in board.indices) {
                for (col in board[row].indices) {
                    if (board[row][col] == PointType.Empty) {
                        val newBoard = board.mapIndexed { r, rowList ->
                            rowList.mapIndexed { c, pointType ->
                                if (r == row && c == col) PointType.O else pointType
                            }
                        }
                        val score = minimax(newBoard, 0, false, Int.MIN_VALUE, Int.MAX_VALUE)
                        if (score > bestScore) {
                            bestScore = score
                            bestMove = row to col
                        }
                    }
                }
            }
            bestMove
        }
    }
}

fun minimax(board: List<List<PointType>>, depth: Int, isMaximizing: Boolean, alpha: Int, beta: Int): Int {
    val winner = checkWinner(board)
    if (winner != null) {
        return when (winner) {
            PointType.O -> 10 - depth
            PointType.X -> depth - 10
            PointType.Empty -> 0
        }
    }

    if (board.flatten().none { it == PointType.Empty }) {
        return 0
    }

    return if (isMaximizing) {
        var maxEval = Int.MIN_VALUE
        var currentAlpha = alpha
        for (row in board.indices) {
            for (col in board[row].indices) {
                if (board[row][col] == PointType.Empty) {
                    val newBoard = board.mapIndexed { r, rowList ->
                        rowList.mapIndexed { c, pointType ->
                            if (r == row && c == col) PointType.O else pointType
                        }
                    }
                    val eval = minimax(newBoard, depth + 1, false, currentAlpha, beta)
                    maxEval = maxOf(maxEval, eval)
                    currentAlpha = maxOf(currentAlpha, eval)

                    if (beta <= currentAlpha) {
                        break
                    }
                }
            }
        }
        maxEval
    } else {
        var minEval = Int.MAX_VALUE
        var currentBeta = beta
        for (row in board.indices) {
            for (col in board[row].indices) {
                if (board[row][col] == PointType.Empty) {
                    val newBoard = board.mapIndexed { r, rowList ->
                        rowList.mapIndexed { c, pointType ->
                            if (r == row && c == col) PointType.X else pointType
                        }
                    }
                    val eval = minimax(newBoard, depth + 1, true, alpha, currentBeta)
                    minEval = minOf(minEval, eval)
                    currentBeta = minOf(currentBeta, eval)

                    if (currentBeta <= alpha) {
                        break
                    }
                }
            }
        }
        minEval
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

    val winner =  lines.firstOrNull { it[0] != PointType.Empty && it[0] == it[1] && it[1] == it[2] }?.first()
    if (winner != null) return winner

    if (board.flatten().none { it == PointType.Empty }) {
        return PointType.Empty
    }

    return null
}


@Composable
fun PlayerVsDisplay(currentPlayer: PointType, difficulty: DifficultyLevel) {
    val player1Color = if (currentPlayer == PointType.X) Color.Red else Color.Gray
    val player2Color = if (currentPlayer == PointType.O) Color.Green else Color.Blue

    Row(
        modifier = Modifier
            .fillMaxWidth()
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
            Text(text = "Computer", color = Color.Black)
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

@Composable
fun TicTacToeBoard(
    board: List<List<PointType>>,
    modifier: Modifier = Modifier,
    onClick: (row: Int, col: Int) -> Unit
) {
    val dividerWidth = remember { 8.dp }

    BoxWithConstraints(modifier = modifier) {
        val tileSize = remember(maxWidth, dividerWidth) {
            (maxWidth / 3) - dividerWidth / 1.5f
        }

        for ((row, pointTypeRow) in board.withIndex()) {
            for ((col, pointType) in pointTypeRow.withIndex()) {
                val endPadding = remember { if (col != 0) dividerWidth else 0.dp }
                val bottomPadding = remember { if (row != 0) dividerWidth else 0.dp }

                Box(
                    modifier = Modifier
                        .zIndex(1f)
                        .offset(
                            x = (tileSize * col) + (endPadding * col),
                            y = (tileSize * row) + (bottomPadding * row)
                        )
                        .size(tileSize)
                        .clickable {
                            if (pointType == PointType.Empty) {
                                onClick(row, col)
                            }
                        }
                ) {
                    AnimatedVisibility(
                        visible = pointType != PointType.Empty,
                        enter = scaleIn(animationSpec = tween(200)),
                        exit = scaleOut(animationSpec = tween(200)),
                        modifier = Modifier
                            .padding(8.dp)
                            .matchParentSize()
                    ) {
                        PointTypeImage(pointType = pointType)
                    }
                }
            }
        }

        // Draw the grid dividers
        for (i in 0 until 2) {
            val padding = remember { if (i != 0) dividerWidth else 0.dp }
            BoardDivider(
                maxHeight = maxHeight,
                dividerThickness = { dividerWidth },
                offsetVert = {
                    IntOffset(
                        x = (tileSize * (i + 1) + padding).toPx().toInt(),
                        y = 0.dp.toPx().toInt()
                    )
                },
                offsetHorz = {
                    IntOffset(
                        x = 0.dp.toPx().toInt(),
                        y = (tileSize * (i + 1) + padding).toPx().toInt()
                    )
                }
            )
        }
    }
}

@Composable
private fun BoardDivider(
    maxHeight: Dp,
    dividerThickness: () -> Dp,
    offsetHorz: Density.() -> IntOffset,
    offsetVert: Density.() -> IntOffset
) {
    VerticalDivider(modifier = Modifier
        .offset { offsetVert() }
        .size(dividerThickness(), maxHeight)
        .clip(CircleShape),
        thickness = dividerThickness(), color = Color.White)

    HorizontalDivider(modifier = Modifier
        .offset { offsetHorz() }
        .fillMaxWidth()
        .height(dividerThickness())
        .clip(CircleShape),
        thickness = dividerThickness(), color = Color.White)
}

@Composable
private fun BoxScope.PointTypeImage(
    pointType: PointType
) {
    Image(
        painter = painterResource(
            id = when (pointType) {
                PointType.Empty -> R.drawable.transparent
                PointType.X -> R.drawable.ic_tic_tac_toe_x
                PointType.O -> R.drawable.ic_tic_tac_toe_o
            }
        ),
        contentDescription = null,
        modifier = Modifier.matchParentSize()
    )
}

//Logic for changing difficulty on the go
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithSettingsIcon(
    currentDifficulty: DifficultyLevel,
    onDifficultyChange: (DifficultyLevel) -> Unit,
    onBackPress: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }  // State to track dropdown visibility

    CenterAlignedTopAppBar(
        title = {
            Text("Tic Tac Toe")
        },
        navigationIcon ={
            IconButton(onClick = onBackPress) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            // Settings icon
            IconButton(onClick = { isMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }

            // Dropdown menu to select difficulty level
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Easy") },
                    onClick = {
                        onDifficultyChange(DifficultyLevel.EASY)  // Update difficulty
                        isMenuExpanded = false  // Close menu
                    }
                )
                DropdownMenuItem(
                    text = { Text("Medium") },
                    onClick = {
                        onDifficultyChange(DifficultyLevel.MEDIUM)  // Update difficulty
                        isMenuExpanded = false  // Close menu
                    }
                )
                DropdownMenuItem(
                    text = { Text("Hard") },
                    onClick = {
                        onDifficultyChange(DifficultyLevel.HARD)  // Update difficulty
                        isMenuExpanded = false  // Close menu
                    }
                )
            }
        }
    )
}
