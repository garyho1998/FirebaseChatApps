package com.threebeebox.firebasechatapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFdActivity extends AppCompatActivity{
// implements AdapterView.OnItemSelectedListener

    private ArrayList<String> contactsList = new ArrayList<String>();

    private Toolbar mToolbar;
    private EditText mPhoneDistictView, mPhoneNoView, mNameView;
    private CircleImageView mUserProfileImage;
    private TextView mUsrName, mUsrStatus, mUid, mResultView, mReminderView, mUsrPhone, mPlusText;
    private Button mSearchBtn, mAddBtn;
    private ProgressDialog loadingBar;
    private String currentUserID, receiverUserID;

    private DatabaseReference RootRef, UsersRef, ContactsRef, currentUserRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_fd);

        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = RootRef.child("Users");
        ContactsRef = RootRef.child("Contacts");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        currentUserRef = RootRef.child("Users").child(currentUserID);

        loadingBar = new ProgressDialog(this);

        mPhoneNoView = (EditText) findViewById(R.id.phone_input);
        mUserProfileImage = (CircleImageView) findViewById(R.id.usr_image_view);
        mUsrName = (TextView) findViewById(R.id.usr_name_view);
        mUsrStatus = (TextView) findViewById(R.id.usr_status_view);
        mUid = (TextView) findViewById(R.id.usr_uid_view);
        mResultView = (TextView) findViewById(R.id.result_view);
        mReminderView = (TextView) findViewById(R.id.reminder_view);
        mSearchBtn = (Button) findViewById(R.id.find_btn);
        mAddBtn = (Button) findViewById(R.id.add_contact_btn);
        mNameView = (EditText) findViewById(R.id.name_input);
        mNameView.setVisibility(View.INVISIBLE);
        mUsrPhone = (TextView) findViewById(R.id.usr_phone_view);
        mPlusText = (TextView) findViewById(R.id.plus_text);
        mPhoneDistictView = (EditText) findViewById(R.id.distict_input);

        /*
        Spinner dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{"click to choose...", "phone number", "user name"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
        */

        mToolbar = (Toolbar) findViewById(R.id.find_fd_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friend via phone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        loadingBar = new ProgressDialog(this);

        contactsList.clear();
    }

    /*
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {

        switch (position) {
            case 0:
                mNameView.setVisibility(View.INVISIBLE);
                mPlusText.setVisibility(View.INVISIBLE);
                mPhoneDistictView.setVisibility(View.INVISIBLE);
                mPhoneNoView.setVisibility(View.INVISIBLE);
                mSearchBtn.setVisibility(View.INVISIBLE);
                break;
            case 1:
                mNameView.setVisibility(View.INVISIBLE);
                mPlusText.setVisibility(View.VISIBLE);
                mPhoneDistictView.setVisibility(View.VISIBLE);
                mPhoneNoView.setVisibility(View.VISIBLE);
                mSearchBtn.setVisibility(View.VISIBLE);

                mSearchBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SearchFriend("phoneNumber");

                        if (mResultView.getText().equals("Not found")) {
                            mReminderView.setVisibility(View.VISIBLE);
                            mResultView.setVisibility(View.VISIBLE);
                            mUserProfileImage.setVisibility(View.INVISIBLE);
                            mUsrName.setVisibility(View.INVISIBLE);
                            mUsrPhone.setVisibility(View.INVISIBLE);
                            mUsrStatus.setVisibility(View.INVISIBLE);
                            mAddBtn.setVisibility(View.INVISIBLE);
                            loadingBar.dismiss();
                        }

                    }
                });
                break;
            case 2:
                mPlusText.setVisibility(View.INVISIBLE);
                mPhoneDistictView.setVisibility(View.INVISIBLE);
                mPhoneNoView.setVisibility(View.INVISIBLE);
                mNameView.setVisibility(View.VISIBLE);
                mSearchBtn.setVisibility(View.VISIBLE);

                mSearchBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SearchFriend("name");

                        if (mResultView.getText().equals("Not found")) {
                            mReminderView.setVisibility(View.VISIBLE);
                            mResultView.setVisibility(View.VISIBLE);
                            mUserProfileImage.setVisibility(View.INVISIBLE);
                            mUsrName.setVisibility(View.INVISIBLE);
                            mUsrPhone.setVisibility(View.INVISIBLE);
                            mUsrStatus.setVisibility(View.INVISIBLE);
                            mAddBtn.setVisibility(View.INVISIBLE);
                            loadingBar.dismiss();
                        }
                    }
                });
                break;

        }
    }
    */


    @Override
    protected void onStart() {
        super.onStart();

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFriend("phoneNumber");

                if (mResultView.getText().equals("Not found")) {
                    mReminderView.setVisibility(View.VISIBLE);
                    mResultView.setVisibility(View.VISIBLE);
                    mUserProfileImage.setVisibility(View.INVISIBLE);
                    mUsrName.setVisibility(View.INVISIBLE);
                    mUsrPhone.setVisibility(View.INVISIBLE);
                    mUsrStatus.setVisibility(View.INVISIBLE);
                    mAddBtn.setVisibility(View.INVISIBLE);
                    loadingBar.dismiss();
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


    private void SearchFriend(String type) {
        String input = "";
        if (type.equals("phoneNumber")) {
            input = "+" + mPhoneDistictView.getText().toString() + mPhoneNoView.getText().toString();
            input = input.replaceAll("\\s", "");
        } else if (type.equals("name")) {
            input = mNameView.getText().toString();
        }

        mResultView.setText("Not found");

        if (TextUtils.isEmpty(input) || TextUtils.isEmpty(mPhoneNoView.getText().toString())) {
            Toast.makeText(this, "Please enter to search...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Finding Friend");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            UsersRef.orderByChild(type).equalTo(input)
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
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }
    }

    private void GetFriendAndDisplay(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            final String userID = dataSnapshot.getKey();
            mUid.setText(userID);
            if (dataSnapshot.hasChild("name")) {
                String userName = (String) dataSnapshot.child("name").getValue();
                String userStatus = (String) dataSnapshot.child("status").getValue();
                mUsrName.setText(userName);
                mUsrStatus.setText(userStatus);
            } else {
                if (dataSnapshot.hasChild("phoneNumber")) {
                    String phoneNumber = (String) dataSnapshot.child("phoneNumber").getValue();
                    mUsrName.setText(phoneNumber);
                }
            }
            String imageUrl = (String) dataSnapshot.child("image").getValue();
            String userPhone = (String) dataSnapshot.child("phoneNumber").getValue();

            mUsrName.setVisibility(View.VISIBLE);
            mUsrStatus.setVisibility(View.VISIBLE);
            mUserProfileImage.setVisibility(View.VISIBLE);
            mUsrPhone.setVisibility(View.VISIBLE);
            mUsrPhone.setText(userPhone);

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

            if (!userID.equals(currentUserRef.getKey())) {
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
                mAddBtn.setText("This is you");
                mAddBtn.setBackgroundResource(R.drawable.dull_button);
                mAddBtn.setClickable(false);
            }

        }
        loadingBar.dismiss();
    }

    private void CheckIfContactNotExists(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
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
            ContactsRef.child(currentUserID).child(receiverUserID).setValue("Saved")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mAddBtn.setText("Already in your contact!");
                            mAddBtn.setBackgroundResource(R.drawable.dull_button);
                            mAddBtn.setClickable(false);

                            RootRef.child("Messages").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if(!snapshot.hasChild(receiverUserID)){
                                        RootRef.child("Messages").child(currentUserID).child(receiverUserID).setValue("");
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            Toast.makeText(FindFdActivity.this, "Friend added to your contact!", Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }
}
