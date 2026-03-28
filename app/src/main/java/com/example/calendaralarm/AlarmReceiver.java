package com.example.calendaralarm;

import android.app.Notification;
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
        // 获取唤醒锁，确保设备唤醒 - 使用 ACQUIRE_CAUSES_WAKEUP 点亮屏幕
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        
        PowerManager.WakeLock wakeLock = null;
        if (powerManager != null) {
            // 使用 SCREEN_BRIGHT_WAKE_LOCK + ACQUIRE_CAUSES_WAKEUP 来点亮屏幕
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE,
                WAKE_LOCK_TAG
            );
            wakeLock.acquire(60 * 1000L); // 保持唤醒60秒
        }
        
        final String label = intent.getStringExtra("label") != null 
            ? intent.getStringExtra("label") 
            : "闹钟";
        
        // 先显示全屏通知（Android 10+ 推荐方式）
        showFullScreenNotification(context, label);
        
        // 延迟启动 Activity（确保屏幕已唤醒）
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            startAlarmActivity(context, label);
        }, 500);
        
        // 延迟释放唤醒锁 - 使用类成员变量保存引用
        scheduleWakeLockRelease(wakeLock);
    }
    
    private void scheduleWakeLockRelease(final PowerManager.WakeLock wakeLock) {
        if (wakeLock == null) return;
        
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        }, 55 * 1000);
    }
    
    private void startAlarmActivity(Context context, String label) {
        // 启动全屏闹钟界面
        Intent alertIntent = new Intent(context, AlarmAlertActivity.class);
        alertIntent.putExtra("label", label);
        
        // 关键标志组合，确保在任何状态下都能启动
        alertIntent.setFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_CLEAR_TOP |
            Intent.FLAG_ACTIVITY_CLEAR_TASK |
            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
            Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT |
            Intent.FLAG_FROM_BACKGROUND
        );
        
        // Android 8.0+ 添加额外标志
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
        }
        
        // Android 10+ 添加
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        
        try {
            // 检查是否在后台限制状态
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android 9+ 需要确保我们能启动 Activity
                context.startActivity(alertIntent);
            } else {
                context.startActivity(alertIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 如果启动失败，至少确保通知还在响
        }
    }
    
    private void showFullScreenNotification(Context context, String label) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // 创建高优先级通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("日历闹钟提醒");
            channel.setBypassDnd(true); // 绕过勿扰模式
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); // 锁屏显示
            notificationManager.createNotificationChannel(channel);
        }
        
        // 创建打开全屏界面的 Intent
        Intent alertIntent = new Intent(context, AlarmAlertActivity.class);
        alertIntent.putExtra("label", label);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, alertIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 构建通知 - 使用全屏意图
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label)
            .setContentText("闹钟响了！点击查看")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // 关键：全屏意图
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 锁屏可见
            .setOngoing(true); // 持续显示
        
        // 显示通知
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }
}