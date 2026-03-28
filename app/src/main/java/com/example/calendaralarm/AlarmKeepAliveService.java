package com.example.calendaralarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * 前台服务 - 保持应用在后台运行，确保闹钟能正常触发
 */
public class AlarmKeepAliveService extends Service {
    
    private static final String CHANNEL_ID = "keep_alive_channel";
    private static final int NOTIFICATION_ID = 2001;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification());
        
        // START_STICKY 确保服务被杀死后能自动重启
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "日历闹钟服务",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("保持闹钟服务运行");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("日历闹钟运行中")
            .setContentText("闹钟服务正在后台运行")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }
}
