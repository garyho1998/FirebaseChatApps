package com.threebeebox.firebasechatapps;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements ChatsFragmentRecyclerAdapter.OnChatListener{
    private static final String TAG = "ChatFragment";
    private View PrivateChatsView;
    private RecyclerView chatsListRecyclerView;
    ChatsFragmentRecyclerAdapter chatsFragmentRecyclerAdapter;
    final ArrayList<User> UserList = new ArrayList<>();

    private DatabaseReference ChatsRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID = "";

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(TAG, "onCreateView");
        PrivateChatsView = inflater.inflate(R.layout.fragment_chat, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Messages");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        initRecyclerView();

        return PrivateChatsView;
    }

    private void initRecyclerView(){
        chatsListRecyclerView = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsFragmentRecyclerAdapter = new ChatsFragmentRecyclerAdapter(UserList, this);
        chatsListRecyclerView.setAdapter(chatsFragmentRecyclerAdapter);
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        ChatsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final String[] retImage = {"default_image"};
                    for(DataSnapshot child : dataSnapshot.getChildren()) {
                        final String targetID = child.getKey();

                        UsersRef.child(targetID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot userSnapshot) {
                                User target = new User();
                                target.userId = targetID;
                                target.name = userSnapshot.child("name").getValue().toString();
                                target.status = userSnapshot.child("status").getValue().toString();

                                if(userSnapshot.hasChild("image")){
                                    target.image = userSnapshot.child("image").getValue().toString();
                                }else{
                                    target.image = retImage[0];
                                }
                                if (userSnapshot.child("userState").hasChild("state")) {
                                    target.state = userSnapshot.child("userState").child("state").getValue().toString();
                                    target.date = userSnapshot.child("userState").child("date").getValue().toString();
                                    target.time = userSnapshot.child("userState").child("time").getValue().toString();
                                }
                                UserList.add(target);
                                chatsFragmentRecyclerAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        UserList.clear();
    }

    @Override
    public void onChatClick(int position) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        User target = UserList.get(position);
        intent.putExtra("visit_user_id", target.userId);
        intent.putExtra("visit_user_name", target.name);
        intent.putExtra("visit_image", target.image);
        startActivity(intent);
    }
}
