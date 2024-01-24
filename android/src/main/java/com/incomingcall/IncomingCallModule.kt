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
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

class IncomingCallModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return Constants.INCOMING_CALL
  }

  private val broadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      val action = intent?.extras?.getString("action")
      if(!action.isNullOrEmpty()){
        val params: WritableMap = Arguments.createMap()
        params.putString("action", action)
        sendEventToJs("intercom_broadcast", params)
      }
    }
  }

  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  @ReactMethod
  fun registerReceiver() {
    try {
      unregisterReceiver()
      val intentFilter = IntentFilter("android.intercom.broadcast")
      reactApplicationContext.registerReceiver(broadcastReceiver, intentFilter)
    }catch (error: Exception){
      error.printStackTrace()
    }
  }

  @ReactMethod
  fun unregisterReceiver() {
    try {
      reactApplicationContext.unregisterReceiver(broadcastReceiver)
    }catch (error: Exception){
      error.printStackTrace()
    }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun showIncomingCall(options: ReadableMap?) {
    sendIntercomBroadcast(reactApplicationContext, "starting call")
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

    sendIntercomBroadcast(reactApplicationContext, "started call service")
  }

  @ReactMethod
  fun endCall() {
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

  @ReactMethod
  fun areNotificationsEnabled(promise: Promise) {
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


  @ReactMethod
  fun sendEventToJs(eventName: String, params: WritableMap?) {
    reactApplicationContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      ?.emit(eventName, params)
  }


  companion object {
    fun sendIntercomBroadcast(context: Context, action: String){
      val intent = Intent("android.intercom.broadcast")
      intent.putExtra("action", action)
      context.sendBroadcast(intent)
    }
  }
}
