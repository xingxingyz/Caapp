package com.example.calendaralarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView selectedDateText;
    private Button setAlarmButton;
    private ListView alarmListView;
    private List<AlarmItem> alarmList;
    private AlarmAdapter alarmAdapter;
    
    private Calendar selectedCalendar;
    private static final int REQUEST_ALARM_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initCalendar();
        initAlarmList();
        checkPermissions();
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        selectedDateText = findViewById(R.id.selectedDateText);
        setAlarmButton = findViewById(R.id.setAlarmButton);
        alarmListView = findViewById(R.id.alarmListView);

        setAlarmButton.setOnClickListener(v -> openSetAlarmDialog());
    }

    private void initCalendar() {
        selectedCalendar = Calendar.getInstance();
        updateSelectedDateText();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedCalendar.set(year, month, dayOfMonth);
            updateSelectedDateText();
        });
    }

    private void updateSelectedDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        String dateStr = sdf.format(selectedCalendar.getTime());
        selectedDateText.setText("已选择：" + dateStr);
    }

    private void initAlarmList() {
        alarmList = new ArrayList<>();
        alarmAdapter = new AlarmAdapter(this, alarmList);
        alarmListView.setAdapter(alarmAdapter);

        alarmListView.setOnItemClickListener((parent, view, position, id) -> {
            AlarmItem alarm = alarmList.get(position);
            showAlarmOptions(alarm, position);
        });

        loadAlarms();
    }

    private void showAlarmOptions(AlarmItem alarm, int position) {
        new AlertDialog.Builder(this)
                .setTitle("闹钟详情")
                .setMessage("时间：" + alarm.getTimeString() + "\n标签：" + alarm.getLabel())
                .setPositiveButton("删除", (dialog, which) -> {
                    deleteAlarm(position);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void openSetAlarmDialog() {
        Intent intent = new Intent(this, AlarmActivity.class);
        intent.putExtra("year", selectedCalendar.get(Calendar.YEAR));
        intent.putExtra("month", selectedCalendar.get(Calendar.MONTH));
        intent.putExtra("day", selectedCalendar.get(Calendar.DAY_OF_MONTH));
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            int hour = data.getIntExtra("hour", 0);
            int minute = data.getIntExtra("minute", 0);
            String label = data.getStringExtra("label");
            
            setSystemAlarm(hour, minute, label);
        }
    }

    private void setSystemAlarm(int hour, int minute, String label) {
        // 计算闹钟时间（包含正确日期）
        Calendar alarmCal = (Calendar) selectedCalendar.clone();
        alarmCal.set(Calendar.HOUR_OF_DAY, hour);
        alarmCal.set(Calendar.MINUTE, minute);
        alarmCal.set(Calendar.SECOND, 0);
        
        // 如果设置的时间已过，提醒用户
        if (alarmCal.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "选择的时间已过，请重新选择", Toast.LENGTH_LONG).show();
            return;
        }
        
        // 使用 AlarmManager 直接设置闹钟（不依赖系统闹钟应用）
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("label", label);
        
        // 使用唯一ID（基于时间戳）创建 PendingIntent
        int requestCode = (int) (alarmCal.getTimeInMillis() / 1000);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, requestCode, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 设置闹钟（精确时间）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 需要检查权限
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmCal.getTimeInMillis(),
                    pendingIntent
                );
            } else {
                // 引导用户去设置页面开启权限
                Toast.makeText(this, "请开启'允许设置精确闹钟'权限", Toast.LENGTH_LONG).show();
                Intent permIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                permIntent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(permIntent);
                return;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmCal.getTimeInMillis(),
                pendingIntent
            );
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmCal.getTimeInMillis(),
                pendingIntent
            );
        }
        
        // 保存到列表
        AlarmItem alarm = new AlarmItem(alarmCal.getTimeInMillis(), label);
        alarmList.add(alarm);
        alarmList.sort((a, b) -> Long.compare(a.getTimeInMillis(), b.getTimeInMillis()));
        alarmAdapter.notifyDataSetChanged();
        saveAlarms();
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);
        Toast.makeText(this, "闹钟已设置：" + sdf.format(alarmCal.getTime()), Toast.LENGTH_LONG).show();
    }

    private void deleteAlarm(int position) {
        AlarmItem alarm = alarmList.get(position);
        
        // 取消 AlarmManager 的闹钟
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        int requestCode = (int) (alarm.getTimeInMillis() / 1000);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, requestCode, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        
        alarmList.remove(position);
        alarmAdapter.notifyDataSetChanged();
        saveAlarms();
        Toast.makeText(this, "闹钟已删除", Toast.LENGTH_SHORT).show();
    }

    private void saveAlarms() {
        // 使用 SharedPreferences 保存闹钟列表
        StringBuilder sb = new StringBuilder();
        for (AlarmItem alarm : alarmList) {
            sb.append(alarm.getTimeInMillis()).append(",").append(alarm.getLabel()).append(";");
        }
        getSharedPreferences("alarms", MODE_PRIVATE)
                .edit()
                .putString("alarm_list", sb.toString())
                .apply();
    }

    private void loadAlarms() {
        String saved = getSharedPreferences("alarms", MODE_PRIVATE)
                .getString("alarm_list", "");
        
        if (!saved.isEmpty()) {
            String[] items = saved.split(";");
            for (String item : items) {
                if (!item.isEmpty()) {
                    String[] parts = item.split(",");
                    if (parts.length >= 2) {
                        try {
                            long time = Long.parseLong(parts[0]);
                            String label = parts[1];
                            alarmList.add(new AlarmItem(time, label));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            alarmList.sort((a, b) -> Long.compare(a.getTimeInMillis(), b.getTimeInMillis()));
            alarmAdapter.notifyDataSetChanged();
        }
    }

    private void checkPermissions() {
        // 检查通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                    REQUEST_ALARM_PERMISSION);
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 需要 SCHEDULE_EXACT_ALARM 权限
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "请允许设置精确闹钟", Toast.LENGTH_LONG).show();
            }
        }
        
        // 检查电池优化（关键！）
        checkBatteryOptimization();
    }
    
    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                // 引导用户关闭电池优化
                new AlertDialog.Builder(this)
                    .setTitle("需要关闭电池优化")
                    .setMessage("为保证闹钟能正常响铃，请关闭电池优化")
                    .setPositiveButton("去设置", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("稍后", null)
                    .show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ALARM_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要闹钟权限才能设置闹钟", Toast.LENGTH_LONG).show();
            }
        }
    }
}