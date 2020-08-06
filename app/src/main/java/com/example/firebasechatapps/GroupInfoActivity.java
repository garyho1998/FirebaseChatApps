package com.example.firebasechatapps;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GroupInfoActivity extends AppCompatActivity implements ContactRecyclerItemTouchHelper.RecyclerItemTouchHelperListener{
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private RecyclerView MemberList;
    private DatabaseReference RootRef, UsersRef, ContactRef, GroupNameRef;
    private String currentUserID, currentGroupName, currentGroupID;
    private GroupInfoFirebaseRecyclerAdapter adapter;
    RelativeLayout Add_member_relativeLayout;
    final String TAG = "GroupInfoActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupID = getIntent().getExtras().get("groupID").toString();

        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = RootRef.child("Users");
        ContactRef = RootRef.child("Contacts").child(currentUserID);
        GroupNameRef = RootRef.child("Groups").child(currentGroupID);

        MemberList = (RecyclerView) findViewById(R.id.member_list);
        MemberList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        Add_member_relativeLayout = (RelativeLayout) findViewById(R.id.add_member_relativeLayout);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Group Info");

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ContactRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(MemberList);
    }

    public void onStart() {
        super.onStart();
        String member = " ";
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(GroupNameRef.child("Member"), String.class)
                        .build();

        adapter = new GroupInfoFirebaseRecyclerAdapter(options, UsersRef, GroupNameRef, currentGroupName, currentGroupID, currentUserID);

        MemberList.setAdapter(adapter);
        adapter.startListening();
        Add_member_relativeLayout.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        Intent intent = new Intent(GroupInfoActivity.this, AddMemberActivity.class);
                        intent.putExtra("groupName", currentGroupName);
                        intent.putExtra("groupID", currentGroupID);
                        startActivityForResult(intent, 0);
                    }
                });
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        adapter.deleteItem(viewHolder.getAdapterPosition());
    }



}
