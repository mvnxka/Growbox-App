package com.example.growbox

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.io.InputStream
import java.util.*

class BluetoothService(private val context: Context, private val onDataReceived: (String) -> Unit) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var socket: BluetoothSocket? = null

    fun connect(deviceAddress: String) {
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        device?.let {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            try {
                socket = it.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                socket?.connect()
                listenForData()
            } catch (e: Exception) {
                e.printStackTrace()
                socket = null
            }
        }
    }

    private fun listenForData() {
        socket?.inputStream?.let { stream ->
            val buffer = ByteArray(1024)
            val inputStream: InputStream = stream
            val reader = inputStream.bufferedReader()

            while (true) {
                try {
                    val data = reader.readLine()
                    if (data != null) {
                        onDataReceived(data.trim())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }
}
