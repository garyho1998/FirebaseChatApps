package com.threebeebox.firebasechatapps;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements EditDelayMsgDialog.EditMsgDialogListener {
    private Toolbar mToolbar;
    private BottomNavigationView bottomNav;
//    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID, currentGroupName, currentGroupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#FF9800\">" + getString(R.string.app_name) + "</font>"));
        getSupportActionBar().setElevation(20);

        bottomNav = findViewById(R.id.bottom_nav);
        if (currentUser == null) {
            SendUserToLoginActivity();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_tabs_pager, new ChatFragment()).commit();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            SendUserToLoginActivity();
        } else {
            updateUserStatus("online");
            VerifyUserExistance();

            BottomNavigationView.OnNavigationItemSelectedListener navListener =
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            Fragment selectedFragment = null;

                            switch (item.getItemId()) {
                                case R.id.nav_chat:
                                    selectedFragment = new ChatFragment();
                                    break;
                                case R.id.nav_group:
                                    selectedFragment = new GroupFragment();
                                    break;
                                case R.id.nav_contact:
                                    selectedFragment = new ContactFragment();
                                    break;
                                case R.id.nav_calender:
                                    selectedFragment = new CalendarFragment();
                                    break;
                            }
                            getSupportFragmentManager().beginTransaction().replace(R.id.main_tabs_pager, selectedFragment).commit();
                            return true;
                        }
                    };
            bottomNav.setOnNavigationItemSelectedListener(navListener);


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        currentUserID = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.child("name").exists())) {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToLoginActivity() {
        updateUserStatus("offline");
        currentUser = null;

        Intent intent = new Intent(MainActivity.this, PhoneLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void SendUserToSettingsActivity() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void SendUserToFindFriendsActivity() {
        Intent intent = new Intent(MainActivity.this, FindFdActivity    .class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_option) {
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if (item.getItemId() == R.id.main_settings_option) {
            SendUserToSettingsActivity();
        }
        if (item.getItemId() == R.id.main_create_group_option) {
            RequestNewGroup();
        }
        if (item.getItemId() == R.id.main_fd_option) {
            SendUserToFindFirendActivity();
        }
        return true;
    }

    private void SendUserToFindFirendActivity() {
        Intent intent = new Intent(MainActivity.this, FindFdActivity.class);
        startActivity(intent);
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Coding Group");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(MainActivity.this, "Please write Group Name...", Toast.LENGTH_SHORT).show();
                } else {
                    CreateNewGroup(groupName);

                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void CreateNewGroup(final String groupName) {
        final DatabaseReference GroupRef = RootRef.child("Groups");
        final String groupID = GroupRef.push().getKey();
        GroupRef.child(groupID).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            GroupRef.child(groupID).child("DelayMessage").setValue("");
                            GroupRef.child(groupID).child("Message").setValue("");
                            GroupRef.child(groupID).child("Member").child(currentUserID).setValue("Admin");
                            GroupRef.child(groupID).child("GroupName").setValue(groupName);
                            RootRef.child("Users").child(currentUserID).child("groups").child(groupID).setValue(groupName);

                            Intent intent = new Intent(MainActivity.this, GroupFinishCreateActivity.class);
                            intent.putExtra("groupName", groupName);
                            intent.putExtra("groupID", groupID);
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, groupName + " group is Created Successfully...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void updateUserStatus(String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat(("MMM dd, yyyy"));
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat(("hh:mm a"));
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("time", saveCurrentTime);
        onlineState.put("date", saveCurrentDate);
        onlineState.put("state", state);

        if (currentUser != null) {
            currentUserID = currentUser.getUid();

            RootRef.child("Users").child(currentUserID).child("userState")
                    .updateChildren(onlineState);
        }

    }

    //For edit msg in CalendarFragment
    @Override
    public void applyEdit(String type, String sndID, String rcvID, String groupID, String msgID, String msg, String date, String time) {
        Toast.makeText(MainActivity.this, "Delay message edited" + msg, Toast.LENGTH_SHORT).show();
        if(groupID!=null){
            DatabaseReference groupMsgRef = RootRef.child("Groups").child(groupID).child("DelayMessage").child(msgID);
            groupMsgRef.child("message").setValue(msg);
            groupMsgRef.child("displayDate").setValue(date);
            groupMsgRef.child("displayTime").setValue(time);
        }
        DatabaseReference userMsgRef = RootRef.child("Users").child(currentUserID).child("DelayMessage").child(msgID);
        userMsgRef.child("displayDate").setValue(date);
        userMsgRef.child("message").setValue(msg);
        userMsgRef.child("displayTime").setValue(time);
        onResume();
    }
}
