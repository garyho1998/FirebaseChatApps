package com.threebeebox.firebasechatapps;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupCreateActivity extends AppCompatActivity{
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    FloatingActionButton next;
    EditText groupNameET;
    private DatabaseReference RootRef;
    private String currentUserID, currentGroupName, currentGroupID;

    final String TAG = "GroupCreateActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        initView();
    }
    public void initView(){
        groupNameET = (EditText) findViewById(R.id.group_name);
        next = (FloatingActionButton) findViewById(R.id.next);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Group");

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentGroupName = groupNameET.getText().toString();
                if (currentGroupName.isEmpty()) {
                    Toast.makeText(GroupCreateActivity.this, "Please write Group Name", Toast.LENGTH_SHORT).show();
                } else {
                    CreateNewGroup(currentGroupName);
                    Intent intent = new Intent(GroupCreateActivity.this, GroupFinishCreateActivity.class);
                    intent.putExtra("groupName", currentGroupName);
                    intent.putExtra("groupID", currentGroupID);
                    startActivity(intent);
                }
            }
        });
    }
    public void onStart() {
        super.onStart();
    }

    private void CreateNewGroup(final String groupName) {
        final DatabaseReference GroupRef = RootRef.child("Groups");
        currentGroupID = GroupRef.push().getKey();
        GroupRef.child(currentGroupID).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            GroupRef.child(currentGroupID).child("DelayMessage").setValue("");
                            GroupRef.child(currentGroupID).child("Message").setValue("");
                            GroupRef.child(currentGroupID).child("Member").child(currentUserID).setValue("Admin");
                            GroupRef.child(currentGroupID).child("GroupName").setValue(groupName);
                            RootRef.child("Users").child(currentUserID).child("groups").child(currentGroupID).setValue(groupName);

                            Toast.makeText(GroupCreateActivity.this, groupName + " is Created Successfully, Please Add Members", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
