package com.example.firebasechatapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFdActivity extends AppCompatActivity {

    private ArrayList<String> contactsList = new ArrayList<String>();

    private Toolbar mToolbar;
    private EditText mPhoneNoView;
    private CircleImageView mUserProfileImage;
    private TextView mUsrName, mUsrStatus, mUid, mResultView, mReminderView;
    private Button mSearchBtn, mAddBtn;
    private ProgressDialog loadingBar;
    private String currentUserID, receiverUserID;

    private DatabaseReference UsersRef, ContactsRef, currentUserRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_fd);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        mPhoneNoView = (EditText) findViewById(R.id.phone_input);
        mUserProfileImage = (CircleImageView) findViewById(R.id.usr_image_view);
        mUsrName = (TextView) findViewById(R.id.usr_name_view);
        mUsrStatus = (TextView) findViewById(R.id.usr_status_view);
        mUid = (TextView) findViewById(R.id.usr_uid_view);
        mResultView = (TextView) findViewById(R.id.result_view);
        mReminderView = (TextView) findViewById(R.id.reminder_view);
        mSearchBtn = (Button) findViewById(R.id.find_btn);
        mAddBtn = (Button) findViewById(R.id.add_contact_btn);

        mToolbar = (Toolbar) findViewById(R.id.find_fd_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friend via phone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        contactsList.clear();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SearchFriend();

                if (mResultView.getText().equals("Not found")) {
                    mReminderView.setVisibility(View.VISIBLE);
                    mResultView.setVisibility(View.VISIBLE);
                    mUserProfileImage.setVisibility(View.INVISIBLE);
                    mUsrName.setVisibility(View.INVISIBLE);
                    mUsrStatus.setVisibility(View.INVISIBLE);
                    mAddBtn.setVisibility(View.INVISIBLE);
                }

            }
        });

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddFriendToContact();
            }
        });
    }

    private void SearchFriend() {
        final String input = mPhoneNoView.getText().toString();
        final String phone = input.replaceAll("\\s", "");
        String debugPhone = "+85222345678";
        mResultView.setText("Not found");
        Log.d("findFriend", "phone input to compare: " + phone);

        if (TextUtils.isEmpty(debugPhone)) {
            Toast.makeText(this, "Please enter phone number...", Toast.LENGTH_SHORT).show();
        } else {
            UsersRef.orderByChild("phoneNumber").equalTo(debugPhone)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            GetFriendAndDisplay(dataSnapshot);
                        }
                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            GetFriendAndDisplay(dataSnapshot);
                        }
                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {}
                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }
    }
    private void GetFriendAndDisplay(DataSnapshot dataSnapshot){
        if (dataSnapshot.exists()) {
            final String userID = dataSnapshot.getKey();
            String userName = (String) dataSnapshot.child("name").getValue();
            String userStatus = (String) dataSnapshot.child("status").getValue();
            String imageUrl = (String) dataSnapshot.child("image").getValue();

            mUsrName.setVisibility(View.VISIBLE);
            mUsrName.setText(userName);
            mUsrStatus.setVisibility(View.VISIBLE);
            mUsrStatus.setText(userStatus);
            mUid.setText(userID);
            mUserProfileImage.setVisibility(View.VISIBLE);

            if (imageUrl != null) {
                Picasso.get().load(imageUrl).placeholder(R.drawable.profile_image).into(mUserProfileImage);
            } else {
                Picasso.get().load(R.drawable.profile_image).into(mUserProfileImage);
            }
            mAddBtn.setText("Add to contact");
            mAddBtn.setBackgroundResource(R.drawable.buttons);
            mAddBtn.setClickable(true);
            mAddBtn.setVisibility(View.VISIBLE);
            mResultView.setText("Found");

            if (!userID.equals(currentUserRef.getKey())){
                ContactsRef.child(currentUserID).orderByKey().equalTo(userID)
                        .addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                CheckIfContactNotExists(dataSnapshot);
                            }
                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                CheckIfContactNotExists(dataSnapshot);
                            }
                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {}
                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
            }else{
                mAddBtn.setText("This is you");
                mAddBtn.setBackgroundResource(R.drawable.dull_button);
                mAddBtn.setClickable(false);
            }

        }
    }
    private void CheckIfContactNotExists(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()){
            mAddBtn.setText("Already in your contact!");
            mAddBtn.setBackgroundResource(R.drawable.dull_button);
            mAddBtn.setClickable(false);
        }
    }
    private void AddFriendToContact() {
        receiverUserID = mUid.getText().toString();

        if (TextUtils.isEmpty(receiverUserID)) {
            Toast.makeText(this, "No target to add to contact", Toast.LENGTH_SHORT).show();
        } else {
            ContactsRef.child(currentUserID).child(receiverUserID)
                    .child("Contacts").setValue("Saved")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mAddBtn.setText("Already in your contact!");
                            mAddBtn.setBackgroundResource(R.drawable.dull_button);
                            mAddBtn.setClickable(false);
                            Toast.makeText(FindFdActivity.this, "Friend added to your contact!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }
}