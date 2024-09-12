package com.incomingcall

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.facebook.react.ReactActivity

class CallingActivityNew : ReactActivity() {

    private var animationCancelled = false

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            //Some devices need the code below to work when the device is locked
            val keyguardManager =
                getSystemService(AppCompatActivity.KEYGUARD_SERVICE) as KeyguardManager
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

        fun onAnswer() {
            stopService(Intent(this, CallService::class.java))
            IncomingCallModule.sendIntercomBroadcast(this, "Call accepted from full screen")
            val answerIntent = Intent(this, AnswerCallActivity::class.java)
            startActivity(answerIntent)
            finishAndRemoveTask()
        }

        fun onDecline() {
            stopService(Intent(this, CallService::class.java))
            IncomingCallModule.sendIntercomBroadcast(this, "Call declined from full screen")
            finishAndRemoveTask()
        }

        val callIncomingLayout: LinearLayout = findViewById(R.id.callIncomingLayout)
        val callIncoming: ImageButton = findViewById(R.id.callIncoming)

        val swipeUpToAnswerText: TextView = findViewById(R.id.swipeUpToAnswerText)
        val swipeDownToDeclineText: TextView = findViewById(R.id.swipeDownToDeclineText)

        val wobble = ObjectAnimator.ofFloat(callIncoming, "rotation", 0F, 20F).apply {
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = Animation.INFINITE
            duration = 75
        }

        val translateUp = ObjectAnimator.ofFloat(callIncomingLayout, "translationY", -150F).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 800
            startDelay = 300
            doOnStart {
                wobble.start()
            }
            doOnEnd {
                wobble.cancel()
            }
        }

        val translateDown = ObjectAnimator.ofFloat(callIncomingLayout, "translationY", 0F).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 800
        }

        val animation = AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            playSequentially(translateUp, translateDown)
            doOnStart {
                animationCancelled = false
            }
            doOnCancel {
                animationCancelled = true
            }
            doOnEnd {
                if (!animationCancelled) {
                    it.start()
                }
            }
        }

        animation.start()

        var translationY = 0F
        val handler = Handler(Looper.getMainLooper())
        fun reset() {
            translationY = 0F
            val drawable = ResourcesCompat.getDrawable(resources, R.drawable.icon_background, theme)
            drawable?.setTint(ContextCompat.getColor(this, R.color.white))
            callIncoming.background = drawable
            callIncoming.setImageDrawable(
                ResourcesCompat.getDrawable(resources, R.drawable.call_accept, theme)
            )
            callIncoming.translationY = 0F
            swipeUpToAnswerText.alpha = 1F
            swipeDownToDeclineText.alpha = 1F
            animation.start()
        }
        callIncoming.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animation.cancel()
                    wobble.cancel()
                    callIncoming.rotation = 0F
                    swipeUpToAnswerText.alpha = 0F
                    swipeDownToDeclineText.alpha = 0F
                    translationY = event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    var newY = event.rawY - translationY
                    if (newY < 0 && newY < -300F) {
                        newY = -300F
                    } else if (newY > 0 && newY > 300F) {
                        newY = 300F
                    }

                    val drawable =
                        ResourcesCompat.getDrawable(resources, R.drawable.icon_background, theme)

                    if (newY < -220F) {
                        callIncoming.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources,
                                R.drawable.call_accept_white,
                                theme
                            )
                        )
                        drawable?.setTint(ContextCompat.getColor(this, R.color.answer))
                    } else if (newY > 220F) {
                        drawable?.setTint(ContextCompat.getColor(this, R.color.decline))
                        callIncoming.setImageDrawable(
                            ResourcesCompat.getDrawable(resources, R.drawable.call_end, theme)
                        )
                    } else {
                        drawable?.setTint(ContextCompat.getColor(this, R.color.white))
                        callIncoming.setImageDrawable(
                            ResourcesCompat.getDrawable(resources, R.drawable.call_accept, theme)
                        )
                    }
                    callIncoming.background = drawable
                    callIncoming.translationY = newY
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val newY = event.rawY - translationY
                    if (newY < -220F) {
                        onAnswer()
                        handler.postDelayed({ reset() }, 1000)
                    } else if (newY > 220F) {
                        onDecline()
                        handler.postDelayed({ reset() }, 1000)
                    } else {
                        reset()
                    }
                }
            }
            true
        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        val mIntentFilter = IntentFilter()
        mIntentFilter.addAction(Constants.ACTION_END_CALL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mBroadcastReceiver, mIntentFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(mBroadcastReceiver, mIntentFilter)
        }

        IncomingCallModule.sendIntercomBroadcast(this, "Incoming call full screen showed")

    }

    override fun onStart() {
        super.onStart()
        active = true
    }

    override fun onDestroy() {
        super.onDestroy()
        active = false
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
        private const val TAG_KEYGUARD = "Incoming:CallActivity"
    }
}
