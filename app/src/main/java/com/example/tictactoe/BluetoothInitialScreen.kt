package com.example.tictactoe

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.core.content.ContextCompat

class BluetoothInitialScreen : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Request Bluetooth permissions if necessary (use your existing permission code)
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // This callback will check if all permissions were granted
            if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true &&
                permissions[Manifest.permission.BLUETOOTH_SCAN] == true) {
                // Permissions were granted, proceed with Bluetooth functionality
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permissions were denied
                Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
        checkBluetoothPermissions()
        setContent {
            Column {
                Button(onClick = { startServer() }) {
                    Text("Start Server")
                }
                Button(onClick = { startClient() }) {
                    Text("Start Client")
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkBluetoothPermissions() {
        // Check if both Bluetooth permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            // If not granted, request the permissions
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            // Permissions are already granted, proceed with Bluetooth functionality
            Toast.makeText(this, "Bluetooth permissions already granted", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to start server and wait for a connection
    private fun startServer() {
        AcceptThread { socket ->
            bluetoothSocket = socket
            moveToGameScreen(true) // After the connection is established
        }.start()
    }

    // Function to start client and connect to the server
    @SuppressLint("MissingPermission")
    private fun startClient() {
        // Get the Bluetooth device from a list or discovery (here assuming first paired device)
        val device = bluetoothAdapter.bondedDevices.firstOrNull() ?: return
        ConnectThread(device) { socket ->
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            if (pairedDevices.isNullOrEmpty()) {
                Log.d("BluetoothTicTacToe", "No available devices")
            }
            bluetoothSocket = socket
            moveToGameScreen(false) // After connection is established
        }.start()
    }

    // Move to the game screen and pass the BluetoothSocket to the next activity
    private fun moveToGameScreen(isServer: Boolean) {
        val intent = Intent(this, BluetoothTicTacToeActivity::class.java).apply {
            putExtra("bluetoothDevice", bluetoothSocket.remoteDevice)
            putExtra("isServer", isServer) // Pass whether this device is the server
        }
        startActivity(intent)
        finish() // End this activity
    }

        // AcceptThread (server)
    private inner class AcceptThread(private val onSocketConnected: (BluetoothSocket) -> Unit) : Thread() {
        @SuppressLint("MissingPermission")
        private val serverSocket: BluetoothServerSocket? =
            bluetoothAdapter.listenUsingRfcommWithServiceRecord("TicTacToe", MY_UUID)

        override fun run() {
            val socket = serverSocket?.accept()
            socket?.let {
                onSocketConnected(it) // Callback when the connection is established
                serverSocket?.close() // Close the server socket once connected
            }
        }
    }

    // ConnectThread (client)
    @SuppressLint("MissingPermission")
    private inner class ConnectThread(
        private val device: BluetoothDevice,
        private val onSocketConnected: (BluetoothSocket) -> Unit
    ) : Thread() {
        private val socket: BluetoothSocket? by lazy { device.createRfcommSocketToServiceRecord(MY_UUID) }

        override fun run() {
            bluetoothAdapter.cancelDiscovery() // Cancel discovery to avoid slowing connection
            socket?.connect()
            socket?.let {
                onSocketConnected(it) // Callback when the connection is established
            }
        }
    }
}
