package com.incomingcall

import android.content.Intent
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

data class CallData(
  val channelName: String? = Constants.INCOMING_CALL,
  val channelId: String? = Constants.INCOMING_CALL,
  val timeout: Long?,
  val component: String?,
  val callerName: String? = "Visitor",
  val accessToken: String?
) : Parcelable {

  constructor(parcel: Parcel) : this(
    parcel.readString(),
    parcel.readString(),
    parcel.readLong(),
    parcel.readString(),
    parcel.readString(),
    parcel.readString()
  )

  override fun describeContents(): Int {
    return 0
  }

  override fun writeToParcel(parcel: Parcel, p: Int) {
    parcel.writeString(channelName)
    parcel.writeString(channelId)
    parcel.writeLong(timeout ?: Constants.TIME_OUT)
    parcel.writeString(component)
    parcel.writeString(callerName)
    parcel.writeString(accessToken)
  }

  companion object CREATOR : Parcelable.Creator<CallData> {
    override fun createFromParcel(parcel: Parcel): CallData {
      return CallData(parcel)
    }

    override fun newArray(size: Int): Array<CallData?> {
      return arrayOfNulls(size)
    }
  }

}

class IncomingCallModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
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
    intent.putExtra("timeout", options?.getString("timeout"))
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

    if (CallingActivity.active) {
      reactApplicationContext.sendBroadcast(Intent(Constants.ACTION_END_INCOMING_CALL))
    }
    if (AnswerCallActivity.active) {
      reactApplicationContext.sendBroadcast(Intent(Constants.ACTION_END_ACTIVE_CALL))
    }
  }

  @ReactMethod
  fun sendEventToJs(eventName: String, params: WritableMap?) {
    reactApplicationContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      ?.emit(eventName, params)
  }

  companion object {
    const val NAME = "IncomingCall"
  }
}
