package com.incomingcall

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class VolumeButtonReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent?.action == "android.media.VOLUME_CHANGED_ACTION") {
      // Mute the ringtone when volume button is pressed
      CallService.ringtone?.stop()
      CallService.vibrator?.cancel()
      Log.d("VolumeButtonReceiver", "Volume button pressed, ringtone stopped")
    }
  }
}

