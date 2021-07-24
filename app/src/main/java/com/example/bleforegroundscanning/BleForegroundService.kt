package com.example.bleforegroundscanning

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.concurrent.schedule

//Here let's create Foreground Service, which do it's long running work on background threads
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class BleForegroundService : Service() {

    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private var isScanning: Boolean = false
    private var mBleManager: BleManager? = null

    //Here my objective will be as soon, as people bind to this service, we will increment a KeepAlive counter, which was initially 0,
    //This will be checked after every 10 seconds, that if that counter reaches 0, then we will stop the foreground service
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        handlerThread = HandlerThread(Constants.NOTIFICATION.HANDLER_THREAD_NAME)
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        if (mBleManager == null) {
            BleManager.getInstance().also { it -> mBleManager = it }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.ACTION.ACTION_STOP) {
            stopAppAndQuit()
            return START_NOT_STICKY
        }

        //Setting up the foreground service notification
        val stopnotificationIntent = Intent(this, BleForegroundService::class.java)
        stopnotificationIntent.action = Constants.ACTION.ACTION_STOP
        val pendingIntent: PendingIntent = PendingIntent.getService(this, Constants.NOTIFICATION.REQUEST_CODE,
                stopnotificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val notification: Notification = NotificationCompat.Builder(this, App.serviceChannelId)
                .setContentTitle(Constants.NOTIFICATION.BLE_FOREGROUND_SERVICE)
                .setContentText(Constants.NOTIFICATION.BLE_FOREGROUND_SERVICE)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        Constants.NOTIFICATION.TITLE_STOP,
                        pendingIntent
                )
                .build()

        startForeground(Constants.NOTIFICATION.BLE_FOREGROUND_SERVICE_ID, notification)

        Timer("Setting up to call doSomeBackgroundWork after the pre-defined time", false).schedule(Constants.BLE.BLE_SCAN_AGAIN_TIMER) {
            if (Constants.DEBUG.IS_DEBUG_MODE) {
                Log.d(TAG, "Calling doSomeBackgroundWork after pre-defined time...")
            }
            doSomeBackgroundWork()
        }
        return START_NOT_STICKY
    }

    private fun doSomeBackgroundWork() {
        isScanning = BleManager?.isBleScanning(this.applicationContext)!!
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (Constants.DEBUG.IS_DEBUG_MODE) {
                    Log.d(TAG, "Running doSomeBackgroundWork, with status of isScanning at start = $isScanning")
                }

                if (!isScanning) {
                    mBleManager?.callStartBleScan(this@BleForegroundService.applicationContext)
                    isScanning = mBleManager?.isBleScanning(this@BleForegroundService.applicationContext)!!

                }

                if (Constants.DEBUG.IS_DEBUG_MODE) {
                    Log.d(TAG, "Running doSomeBackgroundWork, with status of isScanning after starting = $isScanning")
                }

                //Do something after pre-defined time
                handler.postDelayed(this, Constants.BLE.BLE_SCAN_AGAIN_TIMER)
            }
        }, Constants.BLE.BLE_TIMER) //the time is in milliseconds
    }

    //This is run, from the notification, in case user is not interested in running the app
    private fun stopAppAndQuit() {
        handler.removeCallbacksAndMessages(null)
        //quitSafely ensures that all pending messages are processed before the thread stops.
        handlerThread.quitSafely()
        stopForeground(true)
        stopSelf()
        sendBroadcast(Intent(Constants.ACTION.FINISH_ACTIVITY))
        //val intent = Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVTY_CLEAR_TASK)
        //startActivity(intent)
    }

    //We are overwriting this, as we want the foreground service to be killed as soon as the app is closed from the recent screen
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
//        handler.removeCallbacksAndMessages(null)
//        //quitSafely ensures that all pending messages are processed before the thread stops.
//        handlerThread.quitSafely()
//        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        //quitSafely ensures that all pending messages are processed before the thread stops.
        handlerThread.quitSafely()
        stopSelf()
    }

    companion object {
        private val TAG: String = BleForegroundService::class.java.simpleName
    }
}