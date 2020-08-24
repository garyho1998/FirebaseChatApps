package com.threebeebox.firebasechatapps;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.xml.datatype.Duration;

public class AddMemberActivity extends AppCompatActivity implements AddMemberRecyclerItemTouchHelper.RecyclerItemTouchHelperListener{
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private RecyclerView myContactsList;
    private DatabaseReference RootRef, UsersRef, ContactRef, GroupNameRef;
    private String currentUserID, currentGroupName, currentGroupID;
    private AddMemberFirebaseRecyclerAdapter adapter;

    final String TAG = "AddMemberActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupID = getIntent().getExtras().get("groupID").toString();

        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = RootRef.child("Users");
        ContactRef = RootRef.child("Contacts").child(currentUserID);
        GroupNameRef = RootRef.child("Groups").child(currentGroupID);

        myContactsList = (RecyclerView) findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = (Toolbar) findViewById(R.id.add_members_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Members");

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new AddMemberRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(myContactsList);
    }

    public void onStart() {
        super.onStart();

        GroupNameRef.child("Member").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue().toString().equals("Admin")){
                    GroupSingleton.getInstance().isAdmin = true;

                }else{
                    GroupSingleton.getInstance().isAdmin = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(ContactRef, String.class)
                        .build();

        adapter = new AddMemberFirebaseRecyclerAdapter(options, UsersRef, GroupNameRef, currentGroupName, currentGroupID);
        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage("Add this user to group?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        adapter.AddMember(viewHolder.getAdapterPosition());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                }).show();
    }



}
