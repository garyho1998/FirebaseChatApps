package com.threebeebox.firebasechatapps;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddMemberActivity extends AppCompatActivity implements ContactRecyclerItemTouchHelper.RecyclerItemTouchHelperListener{
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

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ContactRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(myContactsList);
    }

    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ContactRef, Contacts.class)
                        .build();

        adapter = new AddMemberFirebaseRecyclerAdapter(options, UsersRef, GroupNameRef, currentGroupName, currentGroupID);

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        adapter.AddMember(viewHolder.getAdapterPosition());
    }



}
