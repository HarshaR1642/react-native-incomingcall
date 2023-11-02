package com.incomingcall

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
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

  @RequiresApi(Build.VERSION_CODES.O)
  @ReactMethod
  fun showIncomingCall(options: ReadableMap?) {
    reactApplicationContext.stopService(
      Intent(
        reactApplicationContext,
        CallService::class.java
      )
    )
    val intent = Intent(reactApplicationContext, CallService::class.java)
    intent.putExtra("channelName", options?.getString("channelName"))
    intent.putExtra("channelId", options?.getString("channelId"))
    intent.putExtra("timeout", options?.getDouble("timeout")?.toLong() ?: Constants.TIME_OUT)
    intent.putExtra("component", options?.getString("component"))
    intent.putExtra("callerName", options?.getString("callerName"))
    intent.putExtra("accessToken", options?.getString("accessToken"))

    reactApplicationContext.startForegroundService(intent)
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
  fun sendEventToJs(eventName: String, params: WritableMap?) {
    reactApplicationContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      ?.emit(eventName, params)
  }
}
