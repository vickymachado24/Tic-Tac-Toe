package com.example.tictactoe

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tictactoe.data.PointType
import com.example.tictactoe.ui.theme.TicTacToeTheme
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")


class BluetoothTicTacToeActivity : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the BluetoothDevice passed from the previous screen
        val bluetoothDevice: BluetoothDevice? = intent.getParcelableExtra("bluetoothDevice")
        val isServer = intent.getBooleanExtra("isServer", true)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Check if the BluetoothDevice is null
        if (bluetoothDevice != null) {
            cancelDiscovery() // Cancel discovery before connecting

            // Re-establish the connection with the BluetoothDevice using fallback method
            bluetoothSocket = createBluetoothSocket(bluetoothDevice)
            try {
                bluetoothSocket?.connect()
                Log.d("BluetoothTicTacToe", "Connection successful")
            } catch (e: IOException) {
                Log.e("BluetoothTicTacToe", "Connection failed", e)
                Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show()
                finish()
            }

            // Initialize input and output streams safely
            bluetoothSocket?.let { socket ->
                inputStream = socket.inputStream
                outputStream = socket.outputStream
            }
        } else {
            // Handle the error where the BluetoothDevice is null
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show()
            finish()
        }

        setContent {
            TicTacToeTheme {
                GameScreen(onBackPress = { finish() }, inputStream, outputStream, isServer)
            }
        }
    }

    // Cancel Bluetooth discovery to speed up connection
    @SuppressLint("MissingPermission")
    private fun cancelDiscovery() {
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
    }

    // Fallback method for creating a Bluetooth socket
    @SuppressLint("MissingPermission")
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket? {
        return try {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        } catch (e: IOException) {
            e.printStackTrace()
            // Fallback to reflection-based RFCOMM socket creation
            try {
                val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                method.invoke(device, 1) as BluetoothSocket
            } catch (e2: Exception) {
                e2.printStackTrace()
                null
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onBackPress: () -> Unit,
    inputStream: InputStream?,
    outputStream: OutputStream?,
    isPlayerX: Boolean // This decides if the local player is 'X' or 'O'
) {
    var board by remember { mutableStateOf(generateEmptyBoard()) }
    var gameOver by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf<PointType?>(null) }
    var currentPlayer by remember { mutableStateOf(PointType.X) }
    var isPlayerTurn by remember { mutableStateOf(isPlayerX) } // Determines if it's the local player's turn

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tic Tac Toe") },
                navigationIcon = {
                    IconButton(onClick = { onBackPress() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayerVsDisplay(currentPlayer)

                Spacer(modifier = Modifier.height(100.dp))

                TicTacToeBoard(
                    board = board,
                    modifier = Modifier.size(300.dp),
                    onClick = { row, col ->
                        // Only allow local player to make a move if it's their turn
                        if (board[row][col] == PointType.Empty && !gameOver && isPlayerTurn) {
                            // Update the board with the local player's move
                            board = board.mapIndexed { r, rowList ->
                                rowList.mapIndexed { c, pointType ->
                                    if (r == row && c == col) currentPlayer else pointType
                                }
                            }

                            // Send the updated board to the opponent
                            sendMoveToOpponent(board, outputStream)

                            // Check if there's a winner
                            winner = checkWinner(board)
                            gameOver = winner != null || board.flatten().none { it == PointType.Empty }

                            // Switch to opponent's turn
                            currentPlayer = if (currentPlayer == PointType.X) PointType.O else PointType.X
                            isPlayerTurn = false
                        }
                    }
                )
            }
        }
    )

    // Listen for incoming move from opponent
    LaunchedEffect(Unit) {
        listenForOpponentMove(inputStream) { updatedBoard ->
            board = updatedBoard
            currentPlayer = if (currentPlayer == PointType.X) PointType.O else PointType.X
            isPlayerTurn = true // Enable the local player's turn after receiving opponent's move
        }
    }

    // Show game over dialog if the game is finished
    if (gameOver) {
        AlertDialog(
            onDismissRequest = { gameOver = false },
            title = { Text("Game Over") },
            text = { Text(text = "Winner: $winner") },
            confirmButton = {
                Button(onClick = {
                    board = generateEmptyBoard()
                    gameOver = false
                    currentPlayer = PointType.X
                    isPlayerTurn = isPlayerX // Reset to initial turn
                }) {
                    Text("Play Again?")
                }
            }
        )
    }
}



@Composable
fun PlayerVsDisplay(currentPlayer: PointType) {
    val player1Color = if (currentPlayer == PointType.X) Color.Red else Color.Gray
    val player2Color = if (currentPlayer == PointType.O) Color.Green else Color.Blue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Player 2", color = Color.Black)
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

fun sendMoveToOpponent(board: List<List<PointType>>, outputStream: OutputStream?) {
    val boardData = board.flatten().joinToString(",") { it.name } // Serialize board to a string
    outputStream?.write("$boardData\n".toByteArray())
    outputStream?.flush()
}

// Function to listen for incoming moves from the opponent
fun listenForOpponentMove(inputStream: InputStream?, onMoveReceived: (List<List<PointType>>) -> Unit) {
    val buffer = ByteArray(1024)
    var bytes: Int

    while (true) {
        try {
            bytes = inputStream?.read(buffer) ?: break
            val receivedData = String(buffer, 0, bytes).trim()

            // Deserialize the board data from the received string
            val boardData = receivedData.split(",").map { PointType.valueOf(it) }
            val updatedBoard = boardData.chunked(3) // Convert the flat list back to a 2D list
            onMoveReceived(updatedBoard)
        } catch (e: IOException) {
            e.printStackTrace()
            break
        }
    }
}
