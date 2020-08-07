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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private String senderUserID, receiverUserID;

    private DatabaseReference UsersRef, ContactsRef, currentUserRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_fd);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(senderUserID);

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
        final String phone = input.replaceAll("\\s","");
        Log.d("findFriend", "phone input to compare: " + phone);
        mResultView.setText("Not found");

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please enter phone number...", Toast.LENGTH_SHORT).show();
        } else {
//            loadingBar.setTitle("Find Friend");
//            loadingBar.setMessage("Please wait...");
//            loadingBar.setCanceledOnTouchOutside(true);
//            loadingBar.show();


            mAddBtn.setText("Add to contact");
            mAddBtn.setBackgroundResource(R.drawable.buttons);
            mAddBtn.setClickable(true);

            UsersRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    final Iterator<DataSnapshot> userItems = dataSnapshot.getChildren().iterator();

                    while (userItems.hasNext()) {
                        DataSnapshot usrItem = userItems.next();
                        DatabaseReference UsrRef = usrItem.getRef();

                        User user = usrItem.getValue(User.class);
                        String number = user.phoneNumber;

                        if ( number.equals(phone) || number.equals("+852"+phone) || number.equals("+"+phone)) {
//                            loadingBar.dismiss();
                            mUsrName.setVisibility(View.VISIBLE);
                            mUsrName.setText(user.name);
                            mUsrStatus.setVisibility(View.VISIBLE);
                            mUsrStatus.setText(user.status);
                            mUid.setText(user.uid);
                            mUserProfileImage.setVisibility(View.VISIBLE);
                            if (user.image!=null) {
                                Picasso.get().load(user.image).placeholder(R.drawable.profile_image).into(mUserProfileImage);
                            } else {
                                Picasso.get().load(R.drawable.profile_image).into(mUserProfileImage);
                            }
                            mAddBtn.setVisibility(View.VISIBLE);
                            mResultView.setText("Found");

                            // check if the contact already exists...
                            receiverUserID = mUid.getText().toString();
                            if (CheckIfContactExists(receiverUserID)) {
//                                contactsList.clear();
                                SetPageForContactAdded();
                            }
//                            contactsList.clear();

                            //check if the user searched is current user
                            if (usrItem.getRef().getKey().equals(currentUserRef.getKey())) {
                                mAddBtn.setText("This is you");
                                mAddBtn.setBackgroundResource(R.drawable.dull_button);
                                mAddBtn.setClickable(false);
                                return;
                            }

                            break;
                        }
                    }
//                    loadingBar.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
//                Log.e("!_@@@_ERROR_>>", "onCancelled", firebaseError.toException());
                }

            });

        }
    }

    private Boolean CheckIfContactExists(final String uid) {
        DatabaseReference UserContactRef = ContactsRef.child(senderUserID);

        UserContactRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();

                while (items.hasNext()) {
                    DataSnapshot item = items.next();
                    String contactUid = item.getRef().getKey();
                    contactsList.add(contactUid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.d("findFriend", "inside CheckIfContactExists, result for " + uid + " is " + Boolean.toString(contactsList.contains(uid)));
        Log.d("findFriend", "length of list: " + contactsList.size());

        boolean result = contactsList.contains(uid);
        contactsList.clear();

        return result;
    }

    private void AddFriendToContact() {
        receiverUserID = mUid.getText().toString();

        if (TextUtils.isEmpty(receiverUserID)) {
            Toast.makeText(this, "No target to add to contact", Toast.LENGTH_SHORT).show();
        } else {
            ContactsRef.child(senderUserID).child(receiverUserID)
                    .child("Contacts").setValue("Saved")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            SetPageForContactAdded();
                            Toast.makeText(FindFdActivity.this, "Friend added to your contact!", Toast.LENGTH_SHORT).show();
                            /*
                            if (task.isSuccessful())
                            {
                                ContactsRef.child(receiverUserID).child(senderUserID)
                                        .child("Contacts").setValue("Saved")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                SetPageForContactAdded();
                                            }
                                        });
                            }*/
                        }
                    });
        }

    }

    private void SetPageForContactAdded() {
        mAddBtn.setText("Already in your contact!");
        mAddBtn.setBackgroundResource(R.drawable.dull_button);
        mAddBtn.setClickable(false);
    }
}