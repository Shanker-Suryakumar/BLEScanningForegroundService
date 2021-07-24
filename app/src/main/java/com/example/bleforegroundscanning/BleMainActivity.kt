package com.example.bleforegroundscanning

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bleforegroundscanning.Constants.BLE.Companion.LOCATION_PERMISSION_REQUEST_CODE

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class BleMainActivity : AppCompatActivity() {

    private var resultLaunchActivity: ActivityResultLauncher<Intent>? = null
    private var mBleManager: BleManager? = null
    private var tvService: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ble_activity_main)

        val tvService = findViewById<TextView>(R.id.tvService)
        tvService.text = "BLE Foreground Service Example"

        if (mBleManager == null) {
            BleManager.getInstance().also { it -> mBleManager = it }
        }

        registerBluetoothEnableActivityForResult()

        //Register a broadcast receiver
        registerReceiver(broadcastReceiver, IntentFilter(Constants.ACTION.FINISH_ACTIVITY))
    }

    //Setting up broadcast receiver for the service to call this activity to kill this
    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            finishAndRemoveTask()
        }
    }

    override fun onResume() {
        super.onResume()
        when {
            prefs.appPrefs != null && prefs.appPrefs!!.isNotEmpty() -> {
                // Get value from the app preference
                tvService?.text = prefs.appPrefs
            }
        }
        when {
            !mBleManager?.isBleEnabled(this)!! -> promptEnableBluetooth()
        }
        requestLocationPermission()
        if (!Utils.isMyServiceRunning(this, BleForegroundService::class.java)) startForegroundService()
    }

    //This ensures that if the user presses back button, the app first stops the service, and then kill this activity and then remove the task from recent activity menu
    override fun onBackPressed() {
        super.onBackPressed()
    }

    //Here we will unregister teh broadcast receiver
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    private fun promptEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        resultLaunchActivity?.launch(enableBtIntent)
    }

    private fun registerBluetoothEnableActivityForResult() {
        resultLaunchActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                if (Constants.DEBUG.IS_DEBUG_MODE) {
                    Log.d(TAG, "Bluetooth is enabled")
                }
                //There are no request codes
                val resultIntent: Intent? = activityResult.data
                //your operation...
            } else {
                if (Constants.DEBUG.IS_DEBUG_MODE) {
                    Log.d(TAG, "Bluetooth is not enabled")
                }
            }
        }
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        runOnUiThread {
            showRequestPermissionAlertDialog()
        }
    }

    private fun showRequestPermissionAlertDialog() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@BleMainActivity)
        alertDialog.setTitle("Location Permission Required")
        alertDialog.setMessage("Starting from Android M (6.0), the system requires apps to be granted \" + \n" +
                " \"location access in order to scan for BLE devices.")
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(android.R.string.ok) { _, _ ->
            requestPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        alertDialog.setNegativeButton("EXIT APP") { _, _ ->
            finishAndRemoveTask()
        }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(false)
        alert.show()
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    private val isLocationPermissionGranted get() = hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    if (Constants.DEBUG.IS_DEBUG_MODE) {
                        Log.d(TAG, "Location permission granted...")
                    }
                }
            }
        }
    }

    private fun startForegroundService() {
        Toast.makeText(this, "Starting Foregorund Service", Toast.LENGTH_SHORT).show()
        if (Constants.DEBUG.IS_DEBUG_MODE) {
            Log.d(TAG, "Starting Foreground Service...")
        }

        //Also as this is the 1st entry point into the app, we are gonna start the service over here only
        val intent = Intent(this, BleForegroundService::class.java)
        intent.putExtra(
                Constants.ACTION.TITLE_KEEP_ALIVE,
                Constants.NOTIFICATION.BLE_FOREGROUND_SERVICE_ID
        )
        //Here we are starting the foreground service no matter what OS it is
        ContextCompat.startForegroundService(this, intent)
        if (Constants.DEBUG.IS_DEBUG_MODE) {
            Log.d(TAG, "Foreground service is running...")
        }
    }

    companion object {
        private val TAG: String = BleMainActivity::class.java.simpleName
    }
}