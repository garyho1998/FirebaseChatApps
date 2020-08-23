package com.threebeebox.firebasechatapps;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddMemberFirebaseRecyclerAdapter extends FirebaseRecyclerAdapter<String, ContactsViewHolder> {
    private DatabaseReference UsersRef, GroupNameRef;
    private String currentGroupName, currentGroupID;

    public AddMemberFirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions options,
                                            DatabaseReference UsersRef, DatabaseReference GroupNameRef,
                                            String currentGroupName, String currentGroupID) {
        super(options);
        this.UsersRef = UsersRef;
        this.GroupNameRef = GroupNameRef;
        this.currentGroupID = currentGroupID;
        this.currentGroupName = currentGroupName;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout_with_add, viewGroup, false);
        return new ContactsViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull String model) {
        final String userIDs = getRef(position).getKey();
        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if(dataSnapshot.hasChild("name")){
                        holder.userName.setText(dataSnapshot.child("name").getValue().toString());
                        holder.userStatus.setText(dataSnapshot.child("status").getValue().toString());
                    }else{
                        if(dataSnapshot.hasChild("phoneNumber")){
                            holder.userName.setText(dataSnapshot.child("phoneNumber").getValue().toString());
                        }
                    }

                    if (dataSnapshot.hasChild("image")) {
                        String userImage = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                    }

                    if (dataSnapshot.child("userState").hasChild("state")) {
                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                        String date = dataSnapshot.child("userState").child("date").getValue().toString();

                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat currentDate = new SimpleDateFormat(("MMM dd, yyyy"));
                        String today = currentDate.format(calendar.getTime());
                        if (date.equals(today)) {
                            holder.minorInfo.setText( dataSnapshot.child("userState").child("time").getValue().toString() );
                        } else {
                            holder.minorInfo.setText(date);
                        }
                    }

                    GroupNameRef.child("Member").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.hasChild(userIDs)) {
                                PorterDuffColorFilter greyFilter = new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
                                holder.profileImage.setColorFilter(greyFilter);
                                holder.userName.setTextColor(0xff777777);
                                holder.userStatus.setText("Contact already in group");
                                holder.userStatus.setTextColor(0xff777777);
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

    public void AddMember(int position) {
        String userID = getSnapshots().getSnapshot(position).getKey();

        GroupNameRef.child("Member").child(userID).setValue("");
        UsersRef.child(userID).child("groups").child(currentGroupID).setValue(currentGroupName);

    }
}
