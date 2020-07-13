package com.example.firebasechatapps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;

public class AlarmController {
    public ArrayList<AlarmManager> alarmManagerlist;
    int num = 0;
    public void addAlarm(Context context, String id){
        Intent intent = new Intent(context, Alarm.class);
        intent.putExtra("id", id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 23131+num, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ (3 * 1000), pendingIntent);
        Toast.makeText(context, num+ ":Alarm ("+id+") set in " + 3 + " seconds",Toast.LENGTH_SHORT).show();
        num++;
    }
}
