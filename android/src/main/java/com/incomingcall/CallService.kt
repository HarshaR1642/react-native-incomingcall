package com.incomingcall

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
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
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.lang.Error

class CallService : Service() {
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

    val notification = NotificationCompat.Builder(this, Constants.CHANNEL)
    notification.setContentTitle(Constants.INCOMING_CALL)
    notification.setTicker(Constants.INCOMING_CALL)
    notification.setContentText(Constants.INCOMING_CALL)
    notification.setSmallIcon(R.drawable.incoming_video_call)
    notification.setCategory(NotificationCompat.CATEGORY_CALL)
    notification.setOngoing(true)
    notification.setFullScreenIntent(pendingIntent, true)
    notification.setStyle(NotificationCompat.DecoratedCustomViewStyle())
    notification.setCustomContentView(customView)
    notification.setCustomBigContentView(customView)
    notification.setPriority(NotificationCompat.PRIORITY_HIGH) // Set notification priority to high to show in power saving mode
    notification.color = 0XF9F9FC

    return notification.build()
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

  private fun stopVibration() {
    vibrator?.cancel()
  }

  override fun onDestroy() {
    super.onDestroy()
    removeNotification()
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
