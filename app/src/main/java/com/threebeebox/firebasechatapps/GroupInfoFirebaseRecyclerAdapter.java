package com.threebeebox.firebasechatapps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class GroupInfoFirebaseRecyclerAdapter extends FirebaseRecyclerAdapter<String, ContactsViewHolder> {
    private DatabaseReference UsersRef, GroupNameRef;
    private String currentGroupName, currentGroupID, currentUserID;

    public GroupInfoFirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions options,
                                            DatabaseReference UsersRef, DatabaseReference GroupNameRef,
                                            String currentGroupName, String currentGroupID, String currentUserID) {
        super(options);
        this.UsersRef = UsersRef;
        this.GroupNameRef = GroupNameRef;
        this.currentGroupID = currentGroupID;
        this.currentGroupName = currentGroupName;
        this.currentUserID = currentUserID;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout_with_delete, viewGroup, false);
        return new ContactsViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull String value) {
        final String userId = getRef(position).getKey();
        if(value.equals("Admin")){
            holder.minorInfo.setText("Admin");
            if (userId.equals(currentUserID)){
                GroupSingleton.getInstance().isAdmin = true;
                System.out.println("isAdmin: true");
            }
        }else {
            holder.minorInfo.setText("");
        }
        System.out.println("userId:" + userId);
        GroupNameRef.child("Member").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UsersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            if (userSnapshot.exists()) {

                                String profileName = userSnapshot.child("name").getValue().toString();
                                String profileStatus = userSnapshot.child("status").getValue().toString();
                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);

                                if (userSnapshot.child("userState").hasChild("state")) {
                                    String state = userSnapshot.child("userState").child("state").getValue().toString();
                                    String date = userSnapshot.child("userState").child("date").getValue().toString();
                                    String time = userSnapshot.child("userState").child("time").getValue().toString();
                                }
                                if (userSnapshot.hasChild("image")) {
                                    String userImage = userSnapshot.child("image").getValue().toString();
                                    Picasso.get().load(userImage).placeholder(R.drawable.user_icon).into(holder.profileImage);
                                }
                            }else{
                                dataSnapshot.getRef().removeValue();
                                System.out.println(userId +" not exist anymore");
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

    public void deleteItem(int position) {
        final String userId = getSnapshots().getSnapshot(position).getKey();
        getSnapshots().getSnapshot(position).getRef().removeValue();
        System.out.println("deleteItem userID: " + userId );
        UsersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userSnapshot) {
                if (userSnapshot.exists()) {
                    userSnapshot.child("groups").child(currentGroupID).getRef().removeValue();
                }else{
                    System.out.println(userId +" not exist when deleteItem");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
