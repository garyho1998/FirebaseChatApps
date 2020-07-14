package com.example.firebasechatapps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class AlarmController {
    public ArrayList<String> alarmIDlist = new ArrayList<String>();
    int num = 0;
    static final String TAG = "AlarmController";

    public void addAlarm(Context context, String id, Long timestamp, String groupName){
        if(alarmIDlist.contains(id)){
            Log.i(TAG,"alarmIDlist.contains(id)");
        }else{
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("id", id);
            intent.putExtra("groupName", groupName);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 23131+num, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            Long alarmTime = timestamp;
            //Long alarmTime = System.currentTimeMillis()+2000;
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            Toast.makeText(context, num+ ":Alarm ("+id+") set in " + (alarmTime - System.currentTimeMillis())/1000 + " seconds",Toast.LENGTH_SHORT).show();
            num++;
            alarmIDlist.add(id);
        }
    }
}
