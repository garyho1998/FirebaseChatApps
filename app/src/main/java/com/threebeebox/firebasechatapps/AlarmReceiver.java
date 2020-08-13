package com.threebeebox.firebasechatapps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AlarmReceiver extends BroadcastReceiver
{
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;
    final String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent)
    {
//        Log.i(TAG, "onReceive");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:mywakelocktag");
        wl.acquire();

        //Update firebase
        String id = intent.getStringExtra("id");
        String groupName = intent.getStringExtra("groupName");

        mAuth = FirebaseAuth.getInstance();
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupName);
        GroupNameRef.child("DelayMessage").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UpdateFirebaseFromAlarm(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        wl.release();
    }
    private void UpdateFirebaseFromAlarm(DataSnapshot dataSnapshot) {
//        Log.i(TAG, "UpdateFirebaseFromAlarm");
        String id = dataSnapshot.getKey();
        String date = (String) dataSnapshot.child("date").getValue();
        String message = (String) dataSnapshot.child("message").getValue();
        String name = (String) dataSnapshot.child("name").getValue();
        String time = (String) dataSnapshot.child("time").getValue();
        Long displayTimestamp = (Long) dataSnapshot.child("displayTimestamp").getValue();
//        Log.i(TAG, "id: "+id+", message:"+message);
        if(message!=null){
            Map<String, Object> messageObject = new HashMap<String, Object>();
            messageObject.put("timestamp", displayTimestamp);
            messageObject.put("date", date);
            messageObject.put("message", message);
            messageObject.put("name", name);
            messageObject.put("time", time);
//            Log.i(TAG, "messageObject: "+convertWithIteration(messageObject));

            Map<String, Object> childUpdates = new HashMap<>();
            Map<String, Object> childDelete = new HashMap<>();
            childUpdates.put(id, messageObject);
            GroupNameRef.child("Message").updateChildren(childUpdates);
            childDelete.put(id, null);
            GroupNameRef.child("DelayMessage").updateChildren(childDelete);
        }
    }

    public void setAlarm(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
    public String convertWithIteration(Map<String, ?> map) {
        StringBuilder mapAsString = new StringBuilder("{");
        for (String key : map.keySet()) {
            mapAsString.append(key + "=" + map.get(key) + ", ");
        }
        mapAsString.delete(mapAsString.length()-2, mapAsString.length()).append("}");
        return mapAsString.toString();
    }
}