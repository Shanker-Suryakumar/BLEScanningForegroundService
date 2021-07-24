package com.example.bleforegroundscanning

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object BleManager {

    private val TAG = BleManager::class.qualifiedName

    @Volatile
    private var mBleManager: BleManager? = null

    fun getInstance(): BleManager? {
        if (Constants.DEBUG.IS_DEBUG_MODE) Log.d(TAG, "BleManager getInstance is called...")
        mBleManager ?: synchronized(this) {
            mBleManager ?: BleManager.also { mBleManager = it }
        }
        return mBleManager
    }

    private fun getBleOperationsInstance(context: Context): BleOperations {
        return BleOperations(context)
    }

    fun isBleEnabled(context: Context): Boolean {
        return getBleOperationsInstance(context).isBleEnabled()
    }

    fun isBleScanning(context: Context): Boolean {
        return getBleOperationsInstance(context).isBleEnabled()
    }

    fun callStartBleScan(context: Context) {
        getBleOperationsInstance(context).startBleScan()
    }
}