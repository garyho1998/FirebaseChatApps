package com.threebeebox.firebasechatapps;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupFragment extends Fragment {
    private static final String TAG = "GroupFragment";
    private View groupFragmentView;
    private RecyclerView GroupList;
    private ArrayAdapter<Group> arrayAdapter;
    private ArrayList<Group> list_of_groups = new ArrayList<>();

    private DatabaseReference GroupRef, UserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String sToday = "";

    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(TAG, "onCreateView");
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        groupFragmentView = inflater.inflate(R.layout.fragment_group, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        GroupList = (RecyclerView) groupFragmentView.findViewById(R.id.groups_list);
        GroupList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat(("MMM dd, yyyy"));
        sToday = currentDate.format(calendar.getTime());

        RetrieveAndDisplayGroups();

        return groupFragmentView;
    }

    private void RetrieveAndDisplayGroups() {
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(UserRef.child("groups"), String.class)
                        .build();
        FirebaseRecyclerAdapter<String, GroupsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<String, GroupsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GroupsViewHolder holder, int i, @NonNull final String GroupName) {
                final String GroupID = getRef(i).getKey();
                UserRef.child("groups").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            GroupRef.child(GroupID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot groupSnapshot) {
                                    if (groupSnapshot.exists()) {
                                        holder.GroupName.setText(GroupName);

                                        if (groupSnapshot.hasChild("image")) {
                                            String image = groupSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(holder.image);
                                        }

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                                                groupChatIntent.putExtra("groupName", GroupName);
                                                groupChatIntent.putExtra("groupID", GroupID);
                                                startActivity(groupChatIntent);
                                            }
                                        });

                                        groupSnapshot.getRef().child("Message").orderByKey().limitToLast(1)
                                                .addChildEventListener(new ChildEventListener() {
                                                    @Override
                                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                        setLastTimeAndMsg(dataSnapshot, holder);
                                                    }
                                                    @Override
                                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                                        setLastTimeAndMsg(dataSnapshot, holder);
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
                                        dataSnapshot.getRef().removeValue();
                                        System.out.println(GroupID + " not exist anymore");
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_display_layout, parent, false);
                return new GroupsViewHolder(view);
            }
        };

        GroupList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    @Override
    public void onResume() {
        super.onResume();
        RetrieveAndDisplayGroups();
    }

    public static class GroupsViewHolder extends RecyclerView.ViewHolder {
        TextView GroupName, GroupMsg, lastSend;
        CircleImageView image;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);

            GroupName = itemView.findViewById(R.id.group_name);
            image = itemView.findViewById(R.id.group_image);
            GroupMsg = itemView.findViewById(R.id.group_msg);
            lastSend = itemView.findViewById(R.id.minor_info);
        }
    }

    public void setLastTimeAndMsg(DataSnapshot dataSnapshot, GroupsViewHolder holder){
        if(dataSnapshot.hasChild("type")){
            String last_time = dataSnapshot.child("time").getValue().toString();
            String last_date = dataSnapshot.child("date").getValue().toString();
            if (last_date.equals(sToday)) {
                holder.lastSend.setText(last_time);
            } else {
                holder.lastSend.setText(last_date);
            }

            String type = dataSnapshot.child("type").getValue().toString();
            if (type.equals("normal")) { //text type
                String text_msg = dataSnapshot.child("message").getValue().toString();
                holder.GroupMsg.setText(text_msg);
            } else if (type.equals("image")) {
                holder.GroupMsg.setText("[Image]");
            }
        }

    }

}


