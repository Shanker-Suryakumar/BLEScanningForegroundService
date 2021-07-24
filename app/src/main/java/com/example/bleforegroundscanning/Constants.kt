package com.example.bleforegroundscanning

class Constants {

    interface DEBUG {
        companion object {
            const val IS_DEBUG_MODE: Boolean = true
        }
    }

    interface ACTION {
        companion object {
            const val ACTION_STOP: String = "stop"
            const val TITLE_KEEP_ALIVE: String = "keepAlive"
            const val FINISH_ACTIVITY: String = "finishActivity"
        }
    }

    interface NOTIFICATION {
        companion object {
            const val REQUEST_CODE: Int = 0
            const val BLE_FOREGROUND_SERVICE_ID: Int = 1
            const val BLE_FOREGROUND_SERVICE: String = "BLE Foreground Service"
            const val BLE_FOREGROUND_SERVICE_RUNNING: String = "BLE foreground service is running"
            const val TITLE_STOP: String = "Stop"
            const val HANDLER_THREAD_NAME: String = "ServiceBackgroundHandler"
        }
    }

    interface BLE {
        companion object {
            const val VEST_SERVICE_UUID: String = "0000180f-0000-1000-8000-00805f9b34fb" // change to specific UUID later
            const val LOCATION_PERMISSION_REQUEST_CODE: Int = 1993
            const val BLE_TIMER: Long = 3000L
            const val BLE_SCAN_AGAIN_TIMER: Long = 30000L
        }
    }
}