package com.incomingcall

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.Intent.ACTION_SCREEN_ON
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.VolumeProviderCompat
import androidx.media.session.MediaButtonReceiver


class CallService : Service() {

    private lateinit var mediaSession: MediaSessionCompat
    private var powerButtonReceiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        try {
            val bundle = intent?.extras

            val notification: Notification = buildNotification(bundle)
            startForeground(Constants.FOREGROUND_SERVICE_ID, notification)
            playRingtone()
            startVibration()
            setVolumeReceiver()
            setPowerButtonReceiver()
            startTimer(Constants.TIME_OUT)
            IncomingCallModule.sendIntercomBroadcast(this, "Notification showed")

        } catch (error: Error) {
            IncomingCallModule.sendIntercomBroadcast(this, "Failed to show notification")
        }

        return START_NOT_STICKY
    }

    private fun buildNotification(bundle: Bundle?): Notification {

        val customView = RemoteViews(packageName, R.layout.call_notification)

        val notificationIntent = Intent(this, CallingActivity::class.java)
        val hungUpIntent = Intent(this, HungUpBroadcast::class.java)
        val answerIntent = Intent(this, AnswerCallActivity::class.java)

        val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, flag)
        val hungUpPendingIntent = PendingIntent.getBroadcast(this, 0, hungUpIntent, flag)
        val answerPendingIntent = PendingIntent.getActivity(this, 0, answerIntent, flag)

        customView.setOnClickPendingIntent(R.id.btnAnswer, answerPendingIntent)
        customView.setOnClickPendingIntent(R.id.btnDecline, hungUpPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                Constants.CHANNEL,
                Constants.CHANNEL,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.setSound(null, null)
            notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

            notificationManager.createNotificationChannel(notificationChannel)
        } else {
            IncomingCallModule.sendIntercomBroadcast(this, "Android OS less than API 26")
        }

        return NotificationCompat.Builder(this, Constants.CHANNEL)
            .setContentTitle(Constants.INCOMING_CALL)
            .setTicker(Constants.INCOMING_CALL)
            .setContentText(Constants.INCOMING_CALL)
            .setSmallIcon(R.drawable.incoming_video_call)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setFullScreenIntent(pendingIntent, true)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(customView)
            .setCustomBigContentView(customView)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set notification priority to high to show in power saving mode
            .setColor(ContextCompat.getColor(this, R.color.white))
            .setColorized(true)
            .build()
    }

    private fun removeNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.FOREGROUND_SERVICE_ID)
    }

    private fun startTimer(timeout: Long) {
        runnable = Runnable {
            run {
                stopSelf()
                if (CallingActivity.active) {
                    sendBroadcast(Intent(Constants.ACTION_END_CALL))
                }
            }
        }
        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed(runnable!!, timeout)
    }

    private fun cancelTimer() {
        handler?.removeCallbacks(runnable!!)
    }

    private fun playRingtone() {
        ringtone = RingtoneManager.getRingtone(
            this,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        )
        ringtone?.play()
    }

    private fun stopRingtone() {
        ringtone?.stop()
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startVibration() {
        val vibratePattern = longArrayOf(0, 1000, 1000)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        stopVibration()
        vibrator?.vibrate(VibrationEffect.createWaveform(vibratePattern, 0))
    }

    private fun setVolumeReceiver() {
        mediaSession = MediaSessionCompat(
            this,
            "CallService",
            ComponentName(this, MediaButtonReceiver::class.java),
            null
        )

        mediaSession.apply {
            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE
                    )
                    .setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        0,
                        0f
                    )
                    .build()
            )
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    val key: KeyEvent? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mediaButtonEvent?.getParcelableExtra(
                                Intent.EXTRA_KEY_EVENT,
                                KeyEvent::class.java
                            )
                        } else {
                            mediaButtonEvent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                        }

                    if (ringtone?.isPlaying == true && key?.keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                        val intent =
                            Intent(this@CallService, AnswerCallActivity::class.java).apply {
                                addFlags(FLAG_ACTIVITY_NEW_TASK)
                            }
                        startActivity(intent)
                        IncomingCallModule.sendIntercomBroadcast(
                            this@CallService,
                            "Call accepted from Notification"
                        )
                        stopSelf()
                    }
                    return true
                }
            })
            setPlaybackToRemote(object : VolumeProviderCompat(
                VOLUME_CONTROL_RELATIVE,
                100,
                100
            ) {
                override fun onAdjustVolume(direction: Int) {
                    if ((direction == -1 || direction == 1) && ringtone?.isPlaying == true) {
                        stopRingtone()
                        stopVibration()
                    }
                }
            })
            setActive(true)
        }
    }

    private fun setPowerButtonReceiver() {
        powerButtonReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (ringtone?.isPlaying == true) {
                    stopRingtone()
                    stopVibration()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(ACTION_SCREEN_ON)
            addAction(ACTION_SCREEN_OFF)
            priority = IntentFilter.SYSTEM_HIGH_PRIORITY
        }
        registerReceiver(powerButtonReceiver, filter)
    }

    private fun stopVibration() {
        vibrator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeNotification()
        if (powerButtonReceiver != null) {
            unregisterReceiver(powerButtonReceiver)
            powerButtonReceiver = null
        }
        mediaSession.release()
        stopRingtone()
        stopVibration()
        cancelTimer()
    }

    companion object {
        var handler: Handler? = null
        var runnable: Runnable? = null
        var vibrator: Vibrator? = null
        var ringtone: Ringtone? = null
    }
}
