package com.example.calendaralarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    
    private static final String CHANNEL_ID = "alarm_channel";
    private static final String CHANNEL_NAME = "闹钟提醒";
    private static final String WAKE_LOCK_TAG = "AlarmReceiver::WakeLock";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取唤醒锁，确保设备唤醒
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE,
                WAKE_LOCK_TAG
            );
            wakeLock.acquire(10 * 60 * 1000L); // 保持唤醒10分钟
        }
        
        try {
            String label = intent.getStringExtra("label");
            if (label == null) label = "闹钟";
            
            // 启动全屏闹钟界面
            Intent alertIntent = new Intent(context, AlarmAlertActivity.class);
            alertIntent.putExtra("label", label);
            alertIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            
            // 添加额外标志确保在锁屏上显示
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
            }
            
            context.startActivity(alertIntent);
            
            // 同时显示通知（作为备用）
            showNotification(context, label);
            
        } finally {
            // 释放唤醒锁
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }
    
    private void showNotification(Context context, String label) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // 创建通知渠道（Android 8+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("日历闹钟提醒");
            notificationManager.createNotificationChannel(channel);
        }
        
        // 创建打开全屏界面的 Intent
        Intent alertIntent = new Intent(context, AlarmAlertActivity.class);
        alertIntent.putExtra("label", label);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, alertIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label)
            .setContentText("闹钟响了！点击查看")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true);
        
        // 显示通知
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }
}