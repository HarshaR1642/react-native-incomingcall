package com.incomingcall

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.ReactActivity
import com.facebook.react.ReactFragment

class AnswerCallActivity : ReactActivity() {

  override fun onStart() {
    super.onStart()
    active = true
  }

  override fun onDestroy() {
    super.onDestroy()
    active = false
  }

  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(null)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
      //Some devices need the code below to work when the device is locked
      val keyguardManager = getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
      if (keyguardManager.isDeviceLocked) {
        val keyguardLock = keyguardManager.newKeyguardLock(TAG_KEYGUARD)
        keyguardLock.disableKeyguard()
      }
    }
    window.addFlags(
      WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
    )

    IncomingCallModule.sendIntercomBroadcast(this, "Call Answered")

    if (CallingActivity.active) {
      sendBroadcast(Intent(Constants.ACTION_END_CALL))
    }

    val mIntentFilter = IntentFilter();
    mIntentFilter.addAction(Constants.ACTION_END_CALL);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      registerReceiver(mBroadcastReceiver, mIntentFilter, Context.RECEIVER_NOT_EXPORTED)
    } else {
      registerReceiver(mBroadcastReceiver, mIntentFilter)
    }

    stopService(Intent(this, CallService::class.java))
    val notificationManager =
      getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(1000)
    setContentView(R.layout.call_accept)

    val reactNativeFragment = ReactFragment.Builder()
      .setComponentName(Constants.CHANNEL)
      .setLaunchOptions(intent.extras)
      .build()
    supportFragmentManager
      .beginTransaction()
      .add(R.id.reactNativeFragment, reactNativeFragment)
      .commit()
  }

  private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
      if (intent.action == Constants.ACTION_END_CALL) {
        context?.let {
          IncomingCallModule.sendIntercomBroadcast(it, "Call Hangup")
        }
        finishAndRemoveTask()
      }
    }
  }

  companion object {
    var active = false
    private const val TAG_KEYGUARD = "Incoming:Intercom"
  }
}
