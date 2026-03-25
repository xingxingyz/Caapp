package com.example.calendaralarm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmItem {
    private long timeInMillis;
    private String label;

    public AlarmItem(long timeInMillis, String label) {
        this.timeInMillis = timeInMillis;
        this.label = label;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public String getLabel() {
        return label;
    }

    public String getTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);
        return sdf.format(new Date(timeInMillis));
    }

    public boolean isExpired() {
        return timeInMillis < System.currentTimeMillis();
    }
}