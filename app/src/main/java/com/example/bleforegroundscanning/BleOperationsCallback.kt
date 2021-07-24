package com.example.bleforegroundscanning

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic

interface BleOperationsCallback {
    fun onDeviceConnected(mBluetoothDevice: BluetoothDevice)

    fun onDeviceDisconnected(mBluetoothDevice: BluetoothDevice?)

    fun onAutoScanDeviceDiscovered(device: BluetoothDevice)

    fun onReconnectOldDevice()

    fun onDataReceived(mBluetoothDevice: BluetoothDevice?,
                       characteristic: BluetoothGattCharacteristic?)
}