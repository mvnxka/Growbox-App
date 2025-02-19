package com.example.growbox

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import java.io.InputStream
import java.util.*

class BluetoothService(private val context: Context, private val onDataReceived: (String) -> Unit) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null
    private var receiveJob: Job? = null

    @SuppressLint("MissingPermission")
    fun connect(deviceAddress: String) {
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        device?.let {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            try {
                socket = it.createRfcommSocketToServiceRecord(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")) // BLE UUID
                socket?.connect()
                startListening()
            } catch (e: Exception) {
                e.printStackTrace()
                socket = null
            }
        }
    }

    private fun startListening() {
        receiveJob = CoroutineScope(Dispatchers.IO).launch {
            socket?.inputStream?.let { stream ->
                val reader = stream.bufferedReader()
                try {
                    while (isActive) {
                        val data = reader.readLine()
                        if (!data.isNullOrEmpty()) {
                            withContext(Dispatchers.Main) {
                                onDataReceived(data.trim()) // Przekazanie danych do UI
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun disconnect() {
        receiveJob?.cancel()
        socket?.close()
        socket = null
    }
}
