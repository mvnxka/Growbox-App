package com.example.growbox

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothService: BluetoothService? = null
    private val PERMISSION_REQUEST_CODE = 101

    // Lista wymaganych uprawnień
    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Sprawdzenie i żądanie uprawnień
        if (!hasAllPermissions()) {
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
        } else {
            checkManageExternalStoragePermission()
        }

        val btnHumidity: Button = findViewById(R.id.btnHumidity)
        val btnTemperature: Button = findViewById(R.id.btnTemperature)
        val btnConnectBluetooth: ImageButton = findViewById(R.id.btnConnectBluetooth)
        val btnDisconnectBluetooth: ImageButton = findViewById(R.id.btnDisconnectBluetooth)

        btnHumidity.setOnClickListener {
            if (bluetoothService == null) {
                showBluetoothWarning()
            } else {
                startActivity(Intent(this, HumidityActivity::class.java))
            }
        }

        btnTemperature.setOnClickListener {
            if (bluetoothService == null) {
                showBluetoothWarning()
            } else {
                startActivity(Intent(this, TemperatureActivity::class.java))
            }
        }

        btnConnectBluetooth.setOnClickListener {
            showDeviceList()
        }

        btnDisconnectBluetooth.setOnClickListener {
            bluetoothService = null
            Toast.makeText(this, "Rozłączono Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    // Sprawdzanie, czy wszystkie wymagane uprawnienia zostały przyznane
    private fun hasAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Sprawdzanie i proszenie o MANAGE_EXTERNAL_STORAGE na Androidzie 11+
    private fun checkManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    // Obsługa odpowiedzi użytkownika na żądanie uprawnień
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.filterIndexed { index, _ ->
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }

            if (deniedPermissions.isNotEmpty()) {
                Toast.makeText(this, "Nie przyznano uprawnień: ${deniedPermissions.joinToString()}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Wszystkie wymagane uprawnienia zostały przyznane", Toast.LENGTH_SHORT).show()
                checkManageExternalStoragePermission()
            }
        }
    }

    private fun showBluetoothWarning() {
        AlertDialog.Builder(this)
            .setTitle("Brak połączenia Bluetooth")
            .setMessage("Najpierw połącz urządzenie Bluetooth.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeviceList() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Bluetooth jest wyłączony", Toast.LENGTH_SHORT).show()
            return
        }

        // Sprawdzenie uprawnień do Bluetooth przed pokazaniem listy urządzeń
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Brak uprawnień do połączenia Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        if (pairedDevices.isNullOrEmpty()) {
            Toast.makeText(this, "Brak sparowanych urządzeń.", Toast.LENGTH_SHORT).show()
            return
        }

        val deviceList = pairedDevices.map { device ->
            device.name + "\n" + device.address
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Wybierz urządzenie")
            .setItems(deviceList) { _, which ->
                val device = pairedDevices.elementAt(which)
                bluetoothService = BluetoothService(this) {}
                bluetoothService?.connect(device.address)
                Toast.makeText(this, "Połączono z ${device.name}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
