package com.example.calendaralarm;

import android.app.KeyguardManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmAlertActivity extends AppCompatActivity {
    
    // 改为非 static，每个实例独立
    private Ringtone currentRingtone;
    private Vibrator currentVibrator;
    
    // 用于标记是否已经停止，防止重复
    private boolean isStopped = false;
    
    private TextView alarmLabelText;
    private TextView alarmTimeText;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 确保只有一个实例运行
        if (isTaskRoot() == false) {
            // 如果已经有实例在运行，结束之前的
            finish();
            return;
        }
        
        // 关键：必须在 setContentView 之前设置窗口标志
        setupWindowFlags();
        
        // 唤醒屏幕
        wakeUpScreen();
        
        setContentView(R.layout.activity_alarm_alert);
        
        alarmLabelText = findViewById(R.id.alarmLabelText);
        alarmTimeText = findViewById(R.id.alarmTimeText);
        stopButton = findViewById(R.id.stopButton);
        
        // 获取闹钟信息
        String label = getIntent().getStringExtra("label");
        if (label == null) label = "闹钟";
        
        alarmLabelText.setText(label);
        alarmTimeText.setText("时间到了！");
        
        // 播放铃声
        playAlarmSound();
        
        // 震动
        vibrate();
        
        // 停止按钮
        stopButton.setOnClickListener(v -> {
            stopAndFinish();
        });
    }
    
    private void setupWindowFlags() {
        // 全屏显示，锁屏也能显示 - 兼容所有 Android 版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        
        // 添加窗口标志
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        );
        
        // Android 8.0+ 设置窗口类型为闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setShowWhenLocked(true);
        }
    }
    
    private void wakeUpScreen() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) return;
        
        boolean isScreenOn = powerManager.isInteractive();
        
        if (!isScreenOn) {
            // 使用 ACQUIRE_CAUSES_WAKEUP 强制点亮屏幕
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | 
                PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "CalendarAlarm::AlarmWakeLock"
            );
            wakeLock.acquire(60 * 1000); // 亮屏60秒
        }
        
        // 解锁键盘（Android 8.0+）
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
    
    private void playAlarmSound() {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            currentRingtone = RingtoneManager.getRingtone(this, alarmUri);
            if (currentRingtone != null) {
                currentRingtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void vibrate() {
        currentVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (currentVibrator != null && currentVibrator.hasVibrator()) {
            long[] pattern = {0, 500, 500, 500, 500, 500};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                currentVibrator.vibrate(pattern, 0); // 重复震动
            }
        }
    }
    
    private void stopAlarm() {
        if (isStopped) return;
        isStopped = true;
        
        // 停止铃声
        if (currentRingtone != null && currentRingtone.isPlaying()) {
            currentRingtone.stop();
        }
        
        // 停止震动
        if (currentVibrator != null) {
            currentVibrator.cancel();
        }
    }
    
    private void stopAndFinish() {
        stopAlarm();
        finish();
    }
    
    @Override
    protected void onDestroy() {
        stopAlarm();
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        stopAndFinish();
    }
}