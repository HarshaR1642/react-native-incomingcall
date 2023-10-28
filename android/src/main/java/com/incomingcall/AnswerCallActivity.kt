package com.incomingcall

import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.PictureInPictureParams
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.ReactActivity
import com.facebook.react.ReactFragment
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap


class AnswerCallActivity : ReactActivity() {

  override fun onStart() {
    super.onStart()
    active = true
  }

  override fun onDestroy() {
    super.onDestroy()
    active = false
  }

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


    if (CallingActivity.active) {
      sendBroadcast(Intent(Constants.ACTION_END_INCOMING_CALL))
    }

    val mIntentFilter = IntentFilter();
    mIntentFilter.addAction(Constants.ACTION_END_ACTIVE_CALL);
    registerReceiver(mBroadcastReceiver, mIntentFilter)

    stopService(Intent(this, CallService::class.java))
    val notificationManager =
      getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(1000)
    setContentView(R.layout.call_accept)
    val bundle = intent.extras
    val component = bundle?.getString("component")
    val reactNativeFragment = ReactFragment.Builder()
      .setComponentName(component)
      .setLaunchOptions(bundle)
      .build()
    supportFragmentManager
      .beginTransaction()
      .add(R.id.reactNativeFragment, reactNativeFragment)
      .commit()
  }

  private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
      if (intent.action == Constants.ACTION_END_ACTIVE_CALL) {
        finishAndRemoveTask()
      }
    }
  }

  override fun onBackPressed() {
    // super.onBackPressed()
    enterPipMode(380, 214)
  }

  override fun onUserLeaveHint() {
    enterPipMode(380, 214)
    super.onUserLeaveHint()
  }

  override fun onPictureInPictureModeChanged(
    isInPictureInPictureMode: Boolean,
    newConfig: Configuration?
  ) {
    super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    val map: WritableMap = Arguments.createMap()
    map.putString("pip_mode", isInPictureInPictureMode.toString())
    IncomingCallModule(reactInstanceManager.currentReactContext as ReactApplicationContext).sendEventToJs(
      "pip_mode",
      map
    )
    if (onPipExit) {
      finishAndRemoveTask()
    }
  }

  private fun enterPipMode(width: Int, height: Int) {
    if(this.isFinishing || this.isDestroyed || !active){
      return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val ratWidth = if (width > 0) width else 380
      val ratHeight = if (height > 0) height else 214
      val ratio = Rational(ratWidth, ratHeight)
      var pip_Builder: PictureInPictureParams.Builder? = null
      pip_Builder = PictureInPictureParams.Builder()
      pip_Builder.setAspectRatio(ratio).build()
      this.enterPictureInPictureMode(pip_Builder.build())
    }
  }

  override fun onStop() {
    super.onStop()
    onPipExit = true
  }

  override fun onResume() {
    super.onResume()
    onPipExit = false
  }

  companion object {
    var onPipExit = false
    var active = false
    private const val TAG_KEYGUARD = "Incoming:unLock"
  }
}
