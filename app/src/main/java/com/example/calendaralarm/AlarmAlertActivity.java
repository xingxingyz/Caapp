package com.example.calendaralarm;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmAlertActivity extends AppCompatActivity {
    
    private TextView alarmLabelText;
    private TextView alarmTimeText;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setupWindowFlags();
        wakeUpScreen();
        
        setContentView(R.layout.activity_alarm_alert);
        
        alarmLabelText = findViewById(R.id.alarmLabelText);
        alarmTimeText = findViewById(R.id.alarmTimeText);
        stopButton = findViewById(R.id.stopButton);
        
        String label = getIntent().getStringExtra("label");
        if (label == null) label = "闹钟";
        
        alarmLabelText.setText(label);
        alarmTimeText.setText("时间到了！");
        
        // 检查是否是从通知点击过来的（需要停止响铃）
        boolean shouldStopAlarm = getIntent().getBooleanExtra("stop_alarm", false);
        if (shouldStopAlarm) {
            AlarmReceiver.stopAll();
            NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1001);
        }
        
        // 停止按钮
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmReceiver.stopAll();
                NotificationManager notificationManager = 
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1001);
                finish();
            }
        });
    }
    
    private void setupWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        );
    }
    
    private void wakeUpScreen() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) return;
        
        if (!powerManager.isInteractive()) {
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | 
                PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "CalendarAlarm::AlarmWakeLock"
            );
            wakeLock.acquire(60 * 1000);
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (keyguardManager != null && keyguardManager.isKeyguardLocked()) {
                    keyguardManager.requestDismissKeyguard(this, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        AlarmReceiver.stopAll();
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        AlarmReceiver.stopAll();
        super.onBackPressed();
    }
}
