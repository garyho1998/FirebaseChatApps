package com.example.firebasechatapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.firebasechatapps.AlarmController;
import com.example.firebasechatapps.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton SendMessageButton, DelyButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private FloatingActionButton mcalendarButton;


    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    AlarmController alarmController;
    final String TAG = "GroupChatActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        InitializeFields();
        GetUserInfo();
        alarmController = new AlarmController();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfoToDatabase(null);
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        DelyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = userMessageInput.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    showDateTimeDialogAndSend();
                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }
        });

        mcalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(GroupChatActivity.this, CalendarActivity.class);
//                calendarIntent.putExtra("type" , valueToPass);
                calendarIntent.putExtra("groupName", currentGroupName);
                finish();
                startActivity(calendarIntent);


            }
        });
    }

    private void showDateTimeDialogAndSend() {
        final Calendar calendar = Calendar.getInstance();
        final Date date;
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm");
                        SaveMessageInfoToDatabase(calendar);
                    }
                };
                new TimePickerDialog(GroupChatActivity.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }
        };
        new DatePickerDialog(GroupChatActivity.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.child("Message").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
                }
            }
            public void onChildRemoved(DataSnapshot dataSnapshot){
                if (dataSnapshot.exists()) {
                    DisplayMessages(dataSnapshot);
                }
            };
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            public void onCancelled(DatabaseError databaseError) {}
        });

        GroupNameRef.child("DelayMessage").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    Log.i(TAG, "onChildAdded, SetAlarmFromDelayMessage");
                    SetAlarmFromDelayMessage(dataSnapshot);
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    Log.i(TAG, "onChildChanged, SetAlarmFromDelayMessage");
                    SetAlarmFromDelayMessage(dataSnapshot);
                }
            }
            public void onChildRemoved(DataSnapshot dataSnapshot){};
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        DelyButton = (ImageButton) findViewById(R.id.send_delay_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
        mcalendarButton = (FloatingActionButton) findViewById(R.id.calendarButton);

    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        System.out.println("DisplayMessages: "+dataSnapshot.getKey());
        //String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
        String id = dataSnapshot.getKey();
        String date = (String) dataSnapshot.child("date").getValue();
        String message = (String) dataSnapshot.child("message").getValue();
        String name = (String) dataSnapshot.child("name").getValue();
        String time = (String) dataSnapshot.child("time").getValue();
        displayTextMessages.append(id+ " :\n" +date + " :\n" + message + "\n" + name + "     " + time + "\n\n\n");
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    private void SetAlarmFromDelayMessage(DataSnapshot dataSnapshot) {
        Log.i(TAG, "SetAlarmFromDelayMessage, Iterator");
        String id = dataSnapshot.getKey();
        String date = (String) dataSnapshot.child("date").getValue();
        String message = (String) dataSnapshot.child("message").getValue();
        String name = (String) dataSnapshot.child("name").getValue();
        String time = (String) dataSnapshot.child("time").getValue();
        Long displayTimestamp = (Long) dataSnapshot.child("displayTimestamp").getValue();

        if((displayTimestamp - System.currentTimeMillis())<=0){
            Map<String, Object> messageObject = new HashMap<String, Object>();
            messageObject.put("timestamp", displayTimestamp);
            messageObject.put("date", date);
            messageObject.put("message", message);
            messageObject.put("name", name);
            messageObject.put("time", time);

            Map<String, Object> childUpdates = new HashMap<>();
            Map<String, Object> childDelete = new HashMap<>();
            childUpdates.put(id, messageObject);
            GroupNameRef.child("Message").updateChildren(childUpdates);
            childDelete.put(id, null);
            GroupNameRef.child("DelayMessage").updateChildren(childDelete);

        }else{
            alarmController.addAlarm(this, id, displayTimestamp, currentGroupName);
        }

    }

    private void GetUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SaveMessageInfoToDatabase(Calendar calendar) {
        String message = userMessageInput.getText().toString();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        } else {
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            Calendar now = Calendar.getInstance();
            currentDate = currentDateFormat.format(now.getTime());
            currentTime = currentTimeFormat.format(now.getTime());
            if (calendar == null) {
                DatabaseReference GroupMessageRef = GroupNameRef.child("Message");
                String messageKey = GroupMessageRef.push().getKey();
                GroupMessageKeyRef = GroupMessageRef.child(messageKey);

                HashMap<String, Object> messageObject = new HashMap<>();
                messageObject.put("name", currentUserName);
                messageObject.put("type", "normal");
                messageObject.put("message", message);
                messageObject.put("date", currentDate);
                messageObject.put("time", currentTime);
                messageObject.put("timestamp", now.getTimeInMillis());
                GroupMessageKeyRef.updateChildren(messageObject);
            } else { //DelayChat
                DatabaseReference GroupDelayRef = GroupNameRef.child("DelayMessage");
                String messageKey = GroupDelayRef.push().getKey();
                GroupMessageKeyRef = GroupDelayRef.child(messageKey);

                HashMap<String, Object> messageObject = new HashMap<>();
                messageObject.put("name", currentUserName);
                messageObject.put("message", message);
                messageObject.put("type", "delay");
                messageObject.put("date", currentDateFormat.format(now.getTime()));
                messageObject.put("time", currentTimeFormat.format(now.getTime()));
                messageObject.put("displayDate", currentDateFormat.format(calendar.getTime()));
                messageObject.put("displayTime", currentTimeFormat.format(calendar.getTime()));
                messageObject.put("displayTimestamp", calendar.getTimeInMillis());
                GroupMessageKeyRef.updateChildren(messageObject);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()== R.id.group_add_members_option){
            Intent intent = new Intent(GroupChatActivity.this, AddMemberActivity.class);
            startActivity(intent);
        }
        return true;
    }
}

