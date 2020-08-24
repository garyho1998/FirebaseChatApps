package com.threebeebox.firebasechatapps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class GroupFinishCreateActivity extends AppCompatActivity implements AddMemberRecyclerItemTouchHelper.RecyclerItemTouchHelperListener{
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    FloatingActionButton next, finish;
    TextView groupNameTV;
    private RecyclerView myContactsList;
    private DatabaseReference RootRef, UsersRef, ContactRef, GroupNameRef;
    private String currentUserID, currentGroupName, currentGroupID;
    private AddMemberFirebaseRecyclerAdapter adapter;

    final String TAG = "GroupCreateActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_finish);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = RootRef.child("Users");
        ContactRef = RootRef.child("Contacts").child(currentUserID);
        currentGroupID = getIntent().getExtras().getString("groupID");
        currentGroupName = getIntent().getExtras().getString("groupName");
        GroupNameRef = RootRef.child("Groups").child(currentGroupID);
        initView();

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new AddMemberRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(myContactsList);
    }
    public void initView(){
        groupNameTV = (TextView) findViewById(R.id.group_name);
        finish = (FloatingActionButton) findViewById(R.id.finish);
        myContactsList = (RecyclerView) findViewById(R.id.member_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(this));
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Group");

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupChatIntent = new Intent(GroupFinishCreateActivity.this, GroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                groupChatIntent.putExtra("groupID", currentGroupID);
                startActivity(groupChatIntent);
            }
        });
    }
    public void onStart() {
        super.onStart();
        SetRecyclerAdapter();
    }

    public void SetRecyclerAdapter(){
        GroupSingleton.getInstance().isAdmin = true;

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
        adapter.AddMember(viewHolder.getAdapterPosition());
        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
    }
}
