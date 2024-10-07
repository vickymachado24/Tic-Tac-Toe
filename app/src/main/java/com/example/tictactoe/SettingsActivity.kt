package com.example.tictactoe

import androidx.compose.material3.Text
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tictactoe.enums.DifficultyLevel
import com.example.tictactoe.ui.theme.TicTacToeTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeTheme {
                SettingsScreen(
                    onModeSelected = { selectedMode ->
                        val intent = Intent(this, GameActivity::class.java).apply {
                            putExtra("MODE", selectedMode)
                        }
                        startActivity(intent)
                    },
                    onBackPress = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onModeSelected: (DifficultyLevel) -> Unit,
    onBackPress: () -> Unit
) {
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
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Select A Mode",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                DifficultyButton(label = "Easy", onClick = { onModeSelected(DifficultyLevel.EASY) }, color = Color(0xff8ee36c))
                DifficultyButton(label = "Medium", onClick = { onModeSelected(DifficultyLevel.MEDIUM) }, color = Color(0xffffb266))
                DifficultyButton(label = "Hard", onClick = { onModeSelected(DifficultyLevel.HARD) }, color = Color(0xffCE5E5E))
            }
        }
    )
}

@Composable
fun DifficultyButton(label: String, onClick: () -> Unit, color: Color) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    TicTacToeTheme {
        SettingsScreen(
            onModeSelected = {},
            onBackPress = {}
        )
    }
}
