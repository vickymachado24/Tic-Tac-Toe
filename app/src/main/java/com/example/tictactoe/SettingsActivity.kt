package com.example.tictactoe

import androidx.compose.material3.Text
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tictactoe.ui.theme.TicTacToeTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeTheme {
                SettingsScreen(
                    onModeSelected = { selectedMode ->
                        val intent = Intent(this, OnlineGameActivity::class.java).apply {
                            putExtra("MODE", selectedMode)
                        }
                        startActivity(intent)
                    },
                    onBackPress = {
                        onBackPressedDispatcher.onBackPressed()
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onModeSelected: (String) -> Unit,
    onBackPress: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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

        DifficultyButton(label = "Easy", onClick = { onModeSelected("Easy") })
        DifficultyButton(label = "Medium", onClick = { onModeSelected("Medium") })
        DifficultyButton(label = "Hard", onClick = { onModeSelected("Hard") })

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBackPress,
            modifier = Modifier
                .width(150.dp)
                .height(48.dp)
        ) {
            Text(text = "Back", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun DifficultyButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
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
