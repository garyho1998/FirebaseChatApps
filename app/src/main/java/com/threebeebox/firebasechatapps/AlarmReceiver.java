package com.threebeebox.firebasechatapps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef, NotificationRef;
    final String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(TAG, "onReceive");
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:mywakelocktag");
        wl.acquire();

        //Update firebase
        final String messageID = intent.getStringExtra("messageID");
        final String groupID = intent.getStringExtra("groupID");

        mAuth = FirebaseAuth.getInstance();
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID);

        GroupNameRef.child("DelayMessage").child(messageID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupNameRef.child("Message").child(messageID).setValue(dataSnapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        GroupNameRef.child("Member").addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                if(snapshot.exists()){
                                    HashMap<String, String> chatNotification = new HashMap<>();
                                    chatNotification.put("from", groupID);
                                    chatNotification.put("type", "groupChat");
                                    NotificationRef.child(snapshot.getKey()).push().setValue(chatNotification);
                                }
                            }
                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                if(snapshot.exists()){
                                    HashMap<String, String> chatNotification = new HashMap<>();
                                    chatNotification.put("from", groupID);
                                    chatNotification.put("type", "chat");
                                    NotificationRef.child(snapshot.getKey()).push().setValue(chatNotification);
                                }
                            }
                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        GroupNameRef.child("DelayMessage").child(messageID).removeValue();
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());


            }
        });
        wl.release();
    }
}