package com.example.calendaralarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class AlarmAdapter extends BaseAdapter {

    private Context context;
    private List<AlarmItem> alarms;

    public AlarmAdapter(Context context, List<AlarmItem> alarms) {
        this.context = context;
        this.alarms = alarms;
    }

    @Override
    public int getCount() {
        return alarms.size();
    }

    @Override
    public Object getItem(int position) {
        return alarms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false);
            holder = new ViewHolder();
            holder.timeText = convertView.findViewById(R.id.itemTimeText);
            holder.labelText = convertView.findViewById(R.id.itemLabelText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AlarmItem alarm = alarms.get(position);
        holder.timeText.setText(alarm.getTimeString());
        holder.labelText.setText(alarm.getLabel());

        return convertView;
    }

    static class ViewHolder {
        TextView timeText;
        TextView labelText;
    }
}