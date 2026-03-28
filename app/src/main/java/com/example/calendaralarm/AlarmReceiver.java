package com.example.calendaralarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    
    private static final String CHANNEL_ID = "alarm_channel";
    private static final String CHANNEL_NAME = "闹钟提醒";
    private static final String WAKE_LOCK_TAG = "AlarmReceiver::WakeLock";
    private static final int NOTIFICATION_ID = 1001;
    
    private static Ringtone currentRingtone;
    private static Vibrator currentVibrator;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE,
                WAKE_LOCK_TAG
            );
            wakeLock.acquire(60 * 1000L);
        }
        
        String label = intent.getStringExtra("label");
        if (label == null) label = "闹钟";
        
        startAlarmSound(context);
        startVibrate(context);
        showFullScreenNotification(context, label);
        
        if (wakeLock != null) {
            final PowerManager.WakeLock finalWakeLock = wakeLock;
            android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (finalWakeLock.isHeld()) finalWakeLock.release();
                }
            }, 55 * 1000);
        }
    }
    
    private void startAlarmSound(Context context) {
        try {
            stopAlarmSound();
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            currentRingtone = RingtoneManager.getRingtone(context, alarmUri);
            if (currentRingtone != null) {
                currentRingtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void startVibrate(Context context) {
        try {
            stopVibrate();
            currentVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (currentVibrator != null && currentVibrator.hasVibrator()) {
                long[] pattern = {0, 500, 500, 500, 500, 500};
                currentVibrator.vibrate(pattern, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void stopAlarmSound() {
        if (currentRingtone != null && currentRingtone.isPlaying()) {
            currentRingtone.stop();
        }
        currentRingtone = null;
    }
    
    public static void stopVibrate() {
        if (currentVibrator != null) {
            currentVibrator.cancel();
        }
        currentVibrator = null;
    }
    
    public static void stopAll() {
        stopAlarmSound();
        stopVibrate();
    }
    
    private void showFullScreenNotification(Context context, String label) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("日历闹钟提醒");
            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        
        // 点击通知停止响铃并打开界面
        Intent alertIntent = new Intent(context, AlarmAlertActivity.class);
        alertIntent.putExtra("label", label);
        alertIntent.putExtra("stop_alarm", true);
        alertIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, alertIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label)
            .setContentText("闹钟响了！点击停止")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true);
        
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
