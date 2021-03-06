package com.threebeebox.firebasechatapps;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
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

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef, NotificationRef;

    private ImageButton SendMessageButton, SendFilesButton, DelayBtn;
    private EditText MessageInputText;
    private FloatingActionButton mcalendarButton;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    IndAlarmController alarmController;

    private String saveCurrentTime, saveCurrentDate;
    private String myUri = "";
    private ProgressDialog loadingBar;
    private static final int GalleryPick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = (String) getIntent().getExtras().get("visit_image");

        IntializeControllers();
        DisplayLastSeen();
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
                        SendMessage(calendar);
                    }
                };
                new TimePickerDialog(ChatActivity.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }
        };
        new DatePickerDialog(ChatActivity.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    private void IntializeControllers()
    {
        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        DelayBtn = (ImageButton) findViewById(R.id.send_delay_button);
        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        SendFilesButton = (ImageButton) findViewById(R.id.send_files_btn);
        MessageInputText = (EditText) findViewById(R.id.input_message);
        mcalendarButton = (FloatingActionButton) findViewById(R.id.calendarButton);

        messageAdapter = new MessageAdapter(this, messageReceiverID, messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        loadingBar = new ProgressDialog(this);

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.user_icon).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendMessage(null);
            }
        });
        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ChatActivity.this);
            }
        });
        DelayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = MessageInputText.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    showDateTimeDialogAndSend();
                }else{
                    Toast.makeText(getApplicationContext(), "Please write your message...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mcalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(ChatActivity.this, CalendarActivity.class);
                calendarIntent.putExtra("type", "chat");
                calendarIntent.putExtra("sndID", messageSenderID);
                calendarIntent.putExtra("rcvID", messageReceiverID);
                calendarIntent.putExtra("name", messageReceiverName);
                startActivity(calendarIntent);
            }
        });
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        alarmController = new IndAlarmController();
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

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID+"/Chat";
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID+"/Chat";

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

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
                                messageTextBody.put("name", resultUri.getLastPathSegment());
                                messageTextBody.put("type", "image");
                                messageTextBody.put("from", messageSenderID);
                                messageTextBody.put("to", messageReceiverID);
                                messageTextBody.put("messageID", messagePushID);
                                messageTextBody.put("time", saveCurrentTime);
                                messageTextBody.put("date", saveCurrentDate);

                                Map messageBodyDetails = new HashMap();
                                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                                messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);
                                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            loadingBar.dismiss();
                                            Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                        } else {
                                            loadingBar.dismiss();
                                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
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


    private void DisplayLastSeen() {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")) {
                                userLastSeen.setText("online");
                            } else if (state.equals("offline")) {
                                userLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        } else {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();

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

//        messagesList.clear();
        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).child("Chat").orderByChild("timestamp")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.child("type").getValue().equals("delay")){
                            DelayMsg messages = dataSnapshot.getValue(DelayMsg.class);
                            if(messages!=null){
                                if (!messagesList.contains(messages.toParent())) {
                                    messagesList.add(messages.toParent());
                                    messageAdapter.notifyDataSetChanged();
                                }
                                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                            }
                        }else{
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            if(messages!=null){
                                if (!messagesList.contains(messages)) {
                                    messagesList.add(messages);
                                    messageAdapter.notifyDataSetChanged();
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


        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).child("Delay")
                .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    SetAlarmFromDelayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    SetAlarmFromDelayMessage(dataSnapshot);
                }
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) { }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void SetAlarmFromDelayMessage(DataSnapshot dataSnapshot) {
        String id = dataSnapshot.getKey();
        String date = (String) dataSnapshot.child("displayDate").getValue();
        String message = (String) dataSnapshot.child("message").getValue();
        String time = (String) dataSnapshot.child("displayTime").getValue();
        String from = (String) dataSnapshot.child("from").getValue();
        String to = (String) dataSnapshot.child("to").getValue();
        Long displayTimestamp = (Long) dataSnapshot.child("displayTimestamp").getValue();

        if ((displayTimestamp - System.currentTimeMillis()) <= 0) {
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

            RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).child("Chat").updateChildren(childUpdates);
            RootRef.child("Messages").child(messageReceiverID).child(messageSenderID).child("Chat").updateChildren(childUpdates);
            childDelete.put(id, null);
            RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).child("Delay").updateChildren(childDelete);
            RootRef.child("Messages").child(messageReceiverID).child(messageSenderID).child("Delay").updateChildren(childDelete);
        } else {
            alarmController.addAlarm(this, id, displayTimestamp, messageSenderID, messageReceiverID);
        }
    }

    private void SendMessage(Calendar calendar)
    {
        String messageText = MessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Please write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            System.out.println("NotEmpty");
            Calendar now = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");

            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID + "/Chat";
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID + "/Chat";

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).child("Chat").push();

            String messagePushID = userMessageKeyRef.getKey();


            String currentDate = currentDateFormat.format(now.getTime());
            String currentTime = currentTimeFormat.format(now.getTime());
            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", currentTime);
            messageTextBody.put("date", currentDate);
            messageTextBody.put("timestamp", now.getTimeInMillis());

            if (calendar==null) {
                Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                messageBodyDetails.put( messageReceiverRef + "/" + messagePushID, messageTextBody);

                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                            HashMap<String, String> chatNotification = new HashMap<>();
                            chatNotification.put("from", messageSenderID);
                            chatNotification.put("type", "chat");

                            NotificationRef.child(messageReceiverID).push()
                                    .setValue(chatNotification)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                        MessageInputText.setText("");
                    }
                });
            } else {
                DatabaseReference SndDelayRef = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).child("Delay");
                DatabaseReference RcvDelayRef = RootRef.child("Messages").child(messageReceiverID).child(messageSenderID).child("Delay");
                String messageKey = SndDelayRef.push().getKey();
                DatabaseReference SndMessageKeyRef = SndDelayRef.child(messageKey);
                DatabaseReference RcvMessageKeyRef = RcvDelayRef.child(messageKey);

                messageTextBody.put("messageID", messageKey);
                messageTextBody.put("type", "delay");
                messageTextBody.put("displayDate", currentDateFormat.format(calendar.getTime()));
                messageTextBody.put("displayTime", currentTimeFormat.format(calendar.getTime()));
                messageTextBody.put("displayTimestamp", calendar.getTimeInMillis());
                SndMessageKeyRef.updateChildren(messageTextBody);
                RcvMessageKeyRef.updateChildren(messageTextBody);

                DatabaseReference UserDelayRef = RootRef.child("Users").child(messageSenderID).child("DelayMessage");
                UserDelayRef.child(messageKey).setValue("");
                UserDelayRef.child(messageKey).child("type").setValue("chat");
                UserDelayRef.child(messageKey).child("ref").setValue(messageReceiverID);
                UserDelayRef.child(messageKey).child("displayDate").setValue(currentDateFormat.format(calendar.getTime()));
                UserDelayRef.child(messageKey).child("displayTimestamp").setValue(calendar.getTimeInMillis());
            }
            MessageInputText.setText("");
        }
    }
}

