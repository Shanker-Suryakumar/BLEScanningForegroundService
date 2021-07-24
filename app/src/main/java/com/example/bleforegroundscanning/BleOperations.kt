package com.example.bleforegroundscanning

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.collections.HashSet
import kotlin.concurrent.schedule

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class BleOperations(context: Context) {

    private var isScanning: Boolean = false
    private var mContext: Context? = null
    private var mAddressSet: HashSet<Any>? = null

    init {
        mContext = context
        mAddressSet = HashSet()
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy<BluetoothAdapter> {
        val bluetoothManager = mContext?.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    fun isBleEnabled(): Boolean {
        if (Constants.DEBUG.IS_DEBUG_MODE) {
            Log.d(TAG, "isBleEnabled check = ${bluetoothAdapter.isEnabled}")
        }
        return bluetoothAdapter.isEnabled
    }

    private val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(Constants.BLE.VEST_SERVICE_UUID))
            .build()

    private val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                // Set value to the app preference
                prefs.appPrefs = result.toString()
                if (Constants.DEBUG.IS_DEBUG_MODE) {
                    Log.d(TAG, "ScanCallback found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                isScanning = true
                if (!mAddressSet?.contains(result.device.address)!!) {
                    mAddressSet?.add(result.device.address)
                }
            }

            Timer("Setting up to call stopBle Scan after the pre-defined time", false).schedule(Constants.BLE.BLE_TIMER) {
                if (Constants.DEBUG.IS_DEBUG_MODE) {
                    Log.d(TAG, "Calling stopBleScan after pre-defined time...")
                }
                stopBleScan()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            if (Constants.DEBUG.IS_DEBUG_MODE) {
                Log.d(TAG, "ScanCallback onScanFailed: code$errorCode")
            }
        }
    }

    fun startBleScan() {
        if (Constants.DEBUG.IS_DEBUG_MODE) {
            Log.d(TAG, "startBleScan called...")
        }
        if (bluetoothAdapter.isEnabled) {
            if (Constants.DEBUG.IS_DEBUG_MODE) {
                Log.d(TAG, "startBleScan called inside...")
            }
            isScanning = true
            bleScanner.startScan(null, scanSettings, scanCallback)
        }
    }

    fun stopBleScan() {
        if (Constants.DEBUG.IS_DEBUG_MODE) {
            Log.d(TAG, "stopBleScan called...")
        }
        isScanning = false
        bleScanner.stopScan(scanCallback)
    }

    companion object {
        private val TAG: String by lazy { BleOperations::class.java.simpleName }
    }
}