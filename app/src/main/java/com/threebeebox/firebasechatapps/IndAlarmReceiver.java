package com.threebeebox.firebasechatapps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class IndAlarmReceiver extends BroadcastReceiver {
    private FirebaseAuth mAuth;
    private DatabaseReference ChatRef, SndChatRef, RcvChatRef, NotificationRef;
    final String TAG = "IndAlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(TAG, "onReceive");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:mywakelocktag");
        wl.acquire();

        String messageID = intent.getStringExtra("messageID");
        String senderID = intent.getStringExtra("senderID");
        String receiverID = intent.getStringExtra("receiverID");

        mAuth = FirebaseAuth.getInstance();
        ChatRef = FirebaseDatabase.getInstance().getReference().child("Messages");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        ChatRef.child(senderID).child(receiverID).child("Delay").child(messageID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange");
                UpdateFirebaseFromAlarm(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        wl.release();
    }
    private void UpdateFirebaseFromAlarm(final DataSnapshot dataSnapshot) {
        String id = dataSnapshot.getKey();
        String date = (String) dataSnapshot.child("displayDate").getValue();
        String message = (String) dataSnapshot.child("message").getValue();
        String time = (String) dataSnapshot.child("displayTime").getValue();
        String from = (String) dataSnapshot.child("from").getValue();
        String to = (String) dataSnapshot.child("to").getValue();
        Long displayTimestamp = (Long) dataSnapshot.child("displayTimestamp").getValue();

        SndChatRef = ChatRef.child(from).child(to);
        RcvChatRef = ChatRef.child(to).child(from);

        if(message!=null){
            Map<String, Object> messageObject = new HashMap<String, Object>();
            messageObject.put("timestamp", displayTimestamp);
            messageObject.put("date", date);
            messageObject.put("message", message);
            messageObject.put("time", time);
            messageObject.put("from", from);
            messageObject.put("to", to);
            messageObject.put("type", "text");
            messageObject.put("messageID", id);

            Map<String, Object> childUpdates = new HashMap<>();
            Map<String, Object> childDelete = new HashMap<>();
            childUpdates.put(id, messageObject);
            SndChatRef.child("Chat").updateChildren(childUpdates);
            RcvChatRef.child("Chat").updateChildren(childUpdates);
            childDelete.put(id, null);
            SndChatRef.child("Delay").updateChildren(childDelete);
            RcvChatRef.child("Delay").updateChildren(childDelete);

            HashMap<String, String> chatNotification = new HashMap<>();
            chatNotification.put("from", from);
            chatNotification.put("type", "chat");
            NotificationRef.child(to).push().setValue(chatNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    dataSnapshot.getRef().removeValue();
                }
            });
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
