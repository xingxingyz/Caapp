package com.example.calendaralarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
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

    @Override
    public void onReceive(Context context, Intent intent) {
        String label = intent.getStringExtra("label");
        if (label == null) label = "闹钟";
        
        // 唤醒屏幕
        wakeUpScreen(context);
        
        // 播放铃声
        playAlarmSound(context);
        
        // 震动
        vibrate(context);
        
        // 显示通知
        showNotification(context, label);
    }
    
    private void wakeUpScreen(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = powerManager.isInteractive();
        
        if (!isScreenOn) {
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,
                "CalendarAlarm::WakeLock"
            );
            wakeLock.acquire(10 * 1000); // 亮屏10秒
        }
    }
    
    private void playAlarmSound(Context context) {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 500, 500, 500, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(pattern, -1);
            }
        }
    }
    
    private void showNotification(Context context, String label) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // 创建通知渠道（Android 8+）- 最高优先级
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("日历闹钟提醒");
            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            
            // 设置声音属性
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
            channel.setSound(alarmSound, audioAttributes);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 500, 500, 500, 500});
            
            notificationManager.createNotificationChannel(channel);
        }
        
        // 创建打开应用的 Intent
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, openIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 构建通知
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label)
            .setContentText("时间到了！")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setVibrate(new long[]{0, 500, 500, 500, 500, 500})
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true);
        
        // 显示通知
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }
}