package com.example.tictactoe

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictactoe.ui.theme.TicTacToeTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicTacToeTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text("Tic Tac Toe")
                            }
                        )
                    }, content = {
                        innerPadding ->
                        Column(
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(
                                id = R.drawable.tic_tac_toe,
                            ), contentDescription = "Tic Tac Toe",
                                modifier =
                                Modifier.width(100.dp).height(100.dp)
                            )
                            Spacer(Modifier.height(25.dp))

                            Text("Game Options",
                                fontStyle = FontStyle.Italic,
                                fontSize = 30.sp,
                                color = Color.White)

                            Spacer(Modifier.height(50.dp))

                            MainScreenOptions("Play vs AI", onClick = {
                                val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                                startActivity(intent)
                            })
                            /*MainScreenOptions("Play vs Player", onClick = {
                                val intent = Intent(this@MainActivity, BluetoothInitialScreen::class.java)
                                startActivity(intent)
                            })*/
                            MainScreenOptions("Past Games", onClick = {
                                val intent = Intent(this@MainActivity, HistoryActivity::class.java)
                                startActivity(intent)
                            })
                        }
                    }
                )
            }
        }
    }
}
@Composable
fun MainScreenOptions(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        modifier = Modifier
            .width(250.dp)
            .padding(vertical = 8.dp)
    ) {
        Text(text = label,
            style = MaterialTheme.typography.titleLarge,
            color = Color.Blue,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold)
    }
}