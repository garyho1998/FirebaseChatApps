package com.threebeebox.firebasechatapps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;

public class IndAlarmController {
    public ArrayList<String> alarmIDlist = new ArrayList<String>();
    int num = 0;
    static final String TAG = "IndAlarmController";

    public void addAlarm(Context context, String messageID, Long timestamp, String senderID, String receiverID){
        if(alarmIDlist.contains(messageID)){
        }else{
            Intent intent = new Intent(context, IndAlarmReceiver.class);
            intent.putExtra("messageID", messageID);
            intent.putExtra("senderID", senderID);
            intent.putExtra("receiverID", receiverID);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 23131+num, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            Long alarmTime = timestamp;
            //Long alarmTime = System.currentTimeMillis()+3000;
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            Toast.makeText(context, num+ ":Alarm ("+messageID+") set in " + (alarmTime - System.currentTimeMillis())/1000 + " seconds",Toast.LENGTH_SHORT).show();
            num++;
            alarmIDlist.add(messageID);
        }
    }
}
