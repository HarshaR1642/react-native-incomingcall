package com.incomingcall

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule

@ReactModule(name = Constants.INCOMING_CALL)
class IncomingCallModule(reactContext: ReactApplicationContext) :
    NativeIncomingCallSpec(reactContext) {

    override fun getName() = Constants.INCOMING_CALL

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.extras?.getString("action")
            if (!action.isNullOrEmpty()) {
                val params: WritableMap = Arguments.createMap()
                params.putString("action", action)
                sendEventToJs("intercom_broadcast", params)
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun registerReceiver() {
        try {
            unregisterReceiver()
            val intentFilter = IntentFilter("android.intercom.broadcast")
            reactApplicationContext.registerReceiver(broadcastReceiver, intentFilter)
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    override fun unregisterReceiver() {
        try {
            reactApplicationContext.unregisterReceiver(broadcastReceiver)
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun showIncomingCall() {
        if (AnswerCallActivity.active) {
            return
        }
        reactApplicationContext.stopService(
            Intent(
                reactApplicationContext,
                CallService::class.java
            )
        )
        val intent = Intent(reactApplicationContext, CallService::class.java)

        reactApplicationContext.startForegroundService(intent)

        sendIntercomBroadcast(reactApplicationContext, "Invoked call notification")
    }

    override fun endCall() {
        reactApplicationContext.stopService(
            Intent(
                reactApplicationContext,
                CallService::class.java
            )
        )

        if (CallingActivity.active || AnswerCallActivity.active) {
            reactApplicationContext.sendBroadcast(Intent(Constants.ACTION_END_CALL))
        }
    }

    override fun areNotificationsEnabled(promise: Promise) {
        if (NotificationManagerCompat.from(reactApplicationContext).areNotificationsEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager =
                    reactApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = notificationManager.getNotificationChannel(Constants.CHANNEL)
                promise.resolve(channel?.importance != NotificationManager.IMPORTANCE_NONE)
            } else {
                promise.resolve(true)
            }
        }
        promise.resolve(false)
    }

    fun sendEventToJs(eventName: String, params: WritableMap?) {
        reactApplicationContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            ?.emit(eventName, params)
    }

    companion object {
        fun sendIntercomBroadcast(context: Context, action: String) {
            val intent = Intent("android.intercom.broadcast")
            intent.putExtra("action", action)
            context.sendBroadcast(intent)
        }
    }
}
