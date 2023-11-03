package com.incomingcall

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.ReactActivity

class CallingActivity : ReactActivity() {

  private lateinit var name: TextView
  private lateinit var acceptButton: ImageButton
  private lateinit var declineButton: ImageButton

  override fun onStart() {
    super.onStart()
    active = true
  }

  override fun onDestroy() {
    active = false
    super.onDestroy()
  }

  @SuppressLint("UnspecifiedRegisterReceiverFlag")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val bundle = intent.extras
    val callerName = bundle?.getString("callerName") ?: "Visitor"

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

    setContentView(R.layout.call_fullscreen)

    name = findViewById(R.id.name)
    acceptButton = findViewById(R.id.acceptButton)
    declineButton = findViewById(R.id.declineButton)
    name.text = callerName

    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
      View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    val mIntentFilter = IntentFilter();
    mIntentFilter.addAction(Constants.ACTION_END_CALL);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      registerReceiver(mBroadcastReceiver, mIntentFilter, Context.RECEIVER_NOT_EXPORTED)
    } else {
      registerReceiver(mBroadcastReceiver, mIntentFilter)
    }

    acceptButton.setOnClickListener {
      stopService(Intent(this, CallService::class.java))
      val answerIntent = Intent(this, AnswerCallActivity::class.java)
      val component = bundle?.getString("component")
      val accessToken = bundle?.getString("accessToken")
      answerIntent.putExtra("component", component)
      answerIntent.putExtra("accessToken", accessToken)
      startActivity(answerIntent)
      finishAndRemoveTask()
    }

    declineButton.setOnClickListener {
      stopService(Intent(this, CallService::class.java))
      finishAndRemoveTask()
    }
  }

  private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
      if (intent.action == Constants.ACTION_END_CALL) {
        finishAndRemoveTask()
      }
    }
  }

  companion object {
    var active = false
    private const val TAG_KEYGUARD = "Incoming:unLock"
  }
}
