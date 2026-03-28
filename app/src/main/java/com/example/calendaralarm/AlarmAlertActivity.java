package com.example.calendaralarm;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
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

        // 判断来源：
        // - from_alarm=true: 闹钟触发自动打开的，已经在响铃了
        // - from_notification=true: 用户点击通知打开的，不响铃只显示界面
        boolean fromAlarm = getIntent().getBooleanExtra("from_alarm", false);
        boolean fromNotification = getIntent().getBooleanExtra("from_notification", false);
        
        // 如果是从通知点击过来的，确保不响铃（停止当前可能存在的响铃）
        if (fromNotification) {
            AlarmReceiver.stopAll();
        }
        
        // 如果是从闹钟触发的，确保正在响铃
        if (fromAlarm) {
            // 响铃已经在 Receiver 中启动了，这里不需要额外操作
        }

        // 停止按钮
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmReceiver.stopAll();
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
        
        WindowManager.LayoutParams params = getWindow().getAttributes();
        
        // 锁屏显示需要特定的窗口类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8+ 使用 SYSTEM_ALERT 类型支持在锁屏上显示
            if (Settings.canDrawOverlays(this)) {
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }
        }
        
        getWindow().setAttributes(params);
        
        // 关键：添加 FLAG_DISMISS_KEYGUARD 确保能显示在锁屏上
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
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
        // 只有在不是从通知点击过来的情况下，销毁时才停止响铃
        // 这样可以避免用户点击通知查看时意外停止响铃
        boolean fromNotification = getIntent().getBooleanExtra("from_notification", false);
        if (!fromNotification) {
            AlarmReceiver.stopAll();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // 返回键不停止响铃，只有停止按钮可以停止
        super.onBackPressed();
    }
}
