package com.threebeebox.firebasechatapps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.threebeebox.firebasechatapps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton SendMessageButton, DelyButton, SendFilesButton;
    private EditText userMessageInput;
    private FloatingActionButton mcalendarButton;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private GroupMessageAdapter gpMsgAdapter;
    private RecyclerView userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef, UserMessageKeyRef, NotificationRef;

    private String currentGroupName, currentGroupID, currentUserID, currentUserName, currentDate, currentTime;
    public Boolean isAdamin;
    private String saveCurrentTime, saveCurrentDate;
    private String myUri = "";
    private ProgressDialog loadingBar;
    private static final int GalleryPick = 1;

    AlarmController alarmController;
    final String TAG = "GroupChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupID = getIntent().getExtras().get("groupID").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupID);
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        GetUserInfo();
        InitializeFields();
        alarmController = new AlarmController();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfoToDatabase(null);
                userMessageInput.setText("");
            }
        });

        DelyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = userMessageInput.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    showDateTimeDialogAndSend();
                }
            }
        });

        mcalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(GroupChatActivity.this, CalendarActivity.class);
                calendarIntent.putExtra("type", "group");
                calendarIntent.putExtra("groupName", currentGroupName);
                calendarIntent.putExtra("groupID", currentGroupID);
                startActivity(calendarIntent);
            }
        });

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(GroupChatActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri ImageUri = data.getData();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Sending Image");
                loadingBar.setMessage("Please wait, your image is uploading...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resultUri = result.getUri();
                File filePathUri = new File(resultUri.getPath());
                Bitmap bitmap = null;
                try {
                    bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(filePathUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] bytes = byteArrayOutputStream.toByteArray();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                DatabaseReference userMessageKeyRef = GroupNameRef.child("Message").push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");
                filePath.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map messageTextBody = new HashMap();
                                messageTextBody.put("message", uri.toString());
                                messageTextBody.put("name", currentUserName);
                                messageTextBody.put("type", "image");
                                //name is the sender user name, here is the sender user id
                                messageTextBody.put("from", currentUserID);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put("/Message/" + messagePushID, messageTextBody);

                                GroupNameRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            loadingBar.dismiss();
                                            Toast.makeText(GroupChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                        } else {
                                            loadingBar.dismiss();
                                            Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }

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
        messagesList.clear();

        userMessagesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && mcalendarButton.getVisibility() == View.VISIBLE) {
                    mcalendarButton.hide();
                } else if (dy < 0 && mcalendarButton.getVisibility() != View.VISIBLE) {
                    mcalendarButton.show();
                }
            }
        });

        GroupNameRef.child("Message").orderByChild("displayTimestamp")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.child("type").getValue().equals("delay")){
                            DelayMsg messages = dataSnapshot.getValue(DelayMsg.class);
                            if(messages!=null){
                                if (!messagesList.contains(messages.toParent())) {
                                    messagesList.add(messages.toParent());
                                    gpMsgAdapter.notifyDataSetChanged();
                                }
                                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                            }
                        }else{
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            if(messages!=null){
                                if (!messagesList.contains(messages)) {
                                    messagesList.add(messages);
                                    gpMsgAdapter.notifyDataSetChanged();
                                }
                                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        GroupNameRef.child("DelayMessage").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
//                    Log.i(TAG, "onChildAdded, SetAlarmFromDelayMessage");
                    SetAlarmFromDelayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
//                    Log.i(TAG, "onChildChanged, SetAlarmFromDelayMessage");
                    SetAlarmFromDelayMessage(dataSnapshot);
                }
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            ;

            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        messagesList.clear();
    }

    private void InitializeFields() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle(currentGroupName);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        DelyButton = (ImageButton) findViewById(R.id.send_delay_button);
        userMessageInput = (EditText) findViewById(R.id.input_message);
        mcalendarButton = (FloatingActionButton) findViewById(R.id.calendarButton);

        // Log.d("myTag", "currentUserName passed to msgAdapter: " + currentUserName + "     currentUserID: " + currentUserID);
        gpMsgAdapter = new GroupMessageAdapter(this, currentGroupName, messagesList, currentUserID);
        userMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(gpMsgAdapter);

        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        messagesList.clear();
    }


    private void SetAlarmFromDelayMessage(DataSnapshot dataSnapshot) {
        final String messageID = dataSnapshot.getKey();
        Long displayTimestamp = (Long) dataSnapshot.child("displayTimestamp").getValue();

        if ((displayTimestamp - System.currentTimeMillis()) <= 0) {
            GroupNameRef.child("DelayMessage").child(messageID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GroupNameRef.child("Message").child(messageID).setValue(dataSnapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            GroupNameRef.child("DelayMessage").child(messageID).removeValue();
                        }
                    });
                }
                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
        } else {
            alarmController.addAlarm(this, messageID, displayTimestamp, currentGroupID);
        }
    }

    private void GetUserInfo() {
        UsersRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
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

            HashMap<String, Object> messageObject = new HashMap<>();
            messageObject.put("name", currentUserName);
            messageObject.put("message", message);
            messageObject.put("from", currentUserID);
            messageObject.put("date", currentDate);
            messageObject.put("time", currentTime);
            messageObject.put("timestamp", now.getTimeInMillis());

            if (calendar == null) {
                DatabaseReference GroupMessageRef = GroupNameRef.child("Message");
                String messageKey = GroupMessageRef.push().getKey();
                GroupMessageKeyRef = GroupMessageRef.child(messageKey);

                messageObject.put("messageID", messageKey);
                messageObject.put("type", "normal");
                messageObject.put("date", currentDate);
                messageObject.put("time", currentTime);
                messageObject.put("displayDate", currentDate);
                messageObject.put("displayTime", currentTime);
                messageObject.put("displayTimestamp", now.getTimeInMillis());
                GroupMessageKeyRef.updateChildren(messageObject).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // find id of all group members, and push a notification...
                            GroupNameRef.child("Member")
                                    .addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                            String memberID = dataSnapshot.getKey();
                                            Log.d("notif", "memberID: " + dataSnapshot.getRef().toString() + "\nkey: " + dataSnapshot.getKey());

                                            if (!memberID.equals(currentUserID)) {
                                                HashMap<String, String> chatNotification = new HashMap<>();
                                                chatNotification.put("from", currentGroupID);
                                                chatNotification.put("type", "groupChat");

                                                NotificationRef.child(memberID).push()
                                                        .setValue(chatNotification)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(GroupChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                        }

                                        @Override
                                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                                        }

                                        @Override
                                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });

                        } else {
                            Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                        userMessageInput.setText("");
                    }
                });
            } else { //DelayChat
                DatabaseReference GroupDelayRef = GroupNameRef.child("DelayMessage");
                String messageKey = GroupDelayRef.push().getKey();
                GroupMessageKeyRef = GroupDelayRef.child(messageKey);

                messageObject.put("messageID", messageKey);
                messageObject.put("type", "delay");
                messageObject.put("displayDate", currentDateFormat.format(calendar.getTime()));
                messageObject.put("displayTime", currentTimeFormat.format(calendar.getTime()));
                messageObject.put("displayTimestamp", calendar.getTimeInMillis());
                GroupMessageKeyRef.updateChildren(messageObject);

                DatabaseReference UserDelayRef = UsersRef.child(currentUserID).child("DelayMessage");
                UserDelayRef.child(messageKey).setValue("");
                UserDelayRef.child(messageKey).child("type").setValue("group");
                UserDelayRef.child(messageKey).child("ref").setValue(currentGroupID);
                UserDelayRef.child(messageKey).child("displayDate").setValue(currentDateFormat.format(calendar.getTime()));
                UserDelayRef.child(messageKey).child("displayTimestamp").setValue(calendar.getTimeInMillis());
            }
            userMessageInput.setText("");
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
        if (item.getItemId() == R.id.group_add_members_option) {
            Intent intent = new Intent(GroupChatActivity.this, AddMemberActivity.class);
            intent.putExtra("groupName", currentGroupName);
            intent.putExtra("groupID", currentGroupID);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.group_info_option) {
            Intent intent = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
            intent.putExtra("groupName", currentGroupName);
            intent.putExtra("groupID", currentGroupID);
            startActivity(intent);
        }
        return true;
    }
}

