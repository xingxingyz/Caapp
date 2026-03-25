package com.example.calendaralarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
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
        // 使用系统闹钟 API（最可靠的方式）
        Intent alarmIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
        alarmIntent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        alarmIntent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        alarmIntent.putExtra(AlarmClock.EXTRA_MESSAGE, label);
        alarmIntent.putExtra(AlarmClock.EXTRA_SKIP_UI, false); // 显示系统闹钟界面
        
        if (alarmIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(alarmIntent);
            
            // 保存到列表
            Calendar alarmCal = (Calendar) selectedCalendar.clone();
            alarmCal.set(Calendar.HOUR_OF_DAY, hour);
            alarmCal.set(Calendar.MINUTE, minute);
            
            AlarmItem alarm = new AlarmItem(alarmCal.getTimeInMillis(), label);
            alarmList.add(alarm);
            alarmList.sort((a, b) -> Long.compare(a.getTimeInMillis(), b.getTimeInMillis()));
            alarmAdapter.notifyDataSetChanged();
            saveAlarms();
            
            Toast.makeText(this, "已跳转到系统闹钟设置", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "未找到系统闹钟应用", Toast.LENGTH_LONG).show();
        }
    }

    private void deleteAlarm(int position) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 需要 SCHEDULE_EXACT_ALARM 权限
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "请允许设置精确闹钟", Toast.LENGTH_LONG).show();
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