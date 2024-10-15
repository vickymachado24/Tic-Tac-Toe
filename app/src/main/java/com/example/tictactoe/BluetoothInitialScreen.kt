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
import java.io.IOException

class BluetoothInitialScreen : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Request Bluetooth permissions
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true &&
                permissions[Manifest.permission.BLUETOOTH_SCAN] == true &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
            } else {
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED // Location permission
        ) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION // Required for scanning on Android 10+
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
        finish()
    }

        // AcceptThread (server)
    private inner class AcceptThread(private val onSocketConnected: (BluetoothSocket) -> Unit) : Thread() {
        @SuppressLint("MissingPermission")
        private val serverSocket: BluetoothServerSocket? =
            bluetoothAdapter.listenUsingRfcommWithServiceRecord("TicTacToe", MY_UUID)

        override fun run() {
            try {
                val socket = serverSocket?.accept()
                socket?.let {
                    onSocketConnected(it)
                    serverSocket?.close() // Close the server socket once connected
                }
            } catch (e: IOException) {
                Log.e("AcceptThread", "Socket accept failed", e)
            }
        }
    }

    // ConnectThread (client)
    @SuppressLint("MissingPermission")
    private inner class ConnectThread(
        private val device: BluetoothDevice,
        private val onSocketConnected: (BluetoothSocket) -> Unit
    ) : Thread() {
        private var socket: BluetoothSocket? = null

        override fun run() {
            bluetoothAdapter.cancelDiscovery() // Cancel discovery to avoid slowing connection

            try {
                // Initial attempt to connect
                socket = device.createRfcommSocketToServiceRecord(MY_UUID)
                socket?.connect()
            } catch (e: IOException) {
                Log.e("ConnectThread", "Connection failed, attempting fallback", e)
                try {
                    // Fallback using reflection
                    val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    socket = method.invoke(device, 1) as BluetoothSocket
                    socket?.connect()
                } catch (fallbackException: Exception) {
                    Log.e("ConnectThread", "Fallback connection failed", fallbackException)
                    return
                }
            }

            socket?.let {
                onSocketConnected(it) // Callback when the connection is established
            }
        }
    }
}