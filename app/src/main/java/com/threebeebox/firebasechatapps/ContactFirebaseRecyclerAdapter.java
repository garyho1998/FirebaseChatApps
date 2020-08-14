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

public class ContactFirebaseRecyclerAdapter extends FirebaseRecyclerAdapter<User, ContactsViewHolder> {
    private DatabaseReference UsersRef;

    public ContactFirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions options, DatabaseReference UsersRef) {
        super(options);
        this.UsersRef = UsersRef;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout_with_delete, viewGroup, false);
        return new ContactsViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull User model) {
        final String userIDs = getRef(position).getKey();

        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.child("userState").hasChild("state"))
                    {
                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                        if (state.equals("online"))
                        {
                            holder.onlineIcon.setVisibility(View.VISIBLE);
                        }
                        else if (state.equals("offline"))
                        {
                            holder.onlineIcon.setVisibility(View.INVISIBLE);
                        }
                    }
                    else
                    {
                        holder.onlineIcon.setVisibility(View.INVISIBLE);
                    }


                    if (dataSnapshot.hasChild("image"))
                    {
                        String userImage = dataSnapshot.child("image").getValue().toString();
                        String profileName = dataSnapshot.child("name").getValue().toString();
                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                        holder.userName.setText(profileName);
                        holder.userStatus.setText(profileStatus);
                        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                    }
                    else
                    {
                        String profileName = dataSnapshot.child("name").getValue().toString();
                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                        holder.userName.setText(profileName);
                        holder.userStatus.setText(profileStatus);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getRef().removeValue();
    }
}
