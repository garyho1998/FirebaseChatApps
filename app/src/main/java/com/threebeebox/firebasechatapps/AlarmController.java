package com.threebeebox.firebasechatapps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;

public class AlarmController {
    public ArrayList<String> alarmIDlist = new ArrayList<String>();
    int num = 0;
    static final String TAG = "AlarmController";

    public void addAlarm(Context context, String messageID, Long timestamp, String groupID){
        if(alarmIDlist.contains(messageID)){
//            Log.i(TAG,"alarmIDlist.contains(id)");
        }else{
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("messageID", messageID);
            intent.putExtra("groupID", groupID);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 23131+num, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            Long alarmTime = timestamp;
            //Long alarmTime = System.currentTimeMillis()+2000;
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            num++;
            alarmIDlist.add(messageID);
        }
    }
}
