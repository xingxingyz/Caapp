package com.example.calendaralarm;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {

    private TextView dateText;
    private EditText labelEdit;
    private Button selectTimeButton;
    private Button saveButton;
    private Button cancelButton;
    
    private int year, month, day;
    private int selectedHour = 8;
    private int selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // 获取传入的日期
        year = getIntent().getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR));
        month = getIntent().getIntExtra("month", Calendar.getInstance().get(Calendar.MONTH));
        day = getIntent().getIntExtra("day", Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        initViews();
        updateDateText();
    }

    private void initViews() {
        dateText = findViewById(R.id.dateText);
        labelEdit = findViewById(R.id.labelEdit);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        selectTimeButton.setOnClickListener(v -> showTimePicker());
        saveButton.setOnClickListener(v -> saveAlarm());
        cancelButton.setOnClickListener(v -> finish());

        // 默认时间
        updateTimeText();
    }

    private void updateDateText() {
        dateText.setText(String.format(Locale.CHINA, "%d年%d月%d日", year, month + 1, day));
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateTimeText();
                },
                selectedHour,
                selectedMinute,
                true
        );
        timePickerDialog.show();
    }

    private void updateTimeText() {
        selectTimeButton.setText(String.format(Locale.CHINA, "%02d:%02d", selectedHour, selectedMinute));
    }

    private void saveAlarm() {
        String label = labelEdit.getText().toString().trim();
        if (label.isEmpty()) {
            label = "闹钟";
        }

        // 检查时间是否已过
        Calendar alarmCal = Calendar.getInstance();
        alarmCal.set(year, month, day, selectedHour, selectedMinute, 0);
        
        if (alarmCal.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "不能设置过去的时间", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("hour", selectedHour);
        resultIntent.putExtra("minute", selectedMinute);
        resultIntent.putExtra("label", label);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}